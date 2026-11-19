package com.rodrigosambadesaa.garajeapi.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.rodrigosambadesaa.garajeapi.domain.Trabajo;
import com.rodrigosambadesaa.garajeapi.domain.TrabajoTipo;
import com.rodrigosambadesaa.garajeapi.dto.AumentarMaterialRequest;
import com.rodrigosambadesaa.garajeapi.dto.CrearTrabajoRequest;
import com.rodrigosambadesaa.garajeapi.dto.TipoMaterial;
import com.rodrigosambadesaa.garajeapi.repository.TrabajoRepository;

@Service
@Transactional
public class TrabajoService {

  // Coincide con precision=12, scale=2 de las columnas JPA.
  private static final BigDecimal MAXIMO_ALMACENABLE = new BigDecimal("9999999999.99");

  private final TrabajoRepository trabajoRepository;

  public TrabajoService(TrabajoRepository trabajoRepository) {
    this.trabajoRepository = trabajoRepository;
  }

  @Transactional(readOnly = true)
  public List<Trabajo> listarActivos() {
    return trabajoRepository.findByEliminadoFalseOrderByDescripcionAsc();
  }

  @Transactional(readOnly = true)
  public Trabajo obtener(long id) {
    return trabajoRepository.findByIdAndEliminadoFalse(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trabajo no encontrado"));
  }

  private Trabajo obtenerParaActualizar(long id) {
    return trabajoRepository.findActivoParaActualizar(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trabajo no encontrado"));
  }

  public Trabajo crear(CrearTrabajoRequest request) {
    if (request == null || request.tipo() == null || request.descripcion() == null
        || request.descripcion().isBlank() || request.descripcion().length() > 300
        || request.descripcion().trim().length() > 300) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos de trabajo no validos");
    }
    Trabajo t = new Trabajo();
    t.setTipo(request.tipo());
    t.setDescripcion(request.descripcion().trim());
    return trabajoRepository.save(t);
  }

  public Trabajo aumentarHoras(long id, BigDecimal horas) {
    validarIncremento(horas, "horas");
    Trabajo t = obtenerParaActualizar(id);
    validarModificable(t);
    t.setHoras(acumular(t.getHoras(), horas));
    return trabajoRepository.save(t);
  }

  public Trabajo aumentarMaterial(long id, AumentarMaterialRequest request) {
    if (request == null || request.tipoMaterial() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de material obligatorio");
    }
    BigDecimal coste = request.coste();
    validarIncremento(coste, "coste");
    TipoMaterial tipoMaterial = request.tipoMaterial();
    Trabajo t = obtenerParaActualizar(id);
    validarModificable(t);

    if ( t.getTipo() == TrabajoTipo.REVISION ) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una revision no acepta coste de material");
    }

    if ( t.getTipo() == TrabajoTipo.REPARACION_MECANICA ) {
      if (tipoMaterial != TipoMaterial.PIEZAS) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Una reparacion mecanica solo acepta material de tipo 'piezas'");
      }
      t.setCostePiezas(acumular(t.getCostePiezas(), coste));
      return trabajoRepository.save(t);
    }

    switch (tipoMaterial) {
      case PINTURA -> t.setCostePintura(acumular(t.getCostePintura(), coste));
      case CHAPA -> t.setCosteChapa(acumular(t.getCosteChapa(), coste));
      case PIEZAS -> t.setCostePiezas(acumular(t.getCostePiezas(), coste));
    }

    return trabajoRepository.save(t);
  }

  public Trabajo finalizar(long id) {
    Trabajo t = obtenerParaActualizar(id);
    validarModificable(t);
    t.setFinalizado(true);
    return trabajoRepository.save(t);
  }

  public void eliminar(long id) {
    Trabajo t = obtenerParaActualizar(id);
    t.setEliminado(true);
    trabajoRepository.save(t);
  }

  public void resetearBbdd() {
    trabajoRepository.deleteAllInBatch();
  }

  public void cargarEjemplo() {
    if ( trabajoRepository.existsByEliminadoFalse() ) {
      return;
    }

    Trabajo mecanica = new Trabajo();
    mecanica.setTipo(TrabajoTipo.REPARACION_MECANICA);
    mecanica.setDescripcion("Cambio de embrague");
    mecanica.setHoras(new BigDecimal("4.5"));
    mecanica.setCostePiezas(new BigDecimal("320"));

    Trabajo chapa = new Trabajo();
    chapa.setTipo(TrabajoTipo.REPARACION_CHAPA_PINTURA);
    chapa.setDescripcion("Golpe lateral y pintado");
    chapa.setHoras(new BigDecimal("6"));
    chapa.setCosteChapa(new BigDecimal("180"));
    chapa.setCostePintura(new BigDecimal("95"));

    Trabajo revision = new Trabajo();
    revision.setTipo(TrabajoTipo.REVISION);
    revision.setDescripcion("Revision anual");
    revision.setHoras(new BigDecimal("1.5"));

    trabajoRepository.saveAll(List.of(mecanica, chapa, revision));
  }

  private static void validarIncremento(BigDecimal cantidad, String campo) {
    if (cantidad == null || cantidad.signum() < 0 || cantidad.stripTrailingZeros().scale() > 2
        || cantidad.compareTo(MAXIMO_ALMACENABLE) > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Incremento de " + campo + " no valido: debe ser no negativo y tener hasta dos decimales");
    }
  }

  private static BigDecimal acumular(BigDecimal actual, BigDecimal incremento) {
    BigDecimal total = Objects.requireNonNull(actual).add(incremento);
    if (total.compareTo(MAXIMO_ALMACENABLE) > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "El importe acumulado supera el maximo admitido");
    }
    return total;
  }

  private void validarModificable(Trabajo trabajo) {
    if ( trabajo.isEliminado() ) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trabajo eliminado");
    }
    if ( trabajo.isFinalizado() ) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trabajo finalizado");
    }
  }
}

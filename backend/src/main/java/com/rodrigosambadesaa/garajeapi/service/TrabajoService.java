package com.rodrigosambadesaa.garajeapi.service;

import java.math.BigDecimal;
import java.util.List;
 
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

  private static final BigDecimal MAX_CANTIDAD = new BigDecimal("9999999999.99");

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

  public Trabajo crear(CrearTrabajoRequest request) {
    if (request == null || request.tipo() == null || request.descripcion() == null
        || request.descripcion().isBlank() || request.descripcion().trim().length() > 300) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo y descripcion validos son obligatorios");
    }
    Trabajo t = new Trabajo();
    t.setTipo(request.tipo());
    t.setDescripcion(request.descripcion());
    return trabajoRepository.save(t);
  }

  public Trabajo aumentarHoras(long id, BigDecimal horas) {
    validarCantidad(horas);
    Trabajo t = obtenerParaModificar(id);
    validarModificable(t);
    BigDecimal total = t.getHoras().add(horas);
    validarCantidad(total);
    t.setHoras(total);
    return trabajoRepository.save(t);
  }

  public Trabajo aumentarMaterial(long id, AumentarMaterialRequest request) {
    if (request == null || request.tipoMaterial() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de material es obligatorio");
    }
    BigDecimal coste = request.coste();
    validarCantidad(coste);
    Trabajo t = obtenerParaModificar(id);
    validarModificable(t);

    TipoMaterial tipoMaterial = request.tipoMaterial();

    if ( t.getTipo() == TrabajoTipo.REVISION ) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una revision no acepta coste de material");
    }

    if ( t.getTipo() == TrabajoTipo.REPARACION_MECANICA ) {
      if (tipoMaterial != TipoMaterial.PIEZAS) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Una reparacion mecanica solo acepta material de tipo 'piezas'");
      }
      BigDecimal total = t.getCostePiezas().add(coste);
      validarCantidad(total);
      t.setCostePiezas(total);
      return trabajoRepository.save(t);
    }

    switch (tipoMaterial) {
      case PINTURA -> t.setCostePintura(sumarCoste(t.getCostePintura(), coste));
      case CHAPA -> t.setCosteChapa(sumarCoste(t.getCosteChapa(), coste));
      case PIEZAS -> t.setCostePiezas(sumarCoste(t.getCostePiezas(), coste));
    }

    return trabajoRepository.save(t);
  }

  public Trabajo finalizar(long id) {
    Trabajo t = obtenerParaModificar(id);
    validarModificable(t);
    t.setFinalizado(true);
    return trabajoRepository.save(t);
  }

  public void eliminar(long id) {
    Trabajo t = obtenerParaModificar(id);
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

  private Trabajo obtenerParaModificar(long id) {
    return trabajoRepository.findActiveByIdForUpdate(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trabajo no encontrado"));
  }

  private static BigDecimal sumarCoste(BigDecimal actual, BigDecimal incremento) {
    BigDecimal total = actual.add(incremento);
    validarCantidad(total);
    return total;
  }

  private static void validarCantidad(BigDecimal cantidad) {
    if (cantidad == null || cantidad.signum() < 0 || cantidad.stripTrailingZeros().scale() > 2
        || cantidad.compareTo(MAX_CANTIDAD) > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "La cantidad debe estar entre 0 y 9999999999.99, con dos decimales");
    }
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

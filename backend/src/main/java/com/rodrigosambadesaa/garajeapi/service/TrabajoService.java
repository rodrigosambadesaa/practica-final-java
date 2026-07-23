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
    Trabajo t = new Trabajo();
    t.setTipo(request.tipo());
    t.setDescripcion(request.descripcion().trim());
    return trabajoRepository.save(t);
  }

  public Trabajo aumentarHoras(long id, BigDecimal horas) {
    Trabajo t = obtener(id);
    validarModificable(t);
    t.setHoras(t.getHoras().add(horas));
    return trabajoRepository.save(t);
  }

  public Trabajo aumentarMaterial(long id, AumentarMaterialRequest request) {
    Trabajo t = obtener(id);
    validarModificable(t);

    BigDecimal coste = request.coste();
    TipoMaterial tipoMaterial = request.tipoMaterial();

    if ( t.getTipo() == TrabajoTipo.REVISION ) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una revision no acepta coste de material");
    }

    if ( t.getTipo() == TrabajoTipo.REPARACION_MECANICA ) {
      if (tipoMaterial != TipoMaterial.PIEZAS) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Una reparacion mecanica solo acepta material de tipo 'piezas'");
      }
      t.setCostePiezas(t.getCostePiezas().add(coste));
      return trabajoRepository.save(t);
    }

    switch (tipoMaterial) {
      case PINTURA -> t.setCostePintura(t.getCostePintura().add(coste));
      case CHAPA -> t.setCosteChapa(t.getCosteChapa().add(coste));
      case PIEZAS -> t.setCostePiezas(t.getCostePiezas().add(coste));
    }

    return trabajoRepository.save(t);
  }

  public Trabajo finalizar(long id) {
    Trabajo t = obtener(id);
    validarModificable(t);
    t.setFinalizado(true);
    return trabajoRepository.save(t);
  }

  public void eliminar(long id) {
    Trabajo t = obtener(id);
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

  private void validarModificable(Trabajo trabajo) {
    if ( trabajo.isEliminado() ) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trabajo eliminado");
    }
    if ( trabajo.isFinalizado() ) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trabajo finalizado");
    }
  }
}

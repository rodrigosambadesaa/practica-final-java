package com.rodrigosambadesaa.garajeapi.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.rodrigosambadesaa.garajeapi.dto.AumentarHorasRequest;
import com.rodrigosambadesaa.garajeapi.dto.AumentarMaterialRequest;
import com.rodrigosambadesaa.garajeapi.dto.CrearTrabajoRequest;
import com.rodrigosambadesaa.garajeapi.dto.TrabajoResponse;
import com.rodrigosambadesaa.garajeapi.service.TrabajoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trabajos")
@Validated
public class TrabajoController {

  private final TrabajoService trabajoService;

  public TrabajoController(TrabajoService trabajoService) {
    this.trabajoService = trabajoService;
  }

  @GetMapping
  public List<TrabajoResponse> listar() {
    return trabajoService.listarActivos().stream().map(TrabajoResponse::from).toList();
  }

  @GetMapping("/{id}")
  public TrabajoResponse obtener(@PathVariable long id) {
    return TrabajoResponse.from(trabajoService.obtener(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TrabajoResponse crear(@Valid @RequestBody CrearTrabajoRequest request) {
    return TrabajoResponse.from(trabajoService.crear(request));
  }

  @PatchMapping("/{id}/horas")
  public TrabajoResponse aumentarHoras(@PathVariable long id, @Valid @RequestBody AumentarHorasRequest request) {
    return TrabajoResponse.from(trabajoService.aumentarHoras(id, request.horas()));
  }

  @PatchMapping("/{id}/material")
  public TrabajoResponse aumentarMaterial(@PathVariable long id,
      @Valid @RequestBody AumentarMaterialRequest request) {
    return TrabajoResponse.from(trabajoService.aumentarMaterial(id, request));
  }

  @PatchMapping("/{id}/finalizar")
  public TrabajoResponse finalizar(@PathVariable long id) {
    return TrabajoResponse.from(trabajoService.finalizar(id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable long id) {
    trabajoService.eliminar(id);
  }
}

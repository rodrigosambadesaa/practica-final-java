package com.rodrigosambadesaa.garajeapi.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rodrigosambadesaa.garajeapi.service.TrabajoService;

@RestController
@RequestMapping("/api/db")
public class DatabaseController {

  private final TrabajoService trabajoService;

  public DatabaseController(TrabajoService trabajoService) {
    this.trabajoService = trabajoService;
  }

  @PostMapping("/reset")
  public String resetear() {
    trabajoService.resetearBbdd();
    return "BBDD reseteada";
  }

  @PostMapping("/seed")
  public String seed() {
    trabajoService.cargarEjemplo();
    return "Datos de ejemplo cargados";
  }
}

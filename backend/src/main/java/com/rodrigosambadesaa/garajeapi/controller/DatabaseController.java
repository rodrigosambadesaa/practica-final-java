package com.rodrigosambadesaa.garajeapi.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rodrigosambadesaa.garajeapi.service.TrabajoService;
import com.rodrigosambadesaa.garajeapi.config.AdminTokenGuard;

@RestController
@RequestMapping("/api/db")
public class DatabaseController {

  private final TrabajoService trabajoService;
  private final AdminTokenGuard adminTokenGuard;

  public DatabaseController(TrabajoService trabajoService, AdminTokenGuard adminTokenGuard) {
    this.trabajoService = trabajoService;
    this.adminTokenGuard = adminTokenGuard;
  }

  @PostMapping("/reset")
  public String resetear(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
    adminTokenGuard.requireAuthorized(adminToken);
    trabajoService.resetearBbdd();
    return "BBDD reseteada";
  }

  @PostMapping("/seed")
  public String seed(@RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
    adminTokenGuard.requireAuthorized(adminToken);
    trabajoService.cargarEjemplo();
    return "Datos de ejemplo cargados";
  }
}

package com.rodrigosambadesaa.garajeapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.rodrigosambadesaa.garajeapi.repository.TrabajoRepository;
import com.rodrigosambadesaa.garajeapi.service.TrabajoService;

@SpringBootTest
@AutoConfigureMockMvc
class TrabajoApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TrabajoRepository repository;

  @Autowired
  private TrabajoService service;

  @BeforeEach
  void limpiarBaseDeDatos() {
    repository.deleteAllInBatch();
  }

  @Test
  void creaListaYValidaTrabajos() throws Exception {
    mockMvc.perform(post("/api/trabajos")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"tipo":"REVISION","descripcion":"  Revision pre-ITV  "}
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.descripcion").value("Revision pre-ITV"))
        .andExpect(jsonPath("$.precio").value(20));

    mockMvc.perform(get("/api/trabajos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].tipo").value("REVISION"));

    mockMvc.perform(post("/api/trabajos")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"tipo":"REVISION","descripcion":" "}
            """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void completaElCicloDeVidaDeUnaReparacion() throws Exception {
    String locationBody = mockMvc.perform(post("/api/trabajos")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"tipo":"REPARACION_MECANICA","descripcion":"Cambio de correa"}
            """))
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long id = repository.findByEliminadoFalseOrderByDescripcionAsc().getFirst().getId();
    assertThat(locationBody).contains("\"id\":" + id);

    mockMvc.perform(patch("/api/trabajos/{id}/horas", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"horas\":2.5}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.horas").value(2.5));

    mockMvc.perform(patch("/api/trabajos/{id}/material", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipoMaterial\":\"piezas\",\"coste\":100}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.costePiezas").value(100))
        .andExpect(jsonPath("$.precio").value(185));

    mockMvc.perform(patch("/api/trabajos/{id}/finalizar", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.finalizado").value(true));

    mockMvc.perform(patch("/api/trabajos/{id}/horas", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"horas\":1}"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(delete("/api/trabajos/{id}", id))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/trabajos/{id}", id))
        .andExpect(status().isNotFound());
  }

  @Test
  void rechazaMaterialIncompatible() throws Exception {
    mockMvc.perform(post("/api/trabajos")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"tipo":"REPARACION_MECANICA","descripcion":"Motor"}
            """))
        .andExpect(status().isCreated());

    Long id = repository.findByEliminadoFalseOrderByDescripcionAsc().getFirst().getId();

    mockMvc.perform(patch("/api/trabajos/{id}/material", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipoMaterial\":\"pintura\",\"coste\":10}"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(patch("/api/trabajos/{id}/material", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipoMaterial\":\"metal\",\"coste\":10}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void permiteSembrarCuandoSoloQuedanTrabajosBorrados() throws Exception {
    mockMvc.perform(post("/api/trabajos")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"tipo":"REVISION","descripcion":"Temporal"}
            """))
        .andExpect(status().isCreated());

    Long id = repository.findByEliminadoFalseOrderByDescripcionAsc().getFirst().getId();
    service.eliminar(id);
    service.cargarEjemplo();

    assertThat(repository.findByEliminadoFalseOrderByDescripcionAsc()).hasSize(3);
  }
}

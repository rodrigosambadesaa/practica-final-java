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

  @Test
  void aceptaCeroYRechazaNegativosYExcesoDePrecision() throws Exception {
    mockMvc.perform(post("/api/trabajos")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipo\":\"REPARACION_CHAPA_PINTURA\",\"descripcion\":\"Aleta\"}"))
        .andExpect(status().isCreated());
    Long id = repository.findByEliminadoFalseOrderByDescripcionAsc().getFirst().getId();

    mockMvc.perform(patch("/api/trabajos/{id}/horas", id)
        .contentType(MediaType.APPLICATION_JSON).content("{\"horas\":0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.horas").value(0));
    mockMvc.perform(patch("/api/trabajos/{id}/material", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipoMaterial\":\"chapa\",\"coste\":0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.costeChapa").value(0));
    mockMvc.perform(patch("/api/trabajos/{id}/horas", id)
        .contentType(MediaType.APPLICATION_JSON).content("{\"horas\":-1}"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(patch("/api/trabajos/{id}/material", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipoMaterial\":\"pintura\",\"coste\":-0.01}"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(patch("/api/trabajos/{id}/material", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipoMaterial\":\"pintura\",\"coste\":0.001}"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(patch("/api/trabajos/{id}/horas", id)
        .contentType(MediaType.APPLICATION_JSON).content("{\"horas\":10000000000}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void bloqueaDesbordamientoAcumuladoYOrdenaSinDistinguirMayusculas() throws Exception {
    var zeta = new com.rodrigosambadesaa.garajeapi.domain.Trabajo();
    zeta.setTipo(com.rodrigosambadesaa.garajeapi.domain.TrabajoTipo.REVISION);
    zeta.setDescripcion("zeta");
    zeta.setHoras(new java.math.BigDecimal("9999999999.99"));
    repository.saveAndFlush(zeta);

    var alfa = new com.rodrigosambadesaa.garajeapi.domain.Trabajo();
    alfa.setTipo(com.rodrigosambadesaa.garajeapi.domain.TrabajoTipo.REVISION);
    alfa.setDescripcion("Alfa");
    repository.saveAndFlush(alfa);

    mockMvc.perform(get("/api/trabajos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].descripcion").value("Alfa"))
        .andExpect(jsonPath("$[1].descripcion").value("zeta"));

    mockMvc.perform(patch("/api/trabajos/{id}/horas", zeta.getId())
        .contentType(MediaType.APPLICATION_JSON).content("{\"horas\":0.01}"))
        .andExpect(status().isBadRequest());
    assertThat(repository.findById(zeta.getId()).orElseThrow().getHoras())
        .isEqualByComparingTo("9999999999.99");
  }

  @Test
  void losTrabajosFinalizadosNoAceptanCambiosEnMaterial() throws Exception {
    mockMvc.perform(post("/api/trabajos")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipo\":\"REPARACION_MECANICA\",\"descripcion\":\"Correa\"}"))
        .andExpect(status().isCreated());
    Long id = repository.findByEliminadoFalseOrderByDescripcionAsc().getFirst().getId();
    mockMvc.perform(patch("/api/trabajos/{id}/finalizar", id))
        .andExpect(status().isOk());
    mockMvc.perform(patch("/api/trabajos/{id}/material", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipoMaterial\":\"piezas\",\"coste\":10}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void protegerOperacionesAdministrativasYConservarDatosCuandoFallaLaClave() throws Exception {
    mockMvc.perform(post("/api/trabajos")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"tipo\":\"REVISION\",\"descripcion\":\"Persistir\"}"))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/db/reset"))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/api/db/reset")
        .header("X-Admin-Token", "incorrecto"))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/api/db/seed"))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/api/db/seed")
        .header("X-Admin-Token", "incorrecto"))
        .andExpect(status().isForbidden());
    assertThat(repository.findByEliminadoFalseOrderByDescripcionAsc()).hasSize(1);

    mockMvc.perform(post("/api/db/reset")
        .header("X-Admin-Token", "integration-tests-only-abcdefghijklmnopqrstuvwxyz"))
        .andExpect(status().isOk());
    assertThat(repository.findByEliminadoFalseOrderByDescripcionAsc()).isEmpty();

    mockMvc.perform(post("/api/db/seed")
        .header("X-Admin-Token", "integration-tests-only-abcdefghijklmnopqrstuvwxyz"))
        .andExpect(status().isOk());
    assertThat(repository.findByEliminadoFalseOrderByDescripcionAsc()).hasSize(3);
  }

}

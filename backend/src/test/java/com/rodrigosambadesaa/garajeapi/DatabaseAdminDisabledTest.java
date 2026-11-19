package com.rodrigosambadesaa.garajeapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Una clave no configurada nunca debe habilitar las operaciones administrativas. */
@SpringBootTest(properties = "app.admin.token=")
@AutoConfigureMockMvc
class DatabaseAdminDisabledTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void losEndpointsAdministrativosSeCierranInclusoConCabeceraCuandoFaltaLaClave() throws Exception {
    mockMvc.perform(post("/api/db/reset")
        .header("X-Admin-Token", "integration-tests-only-abcdefghijklmnopqrstuvwxyz"))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/api/db/seed")
        .header("X-Admin-Token", "integration-tests-only-abcdefghijklmnopqrstuvwxyz"))
        .andExpect(status().isForbidden());
  }
}

package com.rodrigosambadesaa.garajeapi.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Verifica la clave administrativa solo en el servidor, sin registrarla en logs.
 * Sin clave configurada, los endpoints administrativos permanecen deshabilitados.
 */
@Component
public final class AdminTokenGuard {

  private final byte[] expectedToken;

  public AdminTokenGuard(@Value("${app.admin.token:}") String configuredToken) {
    if (!configuredToken.isEmpty() && configuredToken.length() < 32) {
      throw new IllegalArgumentException("APP_ADMIN_TOKEN debe tener al menos 32 caracteres");
    }
    this.expectedToken = configuredToken.getBytes(StandardCharsets.UTF_8);
  }

  public void requireAuthorized(String suppliedToken) {
    if (expectedToken.length == 0 || suppliedToken == null
        || !MessageDigest.isEqual(expectedToken, suppliedToken.getBytes(StandardCharsets.UTF_8))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operacion administrativa no autorizada");
    }
  }
}

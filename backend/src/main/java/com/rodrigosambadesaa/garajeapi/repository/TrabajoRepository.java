package com.rodrigosambadesaa.garajeapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rodrigosambadesaa.garajeapi.domain.Trabajo;

public interface TrabajoRepository extends JpaRepository<Trabajo, Long> {
  List<Trabajo> findByEliminadoFalseOrderByDescripcionAsc();

  Optional<Trabajo> findByIdAndEliminadoFalse(Long id);

  boolean existsByEliminadoFalse();
}

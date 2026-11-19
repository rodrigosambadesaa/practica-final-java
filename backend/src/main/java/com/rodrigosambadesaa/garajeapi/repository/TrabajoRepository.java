package com.rodrigosambadesaa.garajeapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.rodrigosambadesaa.garajeapi.domain.Trabajo;

public interface TrabajoRepository extends JpaRepository<Trabajo, Long> {
  @Query("select t from Trabajo t where t.eliminado = false order by lower(t.descripcion), t.id")
  List<Trabajo> findByEliminadoFalseOrderByDescripcionAsc();

  Optional<Trabajo> findByIdAndEliminadoFalse(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from Trabajo t where t.id = :id and t.eliminado = false")
  Optional<Trabajo> findActiveByIdForUpdate(@Param("id") Long id);

  boolean existsByEliminadoFalse();
}

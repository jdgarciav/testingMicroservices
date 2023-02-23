package com.minsait.testingMicroservices.repositories;

import com.minsait.testingMicroservices.models.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta,Long> {
    Optional<Cuenta> findByPersona(String persona);

    @Query("select c from Cuenta c where c.persona=?1")
    Optional<Cuenta> buscarPorPresona(String persona);
}

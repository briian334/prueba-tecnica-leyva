package com.leyva.pruebatecnica.persistencia;

import com.leyva.pruebatecnica.dominio.Poliza;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    @Query("""
            SELECT DISTINCT poliza
            FROM Poliza poliza
            LEFT JOIN FETCH poliza.movimientos
            WHERE poliza.id = :id
            """)
    Optional<Poliza> buscarDetallePorId(@Param("id") Long id);
}

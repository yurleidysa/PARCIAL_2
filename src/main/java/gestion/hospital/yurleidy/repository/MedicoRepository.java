package gestion.hospital.yurleidy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import gestion.hospital.yurleidy.model.Medico;

public interface MedicoRepository extends JpaRepository<Medico, Integer> {

    Optional<Medico> findByIdUsuario(Integer idUsuario);

    boolean existsByIdUsuario(Integer idUsuario);
}

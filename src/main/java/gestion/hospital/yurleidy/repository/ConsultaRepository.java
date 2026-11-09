package gestion.hospital.yurleidy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gestion.hospital.yurleidy.model.Consulta;

public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {

    List<Consulta> findByMedicoIdMedico(Integer idMedico);

    List<Consulta> findByNombrePacienteIgnoreCase(String nombrePaciente);

    boolean existsByMedicoIdMedico(Integer idMedico);
}

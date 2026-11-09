package gestion.hospital.yurleidy.model;

import java.time.LocalTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "consulta")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConsulta;

    @NotBlank(message = "El nombre del paciente es obligatorio")
    @Size(max = 40, message = "El nombre del paciente no puede superar 40 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü0-9 ]+$", message = "El nombre del paciente solo admite letras, números y espacios")
    private String nombrePaciente;

    @NotBlank(message = "El motivo de la consulta es obligatorio")
    @Size(max = 100, message = "El motivo no puede superar 100 caracteres")
    private String motivo;

    @NotNull(message = "El número de consultorio es obligatorio")
    @Positive(message = "El número de consultorio debe ser mayor que 0")
    @Max(value = 9999, message = "El número de consultorio no puede superar 9999")
    private Integer numeroConsultorio;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de finalización es obligatoria")
    private LocalTime horaFin;

    @NotNull(message = "El médico es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_medico")
    private Medico medico;

    public Integer getIdConsulta() { return idConsulta; }
    public void setIdConsulta(Integer idConsulta) { this.idConsulta = idConsulta; }

    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Integer getNumeroConsultorio() { return numeroConsultorio; }
    public void setNumeroConsultorio(Integer numeroConsultorio) { this.numeroConsultorio = numeroConsultorio; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
}

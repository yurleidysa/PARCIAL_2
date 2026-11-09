async function cargarConsultas() {
    const lista = await peticion('/api/consultas');
    llenarTabla(campo('tablaConsultas'), lista, 4, 'No tiene consultas asignadas', c => [
        c.nombrePaciente,
        c.motivo,
        c.numeroConsultorio,
        hora(c.horaInicio) + ' - ' + hora(c.horaFin)
    ]);
}

cargarConsultas().catch(e => mostrarMensaje(e.message, 'danger'));

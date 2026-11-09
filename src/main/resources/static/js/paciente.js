async function cargarConsultas() {
    const lista = await peticion('/api/consultas');
    llenarTabla(campo('tablaConsultas'), lista, 5, 'No tiene citas asignadas', c => [
        c.motivo,
        c.numeroConsultorio,
        hora(c.horaInicio) + ' - ' + hora(c.horaFin),
        c.medico ? c.medico.nombre : ''
    ], c => [boton('Horario', 'btn-outline-primary', () => prepararHorario(c))]);
}

function prepararHorario(c) {
    campo('idConsulta').value = c.idConsulta;
    campo('horaInicio').value = hora(c.horaInicio);
    campo('horaFin').value = hora(c.horaFin);
    campo('tituloHorario').textContent = 'Actualizar horario - ' + c.motivo;
    campo('cardHorario').classList.remove('d-none');
    window.scrollTo(0, 0);
}

function cerrarHorario() {
    campo('formHorario').reset();
    campo('cardHorario').classList.add('d-none');
}

campo('formHorario').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    if (!horarioValido(campo('horaInicio').value, campo('horaFin').value)) return;
    const datos = {
        horaInicio: campo('horaInicio').value,
        horaFin: campo('horaFin').value
    };
    try {
        await peticion(`/api/consultas/${campo('idConsulta').value}/horario`, 'PATCH', datos);
        mostrarMensaje('Horario actualizado');
        cerrarHorario();
        cargarConsultas();
    } catch (e) { mostrarMensaje(e.message, 'danger'); }
});

campo('btnCancelar').addEventListener('click', cerrarHorario);

cargarConsultas().catch(e => mostrarMensaje(e.message, 'danger'));

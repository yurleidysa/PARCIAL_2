async function cargarMedicos() {
    const medicos = await peticion('/api/medicos');
    if (!medicos) return;

    const select = campo('medico');
    select.innerHTML = '<option value="">Seleccione...</option>';
    medicos.forEach(m => {
        const opcion = document.createElement('option');
        opcion.value = m.idMedico;
        opcion.textContent = m.nombre + ' ' + m.apellido;
        select.appendChild(opcion);
    });

    llenarTabla(campo('tablaMedicos'), medicos, 3, 'No hay médicos registrados', m => [
        m.nombre + ' ' + m.apellido,
        m.correo
    ], m => [
        boton('Editar', 'btn-outline-secondary', () => editarMedico(m)),
        boton('Eliminar', 'btn-outline-danger', () => eliminarMedico(m))
    ]);
}

async function cargarPacientes() {
    const pacientes = await peticion('/api/consultas/pacientes');
    if (!pacientes) return;
    const lista = campo('listaPacientes');
    lista.innerHTML = '';
    pacientes.forEach(nombre => {
        const opcion = document.createElement('option');
        opcion.value = nombre;
        lista.appendChild(opcion);
    });
}

async function cargarConsultas() {
    const lista = await peticion('/api/consultas');
    llenarTabla(campo('tablaConsultas'), lista, 5, 'No hay consultas registradas', c => [
        c.nombrePaciente,
        c.motivo,
        c.numeroConsultorio,
        hora(c.horaInicio) + ' - ' + hora(c.horaFin),
        c.medico ? c.medico.nombre : ''
    ]);
}

campo('formConsulta').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    if (!horarioValido(campo('horaInicio').value, campo('horaFin').value)) return;
    const datos = {
        nombrePaciente: campo('nombrePaciente').value,
        motivo: campo('motivo').value,
        numeroConsultorio: Number(campo('numeroConsultorio').value),
        horaInicio: campo('horaInicio').value,
        horaFin: campo('horaFin').value,
        medico: { idMedico: Number(campo('medico').value) }
    };
    try {
        await peticion('/api/consultas', 'POST', datos);
        mostrarMensaje('Consultorio registrado');
        campo('formConsulta').reset();
        cargarConsultas();
    } catch (e) { mostrarMensaje(e.message, 'danger'); }
});

function editarMedico(m) {
    campo('idMedico').value = m.idMedico;
    campo('nombre').value = m.nombre;
    campo('apellido').value = m.apellido;
    campo('correo').value = m.correo;
    campo('contrasena').value = '';
    campo('contrasena').required = false;
    campo('ayudaContrasena').classList.remove('d-none');
    campo('tituloMedico').textContent = 'Editar médico - ' + m.nombre + ' ' + m.apellido;
    campo('btnCancelar').classList.remove('d-none');
    campo('formMedico').scrollIntoView();
}

function limpiarMedico() {
    campo('formMedico').reset();
    campo('idMedico').value = '';
    campo('contrasena').required = true;
    campo('ayudaContrasena').classList.add('d-none');
    campo('tituloMedico').textContent = 'Registrar médico';
    campo('btnCancelar').classList.add('d-none');
}

async function eliminarMedico(m) {
    if (!confirm(`¿Eliminar al médico ${m.nombre} ${m.apellido}?`)) return;
    try {
        await peticion(`/api/medicos/${m.idMedico}`, 'DELETE');
        mostrarMensaje('Médico eliminado');
        cargarMedicos();
    } catch (e) { mostrarMensaje(e.message, 'danger'); }
}

campo('formMedico').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const id = campo('idMedico').value;
    const datos = {
        nombre: campo('nombre').value,
        apellido: campo('apellido').value,
        correo: campo('correo').value,
        contrasena: campo('contrasena').value
    };
    try {
        if (id) {
            await peticion(`/api/medicos/${id}`, 'PUT', datos);
            mostrarMensaje('Médico actualizado');
        } else {
            await peticion('/api/medicos', 'POST', datos);
            mostrarMensaje('Médico registrado');
        }
        limpiarMedico();
        cargarMedicos();
        cargarConsultas();
    } catch (e) { mostrarMensaje(e.message, 'danger'); }
});

campo('btnCancelar').addEventListener('click', limpiarMedico);

cargarPacientes().catch(e => mostrarMensaje(e.message, 'danger'));
cargarMedicos().catch(e => mostrarMensaje(e.message, 'danger'));
cargarConsultas().catch(e => mostrarMensaje(e.message, 'danger'));

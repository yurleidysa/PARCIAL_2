const mensaje = document.getElementById('mensaje');

function campo(id) {
    return document.getElementById(id);
}

async function peticion(url, metodo = 'GET', datos = null) {
    const opciones = { method: metodo, headers: {} };
    if (datos) {
        opciones.headers['Content-Type'] = 'application/json';
        opciones.body = JSON.stringify(datos);
    }
    const resp = await fetch(url, opciones);

    if (resp.status === 401) {
        window.location.href = '/login';
        return null;
    }
    if (resp.status === 403) {
        window.location.href = '/acceso-denegado';
        return null;
    }
    if (!resp.ok) {
        const error = await resp.json().catch(() => ({}));
        throw new Error(error.message || error.error || 'Error ' + resp.status);
    }
    const texto = await resp.text();
    return texto ? JSON.parse(texto) : null;
}

function mostrarMensaje(texto, tipo = 'success') {
    mensaje.className = 'alert alert-' + tipo;
    mensaje.textContent = texto;
    setTimeout(() => mensaje.classList.add('d-none'), 4000);
}

function horarioValido(inicio, fin) {
    if (fin <= inicio) {
        mostrarMensaje('La hora de finalización debe ser posterior a la hora de inicio', 'danger');
        return false;
    }
    return true;
}

function hora(h) {
    return h ? h.substring(0, 5) : '';
}

function boton(texto, clase, accion) {
    const b = document.createElement('button');
    b.type = 'button';
    b.className = 'btn btn-sm ' + clase;
    b.textContent = texto;
    b.addEventListener('click', accion);
    return b;
}

function llenarTabla(tabla, lista, totalColumnas, vacio, columnas, acciones = null) {
    tabla.innerHTML = '';
    if (!lista || lista.length === 0) {
        tabla.innerHTML = `<tr><td colspan="${totalColumnas}" class="text-center text-muted">${vacio}</td></tr>`;
        return;
    }
    lista.forEach(item => {
        const fila = document.createElement('tr');
        columnas(item).forEach(valor => {
            const td = document.createElement('td');
            td.textContent = valor;
            fila.appendChild(td);
        });
        if (acciones) {
            const celda = document.createElement('td');
            celda.className = 'd-flex gap-1';
            acciones(item).forEach(b => celda.appendChild(b));
            fila.appendChild(celda);
        }
        tabla.appendChild(fila);
    });
}

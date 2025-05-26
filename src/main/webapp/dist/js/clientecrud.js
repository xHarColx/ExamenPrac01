document.addEventListener('DOMContentLoaded', function () {

    // Al inicio de tu JS
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap 5 no está cargado correctamente');
    }
    $(function () {
        let token = getCookie("token");
        if (!token) {
            console.error("Token no encontrado");
            return;
        }

        // Inicializar DataTable
        const table = $('#example').DataTable({
            ajax: {
                url: 'http://localhost:8080/ExamenPrac01/clientes?token=' + encodeURIComponent(token),
                dataSrc: ''
            },
            columns: [
                {data: 'codiClie'},
                {data: 'ndniClie'},
                {data: 'appaClie'},
                {data: 'apmaClie'},
                {data: 'nombClie'},
                {data: 'edad'},
                {data: 'logiClie'},
                {
                    data: 'passClie', // La data original
                    render: function (data, type, row) {
                        if (type === 'display' && data && data.length > 15) { // Truncar si es para mostrar y es muy largo
                            return '<span title="' + data + '">' + data.substr(0, 15) + '...</span>';
                        }
                        return data; // Devolver data original para otros usos (ordenar, buscar)
                    }
                },
                {
                    data: null,
                    render: function (data, type, row) {
                        return `<button class="btn btn-info btn-editar" 
                                    data-id="${row.codiClie}"
                                    data-appa="${row.appaClie}"
                                    data-apma="${row.apmaClie}"
                                    data-nomb="${row.nombClie}">
                                    Editar</button>`;
                    }
                },
                {
                    data: null,
                    render: function (data, type, row) {
                        return `<button class="btn btn-danger btn-eliminar" 
                                    data-id="${row.codiClie}">
                                    Eliminar</button>`;
                    }
                }
            ]
        });

        // Función para editar - Abre modal con datos
        $(document).on('click', '.btn-editar', function () {
            const codiClie = $(this).data('id');
            const appaClie = $(this).data('appa');
            const apmaClie = $(this).data('apma');
            const nombClie = $(this).data('nomb');

            // Rellenar el modal con los datos
            $('#editCodiClie').val(codiClie);
            $('#editAppaClie').val(appaClie);
            $('#editApmaClie').val(apmaClie);
            $('#editNombClie').val(nombClie);

            // Mostrar el modal
            const modalEditar = new bootstrap.Modal(document.getElementById('modalEditar'));
            modalEditar.show();
        });

        // Función para guardar cambios
        $('#btnGuardarCambios').click(function () {
            const codiClie = $('#editCodiClie').val();
            const appaClie = $('#editAppaClie').val();
            const apmaClie = $('#editApmaClie').val();
            const nombClie = $('#editNombClie').val();

            // Aquí la petición AJAX para actualizar
            $.ajax({
                url: 'http://localhost:8080/BCrypt/clientes?token=' + encodeURIComponent(token),
                method: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify({
                    codiClie: codiClie,
                    appaClie: appaClie,
                    apmaClie: apmaClie,
                    nombClie: nombClie
                }),
                success: function (response) {
                    // Cerrar modal y refrescar tabla
                    $('#modalEditar').modal('hide');
                    table.ajax.reload();
                    alert('Cliente actualizado correctamente');
                },
                error: function (xhr) {
                    alert('Error al actualizar cliente: ' + xhr.responseText);
                }
            });
        });

        // Función para eliminar - Abre modal de confirmación
        $(document).on('click', '.btn-eliminar', function () {
            const codiClie = $(this).data('id');
            $('#deleteCodiClie').val(codiClie);

            const modalEliminar = new bootstrap.Modal(document.getElementById('modalEliminar'));
            modalEliminar.show();
        });

        // Función para preparar eliminación


        // Evento para confirmar eliminación
        $("#btnConfirmarEliminar").click(function () {
            let codiClie = $("#deleteCodiClie").val();
            let token = getCookie("token");

            if (!codiClie) {
                alert("No se ha seleccionado ningún cliente para eliminar");
                return;
            }
            $.ajax({
                url: 'http://localhost:8080/BCrypt/clientes?codiClie=' + encodeURIComponent(codiClie) + '&token=' + encodeURIComponent(token),
                method: 'DELETE',
                contentType: 'application/x-www-form-urlencoded',
                success: function (response) {
                    if (response.success) {
                        alert(response.message);
                        $("#modalEliminar").modal('hide');
                        table.ajax.reload(null, false);
                    } else {
                        alert("Error: " + response.error);
                    }
                },
                error: function (xhr) {
                    let errorMsg = "Error en la solicitud";
                    try {
                        const response = JSON.parse(xhr.responseText);
                        errorMsg = response.error || errorMsg;
                    } catch (e) {
                    }
                    alert(errorMsg);
                }
            });

        });


    });
});

function getCookie(nombre) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${nombre}=`);
    if (parts.length === 2)
        return parts.pop().split(';').shift();
}
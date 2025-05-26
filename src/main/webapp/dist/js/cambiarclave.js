$(document).ready(function () {
    // Cargar información del usuario
    $.get("http://localhost:8080/ExamenPrac01/session-info", function (data) {
        if (data.result === "ok") {
            $("#txtuser").val(data.logiClie);
            sessionStorage.setItem("codiClie", data.codiClie);
        } else {
            alert("Debes iniciar sesión.");
            window.location.href = "http://localhost:8080/ExamenPrac01/index.html";
        }
    });

    let token = getCookie("token");
    if (!token) {
        console.error("Token no encontrado");
        return;
    }

    console.log(token);

    $("#cambiarClaveForm").on("submit", function (e) {
        e.preventDefault();

        const claveActual = $("#claveActual").val();
        const nuevaClave = $("#nuevaClave").val();
        const confirmarClave = $("#confirmarClave").val();
        const codiClie = sessionStorage.getItem("codiClie");

        if (!codiClie) {
            alert("No se ha identificado al usuario");
            return;
        }

        if (nuevaClave !== confirmarClave) {
            alert("Las nuevas claves no coinciden");
            return;
        }

        // Cifrar contraseñas
        const claveActualCifrada = cifrar(claveActual);
        const nuevaClaveCifrada = cifrar(nuevaClave);
        const confirmarClaveCifrada = cifrar(confirmarClave);

        const datos = {
            codiClie: codiClie,
            claveActual: claveActualCifrada,
            nuevaClave: nuevaClaveCifrada,
            confirmarClave: confirmarClaveCifrada
        };

        // Mostrar loading
        const btn = $(this).find("button[type='submit']");
        btn.prop("disabled", true).text("Procesando...");

        $.ajax({
            url: 'http://localhost:8080/ExamenPrac01/cambiarclaveservlet?token=' + encodeURIComponent(token),
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(datos),
            success: function (resp) {
                if (resp.status === true) {
                    alert("Contraseña actualizada correctamente");
                    $("#cambiarClaveForm")[0].reset();
                    window.location.href = "http://localhost:8080/ExamenPrac01/index.html";
                } else {
                    alert(resp.message || "Error al cambiar contraseña");
                }
            },
            error: function (xhr) {
                alert("Error en el servidor: " + (xhr.responseJSON?.message || "Intente nuevamente"));
            },
            complete: function () {
                btn.prop("disabled", false).text("Actualizar Contraseña");
            }
        });
    });
});

function cifrar(texto) {
    const keyUtf8 = CryptoJS.enc.Utf8.parse("1234567890123456");
    return CryptoJS.AES.encrypt(texto, keyUtf8, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
    }).toString();
}

function getCookie(nombre) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${nombre}=`);
    if (parts.length === 2)
        return parts.pop().split(';').shift();
}
const btnLogin = document.getElementById("btnLogin");
const clave = "1234567890123456"; // Asegúrate de que sea igual a la usada en el servidor

btnLogin.addEventListener("click", async (event) => {
    event.preventDefault();

    const log = document.getElementById("log").value.trim();
    const pass = document.getElementById("pass").value.trim();

    if (!log || !pass) {
        alert("Por favor, complete todos los campos.");
        return;
    }

    // Asegúrate de que cifrar(pass, clave) esté definida e implemente AES
    const passCifrada = cifrar(pass, clave);

    const obj = {
        log: log,
        pass: passCifrada
    };
    console.log(obj);
    try {
        const response = await fetch("login-normal", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(obj)
        });

        const result = await response.json();
        console.log(result);

        if (result.result === "ok") {
            setCookie("token", result.token, 7); // función definida aparte
            alert("Inicio de sesión exitoso.");
            window.location.href = "dist/pages/index.html";
        } else {
            alert(result.message || "Credenciales incorrectas.");
        }
    } catch (error) {
        console.error("Error en la solicitud:", error);
        alert("Ocurrió un error al intentar iniciar sesión.");
    }
});

function setCookie(nombre, valor, dias) {
    const fecha = new Date();
    fecha.setTime(fecha.getTime() + (dias * 24 * 60 * 60 * 1000));
    const expira = "expires=" + fecha.toUTCString();
    document.cookie = nombre + "=" + valor + ";" + expira + ";path=/";
}

// Login con Google
function handleCredentialResponse(response) {
    const id_token = response.credential;
    console.log(id_token);
    fetch('logueo-google', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({id_token})
    })
            .then(res => res.json())
            .then(data => {
                if (data.resultado === 'ok') {
                    setCookie("token", data.token, 7);
                    window.location.href = "dist/pages/index.html";
                } else {
                    alert('Error en login con Google');
                }
            })
            .catch(err => {
                console.error('Error al procesar el login:', err);
                alert('No se pudo procesar el inicio de sesión con Google.');
            });
}



function cifrar(message, key) {
    let keyUtf8 = CryptoJS.enc.Utf8.parse(key);
    let encrypted = CryptoJS.AES.encrypt(message, keyUtf8, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
    });
    return encrypted.toString(); // Base64
}

function descifrar(ciphertext, key) {
    try {
        let keyUtf8 = CryptoJS.enc.Utf8.parse(key);
        let decrypted = CryptoJS.AES.decrypt(ciphertext, keyUtf8, {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        });
        return decrypted.toString(CryptoJS.enc.Utf8); // Retorna string plano
    } catch (e) {
        return "[Mensaje no descifrado]";
    }
}

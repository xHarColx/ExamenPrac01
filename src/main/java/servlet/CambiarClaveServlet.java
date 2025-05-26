package servlet;

import dao.ClienteJpaController;
import dto.Cliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import util.AESUtil;
import util.SHA512Util;

@WebServlet(name = "CambiarClaveServlet", urlPatterns = {"/cambiarclaveservlet"})
public class CambiarClaveServlet extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_ExamenPrac01_war_1.0-SNAPSHOTPU");
    }
    private final ClienteJpaController usuarioDAO = new ClienteJpaController();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonResponse = new JSONObject();

        try {
            String token = request.getParameter("token");
            boolean tokenValido = util.JwtUtil.validarToken(token);
            EntityManager em = emf.createEntityManager();

            if (!tokenValido) {
                enviarError(response, "Token inválido", HttpServletResponse.SC_UNAUTHORIZED);
                em.close();
                return;
            }
            // Leer JSON
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject jsonRequest = new JSONObject(sb.toString());

            // Validar campos requeridos
            if (!jsonRequest.has("codiClie") || jsonRequest.getString("codiClie").isEmpty()) {
                throw new Exception("Código de usuario requerido");
            }
            if (!jsonRequest.has("claveActual") || jsonRequest.getString("claveActual").isEmpty()) {
                throw new Exception("Clave actual requerida");
            }
            if (!jsonRequest.has("nuevaClave") || jsonRequest.getString("nuevaClave").isEmpty()) {
                throw new Exception("Nueva clave requerida");
            }

            // Descifrar contraseñas
            int codiClie = Integer.parseInt(jsonRequest.getString("codiClie"));
            String claveActual = AESUtil.descifrar(jsonRequest.getString("claveActual"), "1234567890123456");
            String nuevaClave = AESUtil.descifrar(jsonRequest.getString("nuevaClave"), "1234567890123456");
            String confirmarClave = AESUtil.descifrar(jsonRequest.getString("confirmarClave"), "1234567890123456");

            // Validar coincidencia
            if (!nuevaClave.equals(confirmarClave)) {
                throw new Exception("Las nuevas contraseñas no coinciden");
            }

            // Buscar usuario
            Cliente usuario = usuarioDAO.findCliente(codiClie);
            if (usuario == null) {
                throw new Exception("Usuario no encontrado");
            }

            // Verificar clave actual
            String claveActualHash = SHA512Util.hash(claveActual);
            if (!claveActualHash.equals(usuario.getPassClie())) {
                throw new Exception("Clave actual incorrecta");
            }

            // Actualizar contraseña
            String nuevaClaveHash = SHA512Util.hash(nuevaClave);
            usuario.setPassClie(nuevaClaveHash);
            usuarioDAO.edit(usuario);

            // Respuesta exitosa
            jsonResponse.put("status", true);
            jsonResponse.put("message", "Contraseña actualizada exitosamente");
            response.getWriter().write(jsonResponse.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("status", false);
            jsonResponse.put("message", e.getMessage());
            response.getWriter().write(jsonResponse.toString());
        }
    }

    private void enviarError(HttpServletResponse response, String mensaje, int codigo) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(codigo);
        try ( PrintWriter out = response.getWriter()) {
            out.print(new JSONObject().put("error", mensaje).toString());
        }
    }
}

package servlet;

import dao.ClienteJpaController;
import dto.Cliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import util.AESUtil;
import util.JwtUtil;
import util.SHA512Util;

/**
 *
 * @author harol
 */
@WebServlet(name = "CrearClienteServlet", urlPatterns = {"/crear-cliente"})
public class CrearClienteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        try ( PrintWriter out = response.getWriter()) {
            // Leer el cuerpo de la solicitud como JSON
            StringBuilder sb = new StringBuilder();
            String line;
            try ( BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JSONObject jsonRequest = new JSONObject(sb.toString());
            JSONObject jsonResponse = new JSONObject();

            try {

                // Obtener datos del request
                String ndniClie = jsonRequest.getString("ndniClie");
                String appaClie = jsonRequest.getString("appaClie");
                String apmaClie = jsonRequest.getString("apmaClie");
                String nombClie = jsonRequest.getString("nombClie");
                String fechaNaciStr = jsonRequest.getString("fechaNaci");
                String logiClie = jsonRequest.getString("logiClie");
                String passClie = jsonRequest.getString("passsClie");
                String clave = "1234567890123456";
                String contraDescifrada = AESUtil.descifrar(passClie, clave);
                System.out.println("Contra descifrada: " + contraDescifrada);
                System.out.println("---------------------------");
                String contraSHA512 = SHA512Util.hash(contraDescifrada);
                System.out.println("Contra usando SHA512: " + contraSHA512);
                // Convertir fecha
                SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
                Date fechaNaci = formatoFecha.parse(fechaNaciStr);

                // Aquí deberías crear el controlador JPA para Cliente y la entidad Cliente
                // Ejemplo (necesitas implementar estas clases):
                ClienteJpaController clienteDAO = new ClienteJpaController();
                Cliente nuevoCliente = new Cliente(
                        ndniClie,
                        appaClie,
                        apmaClie,
                        nombClie,
                        fechaNaci,
                        logiClie,
                        contraSHA512
                );

                clienteDAO.create(nuevoCliente);
                String token = JwtUtil.generarToken(logiClie);
                jsonResponse.put("resultado", "ok");
                jsonResponse.put("token", token);
                jsonResponse.put("mensaje", "Cliente registrado correctamente");
                out.print(jsonResponse.toString());

            } catch (Exception e) {
                jsonResponse.put("resultado", "error");
                jsonResponse.put("mensaje", "Error al registrar cliente: " + e.getMessage());
                out.print(jsonResponse.toString());
                e.printStackTrace();
            }
        }
    }

}

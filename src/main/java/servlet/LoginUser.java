package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.ClienteJpaController;
import dto.Cliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.AESUtil;
import util.JwtUtil;
import util.SHA512Util;

@WebServlet(name = "loginservlet", urlPatterns = {"/login-normal"})
public class LoginUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            BufferedReader reader = request.getReader();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            String log = json.get("log").getAsString();
            String pass = json.get("pass").getAsString();
            String clave = "1234567890123456";
            ClienteJpaController ujc = new ClienteJpaController();
            String contraDescifrada = AESUtil.descifrar(pass, clave);
            System.out.println("Contra descifrada: " + contraDescifrada);
            System.out.println("---------------------------");
            String contraSHA512 = SHA512Util.hash(contraDescifrada);
            System.out.println("Contra usando SHA512: " + contraSHA512);
            Cliente usuario = ujc.validarUsuario(new Cliente(log, contraSHA512));

            if (usuario != null) {
                HttpSession session = request.getSession();

                session.setAttribute("codiClie", usuario.getCodiClie());
                session.setAttribute("logiClie", usuario.getLogiClie());
                session.setAttribute("passClie", usuario.getPassClie());
                session.setAttribute("usuario", log);

                String token = JwtUtil.generarToken(log);
                jsonObject.addProperty("result", "ok");
                jsonObject.addProperty("token", token);
                jsonObject.addProperty("codiClie", usuario.getCodiClie());
                jsonObject.addProperty("logiClie", usuario.getLogiClie());
                jsonObject.addProperty("passClie", usuario.getPassClie());

            } else {
                jsonObject.addProperty("result", "not");
                jsonObject.addProperty("message", "credenciales inválidas");
            }
            out.print(gson.toJson(jsonObject));
        } catch (Exception ex) {
            Logger.getLogger(LoginUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}

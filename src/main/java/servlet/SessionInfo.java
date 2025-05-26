/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "SessionInfo", urlPatterns = {"/session-info"})
public class SessionInfo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            HttpSession session = request.getSession(false);
            JsonObject json = new JsonObject();
            
            if (session != null && session.getAttribute("logiClie") != null) {
                json.addProperty("logiClie", (String) session.getAttribute("logiClie"));
                json.addProperty("codiClie", session.getAttribute("codiClie").toString());
                json.addProperty("passClie", session.getAttribute("passClie").toString());
                json.addProperty("result", "ok");
            } else {
                json.addProperty("result", "not");
                json.addProperty("message", "Usuario no autenticado");
            }
            out.print(json);
        }
    }
}

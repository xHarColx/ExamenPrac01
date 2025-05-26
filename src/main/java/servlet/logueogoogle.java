
package servlet;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Collections;
import util.JwtUtil;

@WebServlet(name = "LoginGoogleColanServlet", urlPatterns = {"/logueo-google"})
public class logueogoogle extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Leer JSON con id_token
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String jsonString = sb.toString();

        String idTokenString = "";
        try {
            JsonObject json = new Gson().fromJson(jsonString, JsonObject.class);
            idTokenString = json.get("id_token").getAsString();
        } catch(Exception e){
            out.println("{\"resultado\":\"error\"}");
            return;
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList("1098452111880-hlhrnqrvveiugklvdfhv4bfuqlouutjg.apps.googleusercontent.com"))
                .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                // Aquí puedes validar o registrar el usuario en tu BD

                // Crear JWT propio para tu app
                String token = JwtUtil.generarToken(email);

                // Crear sesión y devolver JSON con token
                HttpSession sesion = request.getSession();
                sesion.setAttribute("usuario", email);

                out.println("{\"resultado\":\"ok\",\"token\":\"" + token + "\"}");

            } else {
                out.println("{\"resultado\":\"error\"}");
            }
        } catch(Exception e){
            e.printStackTrace();
            out.println("{\"resultado\":\"error\"}");
        }
    }
}

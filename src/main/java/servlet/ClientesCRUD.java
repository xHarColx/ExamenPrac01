package servlet;

import dao.ClienteJpaController;
import dto.Cliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "ClientesCRUD", urlPatterns = {"/clientes"})
public class ClientesCRUD extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_ExamenPrac01_war_1.0-SNAPSHOTPU");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        boolean tokenValido = util.JwtUtil.validarToken(token);

        if (!tokenValido) {
            enviarError(response, "Token inválido", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        EntityManager em = emf.createEntityManager();
        try {
            List<Cliente> clientes = em.createNamedQuery("Cliente.findAll", Cliente.class).getResultList();

            JSONArray jsonArray = new JSONArray();
            ClienteJpaController masDAO = new ClienteJpaController();

            for (Cliente c : clientes) {
                JSONObject obj = new JSONObject();
                obj.put("codiClie", c.getCodiClie());
                obj.put("ndniClie", c.getNdniClie());
                obj.put("appaClie", c.getAppaClie());
                obj.put("apmaClie", c.getApmaClie());
                obj.put("nombClie", c.getNombClie());
                int edad = masDAO.calcularEdad(c.getFechNaciClie());
                obj.put("edad", edad);
                obj.put("logiClie", c.getLogiClie());
                obj.put("passClie", c.getPassClie());
                jsonArray.put(obj);
            }

            response.setContentType("application/json;charset=UTF-8");
            try ( PrintWriter out = response.getWriter()) {
                out.print(jsonArray.toString());
            }
        } finally {
            em.close();
        }
    }

    // Métodos auxiliares
    private void enviarRespuesta(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            out.print(new JSONObject().put("resultado", mensaje).toString());
        }
    }

    private void enviarError(HttpServletResponse response, String mensaje, int codigo) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(codigo);
        try ( PrintWriter out = response.getWriter()) {
            out.print(new JSONObject().put("error", mensaje).toString());
        }
    }

    @Override
    public void destroy() {
        if (emf != null) {
            emf.close();
        }
    }
}

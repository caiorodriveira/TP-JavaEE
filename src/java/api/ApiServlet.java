/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package api;

import jakarta.servlet.ServletConfig;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import model.Aviso;
import model.Usuario;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Fatec
 */
@WebServlet(name = "ApiServlet", urlPatterns = {"/api/*"})
public class ApiServlet extends HttpServlet {

    private JSONObject getJSONBody(BufferedReader reader) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return new JSONObject(buffer.toString());
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject file = new JSONObject();
        try {
            if (request.getRequestURI().endsWith("/api/session")) {
                processSession(file, request, response);
            } else if (request.getRequestURI().endsWith("/api/usuarios")) {
                processUsuarios(file, request, response);
            } else if (request.getRequestURI().endsWith("api/avisos")) {
                processAvisos(file, request, response);
            } else {
                response.sendError(400, "URL invalida!");
                file.put("error", "URL invalida");
            }
        } catch (Exception ex) {
            response.sendError(500, "Erro interno: " + ex.getLocalizedMessage());
            file.put("error", "Erro interno: " + ex.getLocalizedMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);

    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void processSession(JSONObject file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getMethod().toLowerCase().equals("put")) {
            JSONObject body = getJSONBody(request.getReader());
            String email = body.getString("email");
            String senha = body.getString("senha");
            Usuario u = Usuario.getUsuario(email, senha);
            if (u == null) {
                response.sendError(403, "Email ou Senha invalidos!");
            } else {
                request.getSession().setAttribute("usuario", u);
                file.put("id", u.getID());
                file.put("nome", u.getNome());
                file.put("email", u.getEmail());
                file.put("role", u.getRole());
                file.put("senha", u.getSenha());

                file.put("message", "Logado com sucesso!");
                response.getWriter().print(file.toString());
            }
        } else if (request.getMethod().toLowerCase().equals("delete")) {
            request.getSession().removeAttribute("usuario");
            file.put("message", "Deslogado com sucesso!");
            response.getWriter().print(file.toString());

        } else if (request.getMethod().toLowerCase().equals("get")) {
            if (request.getSession().getAttribute("usuario") == null) {
                response.sendError(403, "Não existe sessão");
            } else {
                Usuario u = (Usuario) request.getSession().getAttribute("usuario");
                file.put("id", u.getID());
                file.put("nome", u.getNome());
                file.put("email", u.getEmail());
                file.put("role", u.getRole());
                file.put("senha", u.getSenha());
                response.getWriter().print(file.toString());
            }
        } else {
            response.sendError(405, "Method not allowed");
        }
    }

    private void processUsuarios(JSONObject file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getSession().getAttribute("usuario") == null) {
            response.sendError(401, "Não autorizado! Não existe sessão");
        } else if (!((Usuario) request.getSession().getAttribute("usuario")).getRole().equals("ADMIN")) {
            response.sendError(401, "Erro! Apenas administradores podem manipular usuarios!");
        } else if (request.getMethod().toLowerCase().equals("get")) {
            
            file.put("usuarios", new JSONArray(Usuario.getUsuarios()));
            response.getWriter().print(file.toString());
        } else if (request.getMethod().toLowerCase().equals("post")) {

            JSONObject body = getJSONBody(request.getReader());
            String nome = body.getString("nome");
            String email = body.getString("email");
            String senha = body.getString("senha");
            Usuario.addUsuario(nome, email, "USER", senha, true);

            file.put("message", "Usuario cadastrado com sucesso!");
            response.getWriter().print(file.toString());

        } else if (request.getMethod().toLowerCase().equals("put")) {

            JSONObject body = getJSONBody(request.getReader());
            Long id = Long.parseLong(request.getParameter("id"));
            String nome = body.getString("nome");
            String email = body.getString("email");
            String role = body.getString("role");
            String password = body.getString("senha");
            Usuario.updateUsuario(id, nome, email, password, role);

            file.put("message", "Usuário atualizado com sucesso!");
            response.getWriter().print(file.toString());

        } else if (request.getMethod().toLowerCase().equals("delete")) {

            Long id = Long.parseLong(request.getParameter("id"));
            Usuario.deleteUsuario(id);

            file.put("message", "Usuário deletado com sucesso!");
            response.getWriter().print(file.toString());

        }
    }

    private void processAvisos(JSONObject file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getSession().getAttribute("usuario") == null) {
            response.sendError(401, "Erro! Usuário não está logado!");
        } else if (request.getMethod().toLowerCase().equals("get")) {
            file.put("avisos", new JSONArray(Aviso.getAvisos()));
            response.getWriter().print(file.toString());
        } else if (request.getMethod().toLowerCase().equals("post")) {
            if (!((Usuario) request.getSession().getAttribute("usuario")).getRole().equals("ADMIN")) {
                response.sendError(401, "Erro! Apenas administradores podem adicionar avisos!");
            } else {
                JSONObject body = getJSONBody(request.getReader());
                Long id = Long.parseLong(request.getParameter("idUsuario"));
                String titulo = body.getString("titulo");
                String conteudo = body.getString("conteudo");
                String data = LocalDateTime.now().toString();
                Aviso.addAviso(titulo, conteudo, data, id);

                file.put("message", "Aviso adicionado com sucesso!");
                response.getWriter().print(file.toString());
            }
        } else if (request.getMethod().toLowerCase().equals("put")) {
            if (!((Usuario) request.getSession().getAttribute("usuario")).getRole().equals("ADMIN")) {
                response.sendError(401, "Erro! Apenas administradores podem atualizar avisos!");
            } else {
                JSONObject body = getJSONBody(request.getReader());
                Long id = Long.parseLong(request.getParameter("idAviso"));
                String titulo = body.getString("titulo");
                String conteudo = body.getString("conteudo");
                String data = LocalDateTime.now().toString();
                Aviso.updateAviso(id, titulo, conteudo, data);

                file.put("message", "Aviso atualizado com sucesso!");
                response.getWriter().print(file.toString());
            }
        } else if (request.getMethod().toLowerCase().equals("delete")) {
            if (!((Usuario) request.getSession().getAttribute("usuario")).getRole().equals("ADMIN")) {
                response.sendError(401, "Erro! Apenas administradores podem deletar avisos!");
            } else {
                Long id = Long.parseLong(request.getParameter("idAviso"));
                Aviso.removeAviso(id);

                file.put("message", "Aviso deletado com sucesso!");
                response.getWriter().print(file.toString());
            }
        }
    }

}

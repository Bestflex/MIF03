package fr.univlyon1.m1if.m1if03.servlets;

import fr.univlyon1.m1if.m1if03.classes.User;
import fr.univlyon1.m1if.m1if03.daos.Dao;
import fr.univlyon1.m1if.m1if03.daos.UserDao;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.InvalidNameException;
import java.io.IOException;

/**
 * Cette servlet initialise les objets communs à toute l'application,
 * récupère les infos de l'utilisateur pour les placer dans sa session
 * et affiche l'interface du chat.
 *
 * @author Lionel Médini
 */
@WebServlet(name = "Connect", urlPatterns = {"/connect"})
public class Connect extends HttpServlet {
    private Dao<User> users;

    public void init(ServletConfig config) throws ServletException {
        // Cette instruction doit toujours être au début de la méthode init() pour pouvoir accéder à l'objet config.
        super.init(config);
        //Récupère le contexte applicatif et y place les variables globales
        ServletContext context = config.getServletContext();

        // Récupère le DAO d'utilisateurs depuis le contexte
        users = (UserDao) context.getAttribute("users");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String operation = request.getParameter("operation");

        if ("connexion".equals(operation)) {
            // Connexion
            User user = new User(request.getParameter("login"), request.getParameter("name"));
            try {
                users.add(user);
                // Création de la session utilisateur
                HttpSession session = request.getSession(true);
                session.setAttribute("login", user.getLogin());
                response.sendRedirect("interface.jsp"); 
            } catch (NameAlreadyBoundException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Un utilisateur avec le login " + user.getLogin() + " existe déjà.");
            }
        } else if ("deconnexion".equals(operation)) {
            // Déconnexion
            HttpSession session = request.getSession(false);
            if (session != null) {
                String login = (String) session.getAttribute("login");
                session.invalidate();
                try {
                    users.deleteById(login);
                } catch (NameNotFoundException | InvalidNameException e) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Le login de l'utilisateur courant est erroné : " + login + ".");
                }
                response.sendRedirect("index.html");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous devez vous connecter pour accéder au site.");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String operation = request.getParameter("operation");

        if ("connexion".equals(operation)) {
            // Redirige vers la page de connexion
            request.getRequestDispatcher("interface.jsp").forward(request, response);
        } else if ("deconnexion".equals(operation)) {
            // Déconnexion
            HttpSession session = request.getSession(false);
            if (session != null) {
                String login = (String) session.getAttribute("login");
                session.invalidate();
                try {
                    users.deleteById(login);
                } catch (NameNotFoundException | InvalidNameException e) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Le login de l'utilisateur courant est erroné : " + login + ".");
                }
                response.sendRedirect("index.html");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous devez vous connecter pour accéder au site.");
            }
        }
    }
}

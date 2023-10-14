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

import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;

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
    private Dao<User> users; // Plus besoin d'initialiser ici

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();
        
        // Récupère le DAO d'utilisateurs depuis le contexte
        users = (UserDao) context.getAttribute("users");
    }

     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = new User(request.getParameter("login"), request.getParameter("name"));
        // Créez un attribut de session pour l'utilisateur connecté
        HttpSession session = request.getSession(true);
        session.setAttribute("login", user.getLogin());
        
        try {
            users.add(user);
        } catch (NameAlreadyBoundException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Un utilisateur avec le login " + user.getLogin() + " existe déjà.");
            return;
        }
        response.sendRedirect("interface.jsp");
    }

     @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String operation = request.getParameter("operation");
     // vérifier l'opération demandée et rediriger la requête vers la servlet "Deco" si l'opération est "deconnexion" 
        
     if (operation != null && operation.equals("true")) {
        HttpSession session = request.getSession(false);
        String login = null;
        
        try{
            if (session != null) {
                login = (String) session.getAttribute("login");
                session.invalidate();
            }
            ((Dao<User>) this.getServletContext().getAttribute("users")).deleteById(login);
            response.sendRedirect("index.html");
        } catch (NameNotFoundException | InvalidNameException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Le login de l'utilisateur courant est erroné : " + login + ".");
        }
    } else {
        // Gérer d'autres opérations si nécessaire
       
        response.sendRedirect("index.html");
    }
    }
}

package fr.univlyon1.m1if.m1if03.servlets;

import fr.univlyon1.m1if.m1if03.daos.UserDao;
import fr.univlyon1.m1if.m1if03.daos.TodoDao;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

/**
 * Servlet d'initialisation chargée d'initialiser les DAOs au démarrage de l'application.
 * @author Oussama Benaziz
 */
@WebServlet(value = "/init", loadOnStartup = 1)
public class Init extends HttpServlet {
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();
        
        // Initialise les DAOs et les place dans le contexte
        context.setAttribute("users", new UserDao());
        context.setAttribute("todos", new TodoDao());
    }
}

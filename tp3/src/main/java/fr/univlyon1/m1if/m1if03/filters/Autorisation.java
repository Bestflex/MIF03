package fr.univlyon1.m1if.m1if03.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Ce filtre vérifie si l'utilisateur actuellement connecté a le droit de modifier le nom d'un utilisateur.
 * @author Oussama Benaziz
 */
@WebFilter(filterName = "Autorisation", urlPatterns = {"/userlist.jsp"})
public class Autorisation extends HttpFilter {
    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (request.getMethod().equals("POST")) {
            if (session != null) {
                String login = (String) session.getAttribute("login");
                String loginToModify = request.getParameter("login");
                if (loginToModify != null && login.equals(loginToModify)) {
                    // L'utilisateur est autorisé à modifier son propre nom
                    chain.doFilter(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous n'avez pas le droit de modifier l'utilisateur " + loginToModify);
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous devez vous connecter pour accéder à cette page.");
            }
        } else {
            // Pour les requêtes autres que POST, on laisse simplement passer.
            chain.doFilter(request, response);
        }
    }
}


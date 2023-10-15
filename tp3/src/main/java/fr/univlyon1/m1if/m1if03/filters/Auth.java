package fr.univlyon1.m1if.m1if03.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;

/**
 * Filtre d'authentification.
 * N'autorise l'accès qu'aux clients ayant déjà une session existante ou ayant rempli le formulaire de la page <code>index.html</code>.
 * Dans ce dernier cas, le filtre crée la session de l'utilisateur, crée un objet User et l'ajoute en attribut de la session.
 * Laisse toutefois passer les URLs "/" et "/index.html".
 */
@WebFilter(filterName = "Auth", urlPatterns = {"*"})
public class Auth extends HttpFilter {
    private final String[] whiteList = {"/", "/index.html", "/css/style.css",  "/connect"};

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String url = request.getRequestURI().replace(request.getContextPath(), "");

        if (Arrays.asList(whiteList).contains(url) || request.getSession(false) != null) {
            chain.doFilter(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous devez vous connecter pour accéder au site.");
    }
}

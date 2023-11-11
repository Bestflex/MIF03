package fr.univlyon1.m1if.m1if03.controllers;

import fr.univlyon1.m1if.m1if03.dao.TodoDao;
import fr.univlyon1.m1if.m1if03.dto.todo.TodoRequestDto;
import fr.univlyon1.m1if.m1if03.model.Todo;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.util.NoSuchElementException;

import fr.univlyon1.m1if.m1if03.dao.TodoDao;
import fr.univlyon1.m1if.m1if03.model.Todo;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

/**
 * Contrôleur d'opérations métier "todos".<br>
 * Concrètement : change le status d'un todo
 */
@WebServlet(name = "TodoBusinessController", urlPatterns = {"/todos/toggleStatus"})
public class TodoBusinessController extends HttpServlet {
    private TodoBusiness todoBusiness;


    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        TodoDao todoDao = (TodoDao) config.getServletContext().getAttribute("todoDao");
        todoBusiness = new TodoBusiness(todoDao);
    }

    /**
     * Réalise l'opération demandée en fonction de la fin de l'URL de la requête (<code>/todos/toggleStatus</code>).
     * C'est à dire toggle le statut d'un todo donné en paramètre de requête.
     * @param request  Voir doc...
     * @param response Voir doc...
     * @throws IOException Voir doc...
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            TodoRequestDto requestDto = (TodoRequestDto) request.getAttribute("dto");
            todoBusiness.toggleStatus(requestDto.getHash());
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (NoSuchElementException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Le todo demandé n'existe pas.");
        }

    }

    private static class TodoBusiness {
        private TodoDao todoDao;

        /**
         * Constructeur avec une injection du DAO nécessaire aux opérations.
         * @param todoDao le DAO de todos provenant du contexte applicatif
         */
        TodoBusiness(TodoDao todoDao) {
            this.todoDao = todoDao;
        }

        /**
         * Réalise l'opération de changement de statut d'un todo.<br>
         * Renvoie un code HTTP 204 (No Content).<br>
         * @param hash  l'identifiant hash du todo à modifier
         */
        public void toggleStatus(@NotNull int hash) {
            Todo targetTodo = todoDao.findByHash(hash);
            targetTodo.setCompleted(!targetTodo.isCompleted());
        }
    }
}
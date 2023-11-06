package fr.univlyon1.m1if.m1if03.controllers;

import fr.univlyon1.m1if.m1if03.dao.TodoDao;
import fr.univlyon1.m1if.m1if03.dto.todo.TodoDtoMapper;
import fr.univlyon1.m1if.m1if03.dto.todo.TodoResponseDto;
import fr.univlyon1.m1if.m1if03.dto.user.UserResponseDto;
import fr.univlyon1.m1if.m1if03.exceptions.ForbiddenLoginException;
import fr.univlyon1.m1if.m1if03.model.Todo;
import fr.univlyon1.m1if.m1if03.model.User;
import fr.univlyon1.m1if.m1if03.utils.UrlUtils;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import java.io.IOException;
import java.util.Collection;

/**
 * Contrôleur de ressources "todos".<br>
 * Gère les opérations CRUD sur la collection de TODOs :
 * <ul>
 *     <li>Création d'un TODOs : POST</li>
 *     <li>Récupération de la liste de TODOs / d'un TODOs : GET</li>
 *     <li>Mise à jour d'un TODOs : PUT</li>
 *     <li>Suppression d'un TODOs : DELETE</li>
 * </ul>
 * Cette servlet fait appel à une <i>nested class</i> <code>TodoResource</code> qui se charge des appels au DAO.
 *
 * @author Oussama Benaziz
 */
@WebServlet(name = "TodoResourceController", urlPatterns = {"/todos", "/todos/*"})
public class TodoResourceController extends HttpServlet {
    private TodoDtoMapper todoMapper;
    private TodoResource todoResource;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        TodoDao todoDao = (TodoDao) config.getServletContext().getAttribute("todoDao");
        todoMapper = new TodoDtoMapper(config.getServletContext());
        todoResource = new TodoResource(todoDao);
    }

    /**
     * Réalise la création d'un TODOs.
     * Renvoie un code 201 (Created) en cas de création d'un TODOs, ou une erreur HTTP appropriée sinon.
     *
     * @param request  Une requête de création, contenant un body avec un titre et le login du créateur
     * @param response Une réponse vide, avec uniquement un code de réponse et éventuellement un header <code>Location</code>
     * @throws IOException En cas d'erreur d'entrée/sortie lors de la gestion de la requête ou de la réponse
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] url = UrlUtils.getUrlParts(request);

        if (url.length == 1) {// Création d'un todos
            String title = request.getParameter("title");
            String creator = request.getParameter("login");
            try {
                todoResource.create(title, creator);
                response.setHeader("Location", "todos/" + title);
                response.setStatus(HttpServletResponse.SC_CREATED);
            } catch (IllegalArgumentException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            }
        } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }


    /**
     * Traite les requêtes GET.
     * Renvoie une représentation de la ressource demandée.
     * <ul>
     *     <li>Soit la liste de tous les utilisateurs</li>
     *     <li>Soit un utilisateur</li>
     *     <li>soit une propriété d'un utilisateur</li>
     *     <li>soit une redirection vers une sous-propriété</li>
     * </ul>
     * Renvoie un code de réponse 200 (OK) en cas de représentation, 302 (Found) en cas de redirection, sinon une erreur HTTP appropriée.
     *
     * @param request  Une requête vide
     * @param response Une réponse contenant :
     *                 <ul>
     *                     <li>la liste des liens vers les instances de <code>User</code> existantes</li>
     *                     <li>les propriétés d'un utilisateur</li>
     *                     <li>une propriété donnée d'un utilisateur donné</li>
     *                 </ul>
     * @throws ServletException Voir doc...
     * @throws IOException      Voir doc...
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("X-test", "doGet");
        String[] url = UrlUtils.getUrlParts(request);
        if (url.length == 1) { // Renvoie la liste de tous les utilisateurs
            request.setAttribute("todos", todoResource.readAll());
            // Transfère la gestion de l'interface à une JSP
            request.getRequestDispatcher("/WEB-INF/components/todos.jsp").include(request, response);
            return;
        }
        try {
            Todo todo = todoResource.readOne(url[1]);
            TodoResponseDto todoDto = todoMapper.toDto(todo);
            switch (url.length) {
                case 2 -> { // Renvoie un DTO d'utilisateur (avec toutes les infos le concernant pour pouvoir le templater dans la vue)
                    request.setAttribute("todoDto", todoDto);
                    request.getRequestDispatcher("/WEB-INF/components/todo.jsp").include(request, response);
                }
                case 3 -> { // Renvoie une propriété d'un utilisateur
                    switch (url[2]) {
                        case "title" -> {
                            request.setAttribute("todoDto", new TodoResponseDto(todoDto.getTitle(), todoDto.getHash(), null, null));
                            request.getRequestDispatcher("/WEB-INF/components/todoProperty.jsp").include(request, response);
                        }
                        case "assignee" -> {
                            request.setAttribute("todoDto", new TodoResponseDto(todoDto.getTitle(), todoDto.getHash(), todoDto.getAssignee(), null));
                            request.getRequestDispatcher("/WEB-INF/components/todoProperty.jsp").include(request, response);
                        }
                        case "completed" -> {
                            request.setAttribute("todoDto", new TodoResponseDto(todoDto.getTitle(), todoDto.getHash(), null, todoDto.getCompleted()));
                            request.getRequestDispatcher("/WEB-INF/components/todoProperty.jsp").include(request, response);
                        }
                        default -> response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
                default -> { // Redirige vers l'URL qui devrait correspondre à la sous-propriété demandée (qu'elle existe ou pas ne concerne pas ce contrôleur)
                    if (url[2].equals("assignee")) {
                        // Construction de la fin de l'URL vers laquelle rediriger
                        String urlEnd = UrlUtils.getUrlEnd(request, 3);
                        response.sendRedirect("todos" + urlEnd);
                    } else {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (NameNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Le todo " + url[1] + " n'existe pas.");
        } catch (InvalidNameException ignored) {
            // Ne devrait pas arriver car les paramètres sont déjà des Strings
        }
    }

    /**
     * Réalise la modification d'un todos.
     * En fonction du title passé dans l'URL :
     * <ul>
     *     <li>création du todos s'il n'existe pas</li>
     *     <li>Mise à jour sinon</li>
     * </ul>
     * Renvoie un code de statut 204 (No Content) en cas de succès ou une erreur HTTP appropriée sinon.
     *
     * @param request  Une requête dont l'URL est de la forme <code>/todos/{title}</code>, et contenant :
     *                 <ul>
     *                     <li>un password (obligatoire en cas de création)</li>
     *                     <li>un nom (obligatoire en cas de création)</li>
     *                 </ul>
     * @param response Une réponse vide (si succès)
     * @throws IOException Voir doc...
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] url = UrlUtils.getUrlParts(request);
        String title = url[1];
        String assignee = request.getParameter("assignee");
        String creator = request.getParameter("login");

        if (url.length == 2) {
            try {
                todoResource.update(title, assignee);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (IllegalArgumentException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            } catch (NameNotFoundException e) {
                try {
                    todoResource.create(title, creator);
                    response.setHeader("Location", "todos/" + title);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                } catch (IllegalArgumentException ex) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                }
            } catch (InvalidNameException ignored) {
                // Ne devrait pas arriver car les paramètres sont déjà des Strings
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Réalise l'aiguillage des requêtes DELETE.<br>
     * Réalise la suppression d'un TODOs à partir de son titre.
     * Renvoie un code 204 (No Content) en cas de suppression réussie, ou une erreur HTTP appropriée sinon.
     *
     * @param request  Une requête de suppression, contenant le titre du TODOs à supprimer
     * @param response Une réponse vide, avec uniquement un code de réponse
     * @throws IOException En cas d'erreur d'entrée/sortie lors de la gestion de la requête ou de la réponse
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] url = UrlUtils.getUrlParts(request);
        String title = url[1];
        if (url.length == 2) {
            try {
                todoResource.delete(title);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (IllegalArgumentException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            } catch (NameNotFoundException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Le todo " + title + " n'existe pas.");
            } catch (InvalidNameException ignored) {
                // Ne devrait pas arriver car les paramètres sont déjà des Strings
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private static class TodoResource {
        private final TodoDao todoDao;

        /**
         * Constructeur avec une injection du DAO nécessaire aux opérations.
         * @param todoDao le DAO des todos provenant du contexte applicatif
         */
        TodoResource(TodoDao todoDao) { this.todoDao = todoDao; }

        /**
         * Crée un todos.
         *
         * @param title    Title du todos à créer
         * @param creator   Creator du todos à créer
         * @throws IllegalArgumentException Si le login est null ou vide ou si le password est null
         */
        public void create(@NotNull String title, @NotNull String creator)
                throws IllegalArgumentException{
            if (title == null || title.isEmpty()) {
                throw new IllegalArgumentException("Le title ne doit pas être null ou vide.");
            }
            todoDao.add(new Todo(title, creator));
        }

        /**
         * Renvoie les IDs de tous les todos présents dans le DAO.
         *
         * @return L'ensemble des IDs sous forme d'un <code>Set&lt;Serializable&gt;</code>
         */
        public Collection<Todo> readAll() { return todoDao.findAll(); }

        /**
         * Renvoie un todos s'il est présent dans le DAO.
         *
         * @param title Le tile de l'utilisateur demandé
         * @return L'instance de <code>TODOs</code> correspondant au title
         * @throws IllegalArgumentException Si le title est null ou vide
         * @throws NameNotFoundException Si le title ne correspond à aucune entrée dans le DAO
         * @throws InvalidNameException Ne doit pas arriver car les clés du DAO todos sont des strings
         */
        public Todo readOne(@NotNull String title) throws IllegalArgumentException, NameNotFoundException, InvalidNameException {
            if (title == null || title.isEmpty()) {
                throw new IllegalArgumentException("Le title ne doit pas être null ou vide.");
            }
            return todoDao.findOne(title);
        }

        /**
         * Met à jour un todos en fonction des paramètres envoyés.<br>
         * Si l'un des paramètres est nul ou vide, le champ correspondant n'est pas mis à jour.
         *
         * @param title     Le login de l'utilisateur à mettre à jour
         * @param assignee  le login de l'utilisateur assigné au todos
         * @throws IllegalArgumentException Si le title est null ou vide
         * @throws InvalidNameException Ne doit pas arriver car les clés du DAO user sont des strings
         * @throws NameNotFoundException Si le title ne correspond pas à un utilisateur existant
         */
        public void update(@NotNull String title, String assignee) throws IllegalArgumentException, InvalidNameException, NameNotFoundException {
            Todo todo = readOne(title);
            if (assignee != null && !assignee.isEmpty()) {
                todo.setAssignee(assignee);
            }
        }

        /**
         * Supprime un utilisateur dans le DAO.
         *
         * @param title Le login de l'utilisateur à supprimer
         * @throws IllegalArgumentException Si le login est null ou vide
         * @throws NameNotFoundException Si le login ne correspond à aucune entrée dans le DAO
         * @throws NameNotFoundException Si le login ne correspond à aucune entrée dans le DAO
         */
        public void delete(@NotNull String title) throws IllegalArgumentException, NameNotFoundException, InvalidNameException {
            if (title == null || title.isEmpty()) {
                throw new IllegalArgumentException("Le title ne doit pas être null ou vide.");
            }
            todoDao.deleteById(title);
        }

    }
}

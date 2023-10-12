package fr.univlyon1.m1if.m1if03.daos;
import fr.univlyon1.m1if.m1if03.classes.Todo;

/**
 * classe TodoDao qui dérive de AbstractListDao.
 *
 * @author Oussama Benaziz
 */
public class TodoDao extends AbstractListDao<Todo> {
	public List<Todo> findTodosByUser(User user) {
        List<Todo> todosByUser = new ArrayList<>();
        for (Todo todo : collection) {
            if (todo != null && todo.getAssignee() != null && todo.getAssignee().equals(user)) {
                todosByUser.add(todo);
            }
        }
        return todosByUser;
    }
}

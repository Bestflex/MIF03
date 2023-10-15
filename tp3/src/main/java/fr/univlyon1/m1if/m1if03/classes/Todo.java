package fr.univlyon1.m1if.m1if03.classes;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * TODOs créés par les utilisateurs.
 * Un TODO_ comporte un titre et un statut (à faire ou fait),
 * et éventuellement, le login de l'utilisateur assigné à sa réalisation.
 * Pour créer un TODO_, il faut indiquer l'id de l'utilisateur qui l'a créé.
 */
public class Todo {
    private final int hash;
    private String title;
    private Serializable assigneeId = null;
    private boolean completed = false;

    /**
     * Création d'un TODO_.
     * @param title Texte indiqué dans le TODO_
     * @param creatorId Login de l'utilisateur créateur
     */
    public Todo(String title, Serializable creatorId) {
        this.title = title;
        // On rassemble toutes les infos sur l'instance, pour permettre de les distinguer
        this.assigneeId = creatorId;
        this.hash = Objects.hash(title, creatorId, (new Date()).getTime());
    }

    public String getTitle() {
        return title;
    }

    /**
     * Modifie le titre affecté à la création du TODO_.
     * (n'affecte pas le hash)
     * @param title Nouveau titre du TODO_
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public Serializable getAssigneeId() {
        return assigneeId;
    }

    /**
     * Assigne un utilisateur à la réalisation du TODO_.
     * @param assignee Login de l'utilisateur à assigner
     */
    public void setAssigneeId(User assignee) {
    this.assigneeId = assignee != null ? assignee.getLogin() : null;
}

    public boolean isCompleted() {
        return completed;
    }

    /**
     * Définit le statut du TODO_ (fait ou à faire).
     * @param completed Nouveau statut
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return this.hashCode() == todo.hashCode();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }
}

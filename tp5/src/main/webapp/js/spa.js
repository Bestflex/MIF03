/**
 * Placez ici les scripts qui seront exécutés côté client pour rendre l'application côté client fonctionnelle.
 */

// <editor-fold desc="Gestion de l'affichage">

let user = {
    login: '',
    name: '',
    assignedTodos: []
}

let userAuth = "";
let users = [];

let detailedTodos = [];

let TodosTimer;

function renderTemplate(templateId, data, targetId) {
    let template = document.getElementById(templateId);

    if (template) {
        document.getElementById(targetId).innerHTML = Mustache.render(template.innerHTML, data);
    } else {
        console.error("Erreur: Impossible de trouver l'élément avec l'ID spécifié.");
    }
}

async function getName() {
    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    headers.append("Accept", "application/json");
    headers.append("Authorization", userAuth);

    const requestConfig = {
        method: "GET",
        headers: headers,
        mode: "cors"
    };

    try {
        const response = await fetch(baseUrl + "users/" + user.login, requestConfig);

        if (response.ok) {
            const userData = await response.json();
            user.name = userData.name;
        } else {
            console.error("Erreur lors de la récupération du nom de l'utilisateur :", response.status);
        }
    } catch (error) {
        console.error("Erreur lors de la récupération du nom de l'utilisateur :", error);
    }
}

/**
 * Envoie la requête pour mettre à jour le nom de l'utilisateur.
 */
function updateName() {
    let updatedName = document.getElementById("name_update_input").value;
    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    headers.append("Accept", "application/json");
    headers.append("Authorization", userAuth);
    const requestConfig = {
        method: "PUT",
        headers: headers,
        mode: "cors", // pour le cas où vous utilisez un serveur différent pour l'API et le client.
        body: JSON.stringify({name: updatedName})
    };
    fetch(baseUrl + "users/" + user.login + "/", requestConfig)
        .then(response => {
            if (response.status === 204) {
                displayRequestResult("Mise à jour du nom de l'utilisateur effectuée", "alert-success");
                user.name = updatedName;
            } else {
                displayRequestResult("Erreur dans la mise à jour du nom de l'utilisateur", "alert-success");
            }
        })
}

/**
 * Envoie la requête pour mettre à jour le mot de passe de l'utilisateur.
 */
function updatePassword() {
    let newPassword = document.getElementById("password_update_input").value;
    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    headers.append("Authorization", userAuth);

    const requestConfig = {
        method: "PUT",
        headers: headers,
        mode: "cors",
        body: JSON.stringify({ name: user.name, password: newPassword })
    };

    fetch(baseUrl + "users/" + user.login + "/", requestConfig)
        .then(response => {
            if (response.status === 204) {
                displayRequestResult("Mot de passe mis à jour avec succès", "alert-success");
            } else {
                displayRequestResult("Erreur lors de la mise à jour du mot de passe", "alert-danger");
            }
        })
        .catch(error => {
            console.error("Error updating password: " + error);
        });
}

/**
 * Envoie la requête pour creer un todos.
 */
async function createTodo() {
    try {
        let todoTitle = document.getElementById("newTodoTitle").value;
        const headers = new Headers();
        headers.append("Content-Type", "application/json");
        headers.append("Accept", "application/json");
        headers.append("Authorization", userAuth);

        const requestConfig = {
            method: "POST",
            headers: headers,
            mode: "cors",
            body: JSON.stringify({ title: todoTitle, creator: user.login, name: user.name })
        };

        const response = await fetch(baseUrl + "todos", requestConfig);

        if (response.ok && response.status === 201) {
            displayRequestResult("Nouveau todo créé avec succès", "alert-success");
            user.assignedTodos.push({
                title: todoTitle,
                creator: user.login
            });
        } else {
            displayRequestResult("Erreur lors de la création du todo", "alert-danger");
        }
    } catch (error) {
        console.error("Error creating todo: " + error);
    }
}

async function updateTodo(todoId) {
    try {
        let updatedTitle = document.getElementById("title_update_input" + todoId).value;
        const headers = new Headers();
        headers.append("Content-Type", "application/json");
        headers.append("Accept", "application/json");
        headers.append("Authorization", userAuth);
        const requestConfig = {
            method: "PUT",
            headers: headers,
            mode: "cors",
            body: JSON.stringify({ title: updatedTitle })
        };
        const result = await fetch(baseUrl + "todos/" + todoId, requestConfig);
        if (result.ok) {
            const todoToUpdate = detailedTodos.find(todo => todo.hash === todoId);
            if (todoToUpdate) {
                if (!user.assignedTodos.some(todo => todo.hash === todoId)) {
                    user.assignedTodos.push({
                        hash: todoToUpdate.hash,
                        title: updatedTitle,
                        creator: user.login
                    });
                    detailedTodos.forEach(todo => {
                        if (todo.hash === todoId) {
                            detailedTodos.push({
                                hash: todoToUpdate.hash,
                                title: updatedTitle,
                                assignee: todoToUpdate.assignee
                            });
                        }
                    });
                }
            }
            displayRequestResult("Mise à jour du todo réussie", "alert-success");
        } else {
            displayRequestResult("Erreur dans la mise à jour du todo", "alert-danger");
        }
    } catch (error) {
        console.error("Erreur dans la mise à jour du todo", error);
        displayRequestResult("Erreur dans la mise à jour du todo", "alert-danger");
    }
}

function setStatus(todoId) {
    const headers = new Headers();
    headers.append("Accept", "*/*")
    headers.append("Content-Type", "application/json");
    headers.append("Authorization", userAuth);
    const body = {
        hash: todoId
    };
    const requestConfig = {
        method: "POST",
        headers: headers,
        body: JSON.stringify(body),
        mode: "cors"
    };
    fetch(baseUrl + "todos/toggleStatus", requestConfig)
        .then((response) => {
            if(response.status === 204) {
                displayRequestResult("Todo modifié", "alert-success");
                userAuth = response.headers.get("Authorization") ?? userAuth;
            } else {
                displayRequestResult("Erreur modification Todo", "alert-danger");
                throw new Error("Bad response code (" + response.status + ").");
            }
        })
        .catch((err) => {
            console.error("In setStatus: " + err);
        })
}

function assignTodo(todoId) {
    const headers = new Headers();
    headers.append("Accept", "application/json");
    headers.append("Content-Type", "application/json");
    headers.append("Authorization", userAuth);
    const body = {
        assignee: user.login
    };
    const requestConfig = {
        method: "PUT",
        headers: headers,
        body: JSON.stringify(body),
        mode: "cors"
    };
    fetch(baseUrl + "todos/" + todoId, requestConfig)
        .then((response) => {
            if (response.status === 204) {
                displayRequestResult("Todo assigné", "alert-success");
                userAuth = response.headers.get("Authorization") ?? userAuth;
                //getAllTodos();
            } else {
                displayRequestResult("Mise à jour refusée ou impossible", "alert-danger");
                throw new Error("Bad response code (" + response.status + ").");
            }
        })
        .catch((err) => {
            console.error("In assignTodo: " + err);
        })
}

async function getTodoAssignee(todoIds) {
    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    headers.append("Accept", "application/json");
    headers.append("Authorization", userAuth);

    const requestConfig = {
        method: "GET",
        headers: headers,
        mode: "cors"
    };

    // Tableau pour stocker les données à afficher dans le template
    const assignedTodosData = [];

    // Parcours des IDs fournis
    for (const todoId of todoIds) {
        try {
            const response = await fetch(baseUrl + "todos/" + todoId, requestConfig);
            if (response.ok) {
                const todoData = await response.json();
                console.log("todoData", todoData);
                if (todoData.assignee === ("users/" + user.login)) {
                    // Ajouter les données au tableau
                    assignedTodosData.push({
                        hash: todoData.hash,
                        title: todoData.title,
                        completed :todoData.completed
                    });
                }
            } else {
                console.error("Erreur lors de la récupération du todo " + todoId + ": " + response.status);
            }
        } catch (error) {
            console.error("Erreur lors de la récupération du todo " + todoId + ": " + error);
        }
    }
    // Après la boucle, rendre le template avec toutes les données accumulées
    renderTemplate('monCompteTemplate', { login: user.login, name: user.name, assignedTodos: assignedTodosData }, 'monCompteT');
}


/**
 * Envoie la requête pour tous les todos.
 */
async function getAllTodos() {
    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    headers.append("Accept", "application/json");
    headers.append("Authorization", userAuth);

    const requestConfig = {
        method: "GET",
        headers: headers,
        mode: "cors"
    };

    try {
        const response = await fetch(baseUrl + "todos", requestConfig);

        if (!response.ok) {
            throw new Error("Erreur à la récupération des todos");
        }

        const todoIds = await response.json();
        detailedTodos = await Promise.all(
            todoIds.map(async (todoId) => {
                try {
                    const detailResponse = await fetch(baseUrl + "todos/" + todoId, requestConfig);

                    if (!detailResponse.ok) {
                        throw new Error("Erreur à la récupération des détails du todo");
                    }

                    const detailData = await detailResponse.json();
                    return {
                        todoId: todoId,
                        title: detailData.title,
                        completed : detailData.completed
                    };
                } catch (error) {
                    console.error("Erreur à la récupération des détails du todo", error);
                    return null;
                }
            })
        );

        console.log("Tous les todos:", detailedTodos.map(todo => ({ title: todo.title })));

        const listTemplate = document.getElementById("ListTemplate").innerHTML;
        const renderedList = Mustache.render(listTemplate, { assignedTodos: detailedTodos });
        document.querySelector(".listT").innerHTML = renderedList;

        //renderTemplate("NumtodoList2Template", {assignedTodos: detailedTodos.length },"NumTodoList2");
        await getTodoAssignee(todoIds);

    } catch (error) {
        console.error("Erreur à la récupération des todos", error);
    }
}

function deleteTodo(todoId, updateTitle) {
    const headers = new Headers();
    headers.append("Accept", "application/json");
    headers.append("Authorization", userAuth);
    const requestConfig = {
        method: "DELETE",
        headers: headers,
        mode: "cors"
    };
    fetch(baseUrl + "todos/" + todoId, requestConfig)
        .then((response) => {
            if(response.status === 204) {
                displayRequestResult("Todo supprimé", "alert-success");
                userAuth = response.headers.get("Authorization") ?? userAuth;
                getAllTodos();
                user.assignedTodos = user.assignedTodos.filter(todo => todo.title !== updateTitle);
                //renderTemplate('monCompteTemplate', {login:user.login, name:user.name, assignedTodos: user.assignedTodos}, 'monCompteT');
            } else if(response.status === 404) {
                displayRequestResult("Todo non trouvé", "alert-danger");
            } else {
                displayRequestResult("Suppression refusée ou impossible", "alert-danger");
                throw new Error("Bad response code (" + response.status + ").");
            }
        })
        .catch((err) => {
            console.error("In deleteTodo: " + err);
        })
}
/**
 * Fait basculer la visibilité des éléments affichés quand le hash change.<br>
 * Passe l'élément actif en inactif et l'élément correspondant au hash en actif.
 * @param hash une chaîne de caractères (trouvée a priori dans le hash) contenant un sélecteur CSS indiquant un élément à rendre visible.
 */
function show(hash) {
    const oldActiveElement = document.querySelector(".active");
    oldActiveElement.classList.remove("active");
    oldActiveElement.classList.add("inactive");
    const newActiveElement = document.querySelector(hash.split("/")[0]);
    newActiveElement.classList.remove("inactive");
    newActiveElement.classList.add("active");
}

/**
 * Affiche pendant 10 secondes un message sur l'interface indiquant le résultat de la dernière opération.
 * @param text Le texte du message à afficher
 * @param cssClass La classe CSS dans laquelle afficher le message (défaut = alert-info)
 */
function displayRequestResult(text, cssClass = "alert-info") {
    const requestResultElement = document.getElementById("requestResult");
    requestResultElement.innerText = text;
    requestResultElement.classList.add(cssClass);
    setTimeout(
        () => {
            requestResultElement.classList.remove(cssClass);
            requestResultElement.innerText = "";
        }, 10000);
}

/**
 * Affiche ou cache les éléments de l'interface qui nécessitent une connexion.
 * @param isConnected un Booléen qui dit si l'utilisateur est connecté ou pas
 */
function displayConnected(isConnected) {
    if (isConnected) {
        renderTemplate('wrapperHeaderTemplate', {name: user.login}, 'wrapperHeader');
        renderTemplate('monCompteTemplate', {login: user.login, name: user.name, assignedTodos: user.assignedTodos}, 'monCompteT');
        //renderDiv("todoCountTemplate", {todoCount: user.assignedTodos.length},"todoCount");
        //renderDiv('compteInfosTemplate', {login: user.login, name: user.name}, 'compteInfos');
    }

    const elementsRequiringConnection = document.getElementsByClassName("requiresConnection");
    const visibilityValue = isConnected ? "visible" : "collapse";
    for(const element of elementsRequiringConnection) {
        element.style.visibility = visibilityValue;
    }
}

window.addEventListener('hashchange', () => { show(window.location.hash); });
// </editor-fold>

// <editor-fold desc="Gestion des requêtes asynchrones">
/**
 * Met à jour le nombre d'utilisateurs de l'API sur la vue "index".
 */
function getNumberOfUsers() {
    const headers = new Headers();
    headers.append("Accept", "application/json");
    const requestConfig = {
        method: "GET",
        headers: headers,
        mode: "cors" // pour le cas où vous utilisez un serveur différent pour l'API et le client.
    };

    fetch(baseUrl + "users", requestConfig)
        .then((response) => {
            if(response.ok && response.headers.get("Content-Type").includes("application/json")) {
                return response.json();
            } else {
                throw new Error("Response is error (" + response.status + ") or does not contain JSON (" + response.headers.get("Content-Type") + ").");
            }
        }).then((json) => {
        if(Array.isArray(json)) {
            renderTemplate("NumTodoListTemplate", {utilisateurs:json.length},"NumTodoList");
        } else {
            throw new Error(json + " is not an array.");
        }
    }).catch((err) => {
        console.error("In getNumberOfUsers: " + err);
    });
}

/**
 * Envoie la requête de login en fonction du contenu des champs de l'interface.
 */
function connect() {
    const headers = new Headers();
    headers.append("Content-Type", "application/json");
    headers.append("Accept", "application/json");
    headers.append("Authorization", userAuth);
    const body = {
        login: document.getElementById("login_input").value,
        password: document.getElementById("password_input").value
    };
    const requestConfig = {
        method: "POST",
        headers: headers,
        body: JSON.stringify(body),
        mode: "cors" // pour le cas où vous utilisez un serveur différent pour l'API et le client.
    };
    fetch(baseUrl + "users/login", requestConfig)
        .then((response) => {
            if(response.status === 204) {
                user.login = body.login;
                userAuth = response.headers.get("Authorization").replace("Bearer ", "");
                displayConnected(true);
                displayRequestResult("Connexion réussie", "alert-success");
                console.log("In login: Authorization = " + response.headers.get("Authorization"));
                location.hash = "#index";
                TodosTimer = setInterval(getAllTodos, 5000);
            } else {
                displayConnected(false);
                displayRequestResult("Connexion refusée ou impossible", "alert-danger");
                throw new Error("Bad response code (" + response.status + ").");
            }
        })
        .catch((err) => {
            console.error("In login: " + err);
        })
}

function deco() {
    // TODO envoyer la requête de déconnexion
    location.hash = "#index";
    displayConnected(false);
    clearInterval(TodosTimer);
}

setInterval(getNumberOfUsers, 5000);
// </editor-fold>
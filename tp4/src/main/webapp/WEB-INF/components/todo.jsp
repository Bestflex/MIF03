<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Todo</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<c:set var="todo" value="${requestScope.model}" scope="request"/>

<h1>Todo ${todo.title}</h1>
<ul>
    <li>Title: ${todo.title}</li>
    <li>Assignee: ${todo.assignee}</li>
    <li>Status: ${todo.completed}</li>
</ul>
</body>
</html>

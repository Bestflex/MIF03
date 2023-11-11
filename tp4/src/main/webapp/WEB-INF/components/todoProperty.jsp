<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Todo Property</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<c:set var="todo" value="${requestScope.model}" scope="request"/>

<h1>Propriété du Todo ${todo.title}</h1>
<ul>
    <c:if test="${todo.assignee != null}">
        <li>Assignee: ${todo.assignee}</li>
    </c:if>
    <c:if test="${todo.completed != null}">
        <li>Status: ${todo.completed}</li>
    </c:if>
</ul>
</body>
</html>

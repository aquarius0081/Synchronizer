<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<custom:mainLayout>
<form action="/performAction" method="post">
    Action:<br/>
    <select id="id" name="id">
        <c:forEach var="action" items="${actions}">
            <option value="${action.id}">
                    ${action.name}
            </option>
        </c:forEach>
    </select>
    <br/>
    File name:<br/>
    <input type="text" name="fileName" id="fileName"/><br/>
    <input id="submitBtn" type="submit" value="Submit"/>
</form>
</custom:mainLayout>
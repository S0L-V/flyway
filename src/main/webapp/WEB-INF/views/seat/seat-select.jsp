<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>사전 좌석 지정 - Flyway</title>

    <%@ include file="/WEB-INF/views/seat/seat-assets-css.jspf" %>
</head>

<body>
<%@ include file="/WEB-INF/views/seat/seat-header.jspf" %>

<div class="seat-container">
    <%@ include file="/WEB-INF/views/seat/seat-main.jspf" %>
    <%@ include file="/WEB-INF/views/seat/seat-notices.jspf" %>
</div>

<%@ include file="/WEB-INF/views/seat/seat-assets-js.jspf" %>
</body>
</html>

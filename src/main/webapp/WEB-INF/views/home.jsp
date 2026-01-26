<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>flyway</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/auth/home.css">
</head>
<body>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="banner"></div>
<main class="home-main">
    <a href="${pageContext.request.contextPath}/search"
       class="header__menu-item header__menu-item--cta">항공편 검색</a>
</main>

</body>
</html>

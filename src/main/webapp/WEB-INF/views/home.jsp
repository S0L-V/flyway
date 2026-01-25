<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>flyway</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/auth/home.css">
</head>
<body>

<header class="header">
    <div class="header__content">
        <div class="header__logo">
            <img src="${pageContext.request.contextPath}/resources/common/img/logo.svg" alt="Flyway" />
        </div>
        <nav class="header__menu">
            <a href="${pageContext.request.contextPath}/search" class="header__menu-item header__menu-item--cta">항공편 검색</a>
            <sec:authorize access="isAnonymous()">
                <a href="${pageContext.request.contextPath}/login" class="header__menu-item">로그인</a>
                <a href="${pageContext.request.contextPath}/signup" class="header__menu-item">회원가입</a>
            </sec:authorize>
            <sec:authorize access="isAuthenticated()">
                <a href="${pageContext.request.contextPath}/mypage" class="header__menu-item">마이페이지</a>
                <form class="header__menu-form" action="${pageContext.request.contextPath}/auth/logout" method="post">
                    <button type="submit" class="header__menu-item header__menu-item--logout">로그아웃</button>
                </form>
            </sec:authorize>
        </nav>
    </div>
</header>

<div class="banner"></div>
<main class="home-main">

</main>

</body>
</html>

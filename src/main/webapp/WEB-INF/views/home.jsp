<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>

    <title>Home</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/auth/home.css">
</head>
<body>

<div class="card">

    <div class="button-group">
        <sec:authorize access="isAnonymous()">
            <a href="${pageContext.request.contextPath}/login">
                <button class="btn btn-login">로그인</button>
            </a>

            <a href="${pageContext.request.contextPath}/signup">
                <button class="btn btn-signup">회원가입</button>
            </a>
        </sec:authorize>
        <sec:authorize access="isAuthenticated()">
            <a href="${pageContext.request.contextPath}/mypage">
                <button class="btn btn-mypage">마이페이지</button>
            </a>
            <form class="btn btn-logout" action="${pageContext.request.contextPath}/auth/logout" method="post">
                <button type="submit">로그아웃</button>
            </form>
        </sec:authorize>

    </div>
</div>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
        <a href="/login">
            <button class="btn btn-login">로그인</button>
        </a>

        <a href="/signup">
            <button class="btn btn-signup">회원가입</button>
        </a>

        <div class="divider"></div>

        <a href="/mypage">
            <button class="btn btn-mypage">마이페이지</button>
        </a>
    </div>
</div>

</body>
</html>

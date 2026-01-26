<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>로그인</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/auth/login.css">
</head>
<body>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="login-container">
    <h2>로그인</h2>

    <form action="${pageContext.request.contextPath}/loginProc" method="post">
        <div class="form-group">
            <label for="email">이메일</label>
            <input type="email" id="email" name="username" placeholder="example@mail.com" required>
        </div>

        <div class="form-group">
            <label for="password">비밀번호</label>
            <input type="password" id="password" name="password" placeholder="비밀번호를 입력하세요" required>
        </div>

        <button type="submit" class="btn-login">로그인</button>
    </form>

    <div class="divider">
        <span>또는</span>
    </div>

    <a href="${pageContext.request.contextPath}/auth/kakao" class="btn-kakao">
        <img src="https://developers.kakao.com/tool/resource/static/img/button/login/full/ko/kakao_login_medium_narrow.png"
             alt="카카오 로그인">
    </a>

    <div class="footer-links">
        계정이 없으신가요?
        <a href="${pageContext.request.contextPath}/signup">회원가입</a>
    </div>
</div>

</body>
</html>

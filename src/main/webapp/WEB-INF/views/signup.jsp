<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/auth/signup.css">
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원가입</title>
    <style>

    </style>
</head>
<body>

<div class="register-container">
    <h2>회원가입</h2>
    <form action="register_pro.jsp" method="post">
        <div class="form-group">
            <label for="userName">이름</label>
            <input type="text" id="userName" name="userName" placeholder="이름을 입력하세요" required>
        </div>
        <div class="form-group">
            <label for="userEmail">이메일</label>
            <input type="email" id="userEmail" name="userEmail" placeholder="example@mail.com" required>
        </div>
        <div class="form-group">
            <label for="userPassword">비밀번호</label>
            <input type="password" id="userPassword" name="userPassword" placeholder="비밀번호를 입력하세요" required>
        </div>
        <button type="submit" class="btn-submit">가입하기</button>
    </form>
</div>

</body>
</html>
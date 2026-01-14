<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원가입</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/auth/signup.css">
</head>
<body>

<div class="register-container">
    <h2>회원가입</h2>

    <!-- 에러 메시지 출력 -->
    <c:if test="${not empty error}">
        <div class="error">
            <c:out value="${error}" />
        </div>
    </c:if>

    <!-- Spring MVC 컨트롤러로 POST -->
    <form action="${pageContext.request.contextPath}/auth/signup" method="post">
        <div class="form-group">
            <label for="name">이름</label>
            <input type="text" id="name" name="name" placeholder="이름을 입력하세요" required>
        </div>

        <div class="form-group">
            <label for="email">이메일</label>
            <input type="email" id="email" name="email" placeholder="example@mail.com" required>
        </div>

        <div class="form-group">
            <label for="rawPassword">비밀번호</label>
            <input type="password" id="rawPassword" name="rawPassword" placeholder="비밀번호를 입력하세요" required>
        </div>

        <button type="submit" class="btn-submit">가입하기</button>
    </form>
</div>

</body>
</html>

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

    <form id="signupForm" action="${pageContext.request.contextPath}/auth/signup" method="post">
        <div class="form-group">
            <label for="name">이름</label>
            <input type="text" id="name" name="name" placeholder="이름을 입력하세요" required>
        </div>

        <!-- 이메일 + 인증메일 발송 -->
        <div class="form-group">
            <label for="email">이메일</label>

            <div class="email-row">
                <input type="email" id="email" name="email" placeholder="example@mail.com" required>
                <button type="button" id="sendVerifyBtn">인증메일</button>
            </div>

            <div id="sendStatus" class="status-text"></div>
        </div>

        <!-- 인증 코드 입력 + 검증 -->
        <div class="form-group" id="verifyBox" style="display:none;">
            <label for="verifyCode">인증 코드</label>

            <div style="display:flex; gap:8px;">
                <input type="text" id="verifyCode" name="verifyCode"
                       placeholder="메일로 받은 인증 코드를 입력하세요"
                       style="flex:1;">
                <button type="button" id="verifyBtn" class="btn-submit" style="white-space:nowrap;">
                    인증 확인
                </button>
            </div>

            <div id="verifyStatus" style="margin-top:8px; font-size: 0.9rem;"></div>

            <!-- 서버에서 최종 가입 시 검증할 수 있도록 인증 상태를 함께 보낼 수도 있음(선택) -->
            <input type="hidden" id="emailVerified" name="emailVerified" value="false">
        </div>

        <c:if test="${oauthSignUp}">
            <input type="hidden" name="oauthSignUp" value="true">
        </c:if>

        <c:if test="${not oauthSignUp}">
            <div class="form-group">
                <label for="rawPassword">비밀번호</label>
                <input type="password" id="rawPassword" name="rawPassword"
                       placeholder="비밀번호를 입력하세요" required>
            </div>
        </c:if>

        <button type="submit" class="btn-submit">가입하기</button>
    </form>
</div>

<script src="${pageContext.request.contextPath}/resources/auth/signup.js"></script>

</body>
</html>

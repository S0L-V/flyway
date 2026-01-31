<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>회원가입</title>
    <jsp:include page="/WEB-INF/views/common/head.jsp" />
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/auth/signup.css">
</head>
<body class="hero-page">

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="register-container">
    <h2>회원가입</h2>

    <!-- 에러 메시지 출력 -->
    <c:if test="${not empty error}">
        <div class="error">
            <c:out value="${error}"/>
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
                <input type="email" id="email" name="email" placeholder="example@mail.com" value="${signupEmail}"
                ${oauthSignUp and signupEmail ? 'readonly="readonly"' : ''}
                       required>

                <c:if test="${not oauthSignUp}">
                    <button type="button" id="sendVerifyBtn">인증메일</button>
                </c:if>
            </div>

            <c:if test="${not oauthSignUp}">
                <div id="sendStatus" class="status-text"></div>
            </c:if>
        </div>

        <c:if test="${not oauthSignUp}">
            <!-- 인증 확인 -->
            <div class="form-group verify-box is-hidden" id="verifyBox">
                <label>이메일 인증</label>

                <div class="verify-row">
                    <button type="button" id="verifyBtn" class="btn-submit btn-inline">
                        인증 확인
                    </button>
                </div>

                <div id="verifyStatus" class="verify-status"></div>

                <input type="hidden" id="emailVerified" name="emailVerified" value="false">
                <input type="hidden" id="attemptId" name="attemptId" value="">
            </div>
        </c:if>

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

<script>
    window.APP = {
        contextPath: "${pageContext.request.contextPath}"
    };
</script>
<script src="${pageContext.request.contextPath}/resources/auth/signup.js"></script>

</body>
</html>

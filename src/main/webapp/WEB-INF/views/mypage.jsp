<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>마이페이지</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/user/mypage.css">
</head>
<body>

<div class="mypage-container">
    <h2>내 프로필</h2>
    <div class="info-row">
        <span class="info-label">이름</span>
        <span class="info-value" id="profileName">-</span>
    </div>

    <div class="info-row">
        <span class="info-label">이메일</span>
        <span class="info-value" id="profileEmail">-</span>
    </div>

    <div class="info-row">
        <span class="info-label">가입 날짜</span>
        <span class="info-value" id="profileCreatedAt">-</span>
    </div>

    <div class="info-row">
        <span class="info-label">계정 상태</span>
        <span class="info-value">
        <span class="badge badge-status" id="profileStatus">-</span>
    </span>
    </div>

    <div class="button-group">
        <a href="#" class="btn btn-edit">정보 수정</a>
        <form class="btn btn-logout" action="${pageContext.request.contextPath}/auth/logout" method="post">
            <button type="submit">로그아웃</button>
        </form>
    </div>
</div>

<script>
    window.APP = {
        contextPath: "${pageContext.request.contextPath}"
    };
</script>
<script src="${pageContext.request.contextPath}/resources/user/mypage.js"></script>
</body>
</html>
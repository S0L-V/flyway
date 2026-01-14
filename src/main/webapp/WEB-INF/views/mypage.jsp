<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 세션 확인 (로그인 여부 체크)
    String sessionId = (String) session.getAttribute("userID");

    // 로그인이 안 된 상태라면 로그인 페이지로 리다이렉트
//    if (sessionId == null) {
//        response.sendRedirect("login.jsp");
//        return;
//    }

    // 가상의 사용자 데이터 (실제로는 DB에서 SELECT 쿼리로 가져오는 부분)
    String name = "홍길동";
    String email = sessionId; // 로그인 시 사용한 이메일
    String createdAt = "2023-10-25T10:30";
    String status = "활성(Active)";
    String provider = "EMAIL"; // 또는 "Kakao"
%>
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
        <span class="info-value"><%= name %></span>
    </div>

    <div class="info-row">
        <span class="info-label">이메일</span>
        <span class="info-value"><%= email %></span>
    </div>

    <div class="info-row">
        <span class="info-label">가입 날짜</span>
        <span class="info-value"><%= createdAt %></span>
    </div>

    <div class="info-row">
        <span class="info-label">계정 상태</span>
        <span class="info-value"><span class="badge badge-status"><%= status %></span></span>
    </div>

    <div class="info-row">
        <span class="info-label">가입 경로</span>
        <span class="info-value"><span class="badge badge-provider"><%= provider %></span></span>
    </div>

    <div class="button-group">
        <a href="#" class="btn btn-edit">정보 수정</a>
        <a href="logout.jsp" class="btn btn-logout">로그아웃</a>
    </div>
</div>

</body>
</html>
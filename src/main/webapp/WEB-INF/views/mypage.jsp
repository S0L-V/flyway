<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
  String tab = request.getParameter("tab");
  if (tab == null
      || !("dashboard".equals(tab)
          || "bookings".equals(tab)
          || "booking_detail".equals(tab)
          || "profile".equals(tab))) {
    tab = "dashboard";
  }
  request.setAttribute("activeTab", tab);
%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <title>마이페이지 - Flyway</title>
  <jsp:include page="/WEB-INF/views/mypage/include/head.jsp" />

</head>
<body class="custom-page bg-slate-50 font-sans min-h-screen flex flex-col">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="max-w-5xl w-full mx-auto px-6 py-10">
  <!-- Tab Navigation -->
  <div class="flex gap-8 mb-8 border-b border-slate-200">
    <a href="${pageContext.request.contextPath}/mypage?tab=dashboard"
       class="pb-3 px-1 text-sm font-bold flex items-center gap-2 transition-all relative <%= "dashboard".equals(tab) ? "text-primary-600" : "text-slate-400 hover:text-slate-600" %>">
      <i data-lucide="layout-dashboard" class="w-[18px] h-[18px]"></i> 대시보드
      <% if("dashboard".equals(tab)) { %><span class="absolute bottom-0 left-0 w-full h-[2px] bg-primary-600 rounded-t-full"></span><% } %>
    </a>
    <a href="${pageContext.request.contextPath}/mypage?tab=bookings"
       class="pb-3 px-1 text-sm font-bold flex items-center gap-2 transition-all relative <%= "bookings".equals(tab) || "booking_detail".equals(tab) ? "text-primary-600" : "text-slate-400 hover:text-slate-600" %>">
      <i data-lucide="ticket" class="w-[18px] h-[18px]"></i> 예약목록
      <% if("bookings".equals(tab) || "booking_detail".equals(tab)) { %><span class="absolute bottom-0 left-0 w-full h-[2px] bg-primary-600 rounded-t-full"></span><% } %>
    </a>
    <a href="${pageContext.request.contextPath}/mypage?tab=profile"
       class="pb-3 px-1 text-sm font-bold flex items-center gap-2 transition-all relative <%= "profile".equals(tab) ? "text-primary-600" : "text-slate-400 hover:text-slate-600" %>">
      <i data-lucide="user" class="w-[18px] h-[18px]"></i> 회원정보
      <% if("profile".equals(tab)) { %><span class="absolute bottom-0 left-0 w-full h-[2px] bg-primary-600 rounded-t-full"></span><% } %>
    </a>
  </div>

  <!-- Content Area -->
  <% if("dashboard".equals(tab)) { %>
  <jsp:include page="mypage/tabs/dashboard.jsp" />
  <% } else if("bookings".equals(tab)) { %>
  <jsp:include page="mypage/tabs/bookings.jsp" />
  <% } else if("booking_detail".equals(tab)) { %>
  <jsp:include page="mypage/tabs/booking_detail.jsp" />
  <% } else if("profile".equals(tab)) { %>
  <jsp:include page="mypage/tabs/profile.jsp" />
  <% } %>
</div>

<jsp:include page="mypage/include/toast.jsp" />
<script>
  window.APP = {
    contextPath: "${pageContext.request.contextPath}"
  };
</script>
<script type="module" src="${pageContext.request.contextPath}/resources/mypage/js/index.js?v=<%= System.currentTimeMillis() %>"></script>
<script>
  lucide.createIcons();
</script>
</body>
</html>

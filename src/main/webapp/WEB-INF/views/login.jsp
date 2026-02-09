<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <title>로그인</title>
    <jsp:include page="auth/include/head.jsp" />
    <script src="https://unpkg.com/lucide@0.563.0/dist/umd/lucide.min.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/login/css/login.css">
</head>
<body class="hero-page bg-slate-50 font-sans min-h-screen flex flex-col">

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="login-plane-bg" aria-hidden="true"></div>

<main class="relative z-10 flex-1 w-full flex items-center justify-center py-10 px-6">
    <div class="w-full max-w-md space-y-8 animate-fade-in">
        <div class="text-center">
            <h1 class="text-3xl font-extrabold text-slate-900 mb-2">로그인</h1>
            <p class="text-slate-500">Flyway에 오신 것을 환영합니다.</p>
        </div>

        <div class="bg-white p-8 rounded-2xl shadow-sm border border-slate-200">
            <c:if test="${param.error ne null}">
                <div class="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm">
                    이메일 또는 비밀번호가 올바르지 않습니다.
                </div>
            </c:if>
            <form action="${pageContext.request.contextPath}/loginProc" method="post" class="space-y-5">
                <input type="hidden" name="returnUrl" value="${fn:escapeXml(returnUrl)}">
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1.5">이메일</label>
                    <div class="relative">
                        <input type="email" id="email" name="username" class="w-full pl-10 pr-4 py-2.5 border border-slate-200 rounded-xl focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none" placeholder="name@example.com" required>
                        <i data-lucide="mail" class="absolute left-3.5 top-3 text-slate-400 w-[18px] h-[18px]"></i>
                    </div>
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1.5">비밀번호</label>
                    <div class="relative">
                        <input type="password" id="password" name="password" class="w-full pl-10 pr-4 py-2.5 border border-slate-200 rounded-xl focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none" placeholder="••••••••" required>
                        <i data-lucide="lock" class="absolute left-3.5 top-3 text-slate-400 w-[18px] h-[18px]"></i>
                    </div>
                </div>

                <button type="submit" class="w-full py-3 bg-primary-500 hover:bg-primary-600 text-white font-bold rounded-xl transition-all shadow-lg shadow-primary-500/30 transform active:scale-[0.98]">
                    로그인
                </button>
            </form>

            <div class="relative my-8">
                <div class="absolute inset-0 flex items-center"><div class="w-full border-t border-slate-200"></div></div>
                <div class="relative flex justify-center text-sm"><span class="px-3 bg-white text-slate-400">또는</span></div>
            </div>

            <a href="${pageContext.request.contextPath}/auth/kakao" class="w-full py-3 bg-[#FEE500] hover:bg-[#FDD835] text-[#391B1B] font-bold rounded-xl transition-colors flex items-center justify-center gap-2">
                <i data-lucide="message-circle" class="w-5 h-5 fill-current"></i> 카카오로 시작하기
            </a>
        </div>

        <div class="text-center text-sm text-slate-500">
            계정이 없으신가요? <a href="${pageContext.request.contextPath}/signup" class="text-primary-500 font-bold hover:underline ml-1">회원가입</a>
        </div>
    </div>
</main>

<script>
    if (typeof lucide !== "undefined" && typeof lucide.createIcons === "function") {
        lucide.createIcons();
    }
</script>

</body>
</html>

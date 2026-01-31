<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <title>회원가입</title>
    <jsp:include page="auth/include/head.jsp" />
    <script src="https://unpkg.com/lucide@0.563.0/dist/umd/lucide.min.js"></script>
</head>
<body class="hero-page bg-slate-50 font-sans min-h-screen flex flex-col" data-oauth-signup="${oauthSignUp}" data-has-error="${not empty error}">

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="flex-1 w-full flex items-center justify-center py-10 px-6">
  <!-- Step 1: Terms -->
  <div id="step-1" class="w-full max-w-md animate-fade-in">
    <h1 class="text-2xl font-bold text-slate-900 mb-6">약관 동의</h1>
    <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 space-y-4">
      <div class="pb-4 border-b border-slate-200">
        <label class="flex items-center gap-3 cursor-pointer p-2 hover:bg-slate-50 rounded-lg">
          <input type="checkbox" id="agree-all" class="w-5 h-5 rounded border-slate-300 text-primary-500 focus:ring-primary-500" onchange="toggleAllAgreements(this)">
          <span class="font-bold text-slate-800">약관 전체 동의</span>
        </label>
      </div>
      <div class="space-y-3 pl-2">
        <label class="flex items-center gap-3 cursor-pointer">
          <input type="checkbox" class="agree-item w-4 h-4 rounded border-slate-300 text-primary-500 focus:ring-primary-500" data-required="true" onchange="checkAllStatus()">
          <span class="text-sm text-slate-600">[필수] 이용약관 동의</span>
          <i data-lucide="chevron-right" class="ml-auto text-slate-400 w-4 h-4"></i>
        </label>
        <label class="flex items-center gap-3 cursor-pointer">
          <input type="checkbox" class="agree-item w-4 h-4 rounded border-slate-300 text-primary-500 focus:ring-primary-500" data-required="true" onchange="checkAllStatus()">
          <span class="text-sm text-slate-600">[필수] 개인정보 수집 및 이용 동의</span>
          <i data-lucide="chevron-right" class="ml-auto text-slate-400 w-4 h-4"></i>
        </label>
        <label class="flex items-center gap-3 cursor-pointer">
          <input type="checkbox" class="agree-item w-4 h-4 rounded border-slate-300 text-primary-500 focus:ring-primary-500" data-required="false" onchange="checkAllStatus()">
          <span class="text-sm text-slate-600">[선택] 마케팅 정보 수신 동의</span>
          <i data-lucide="chevron-right" class="ml-auto text-slate-400 w-4 h-4"></i>
        </label>
      </div>
    </div>
    <div class="mt-8 flex gap-3">
      <button type="button" onclick="location.href='${pageContext.request.contextPath}/login'" class="flex-1 py-3 text-slate-500 font-bold hover:bg-slate-100 rounded-xl transition-colors">취소</button>
      <button type="button" onclick="goToStep(2)" class="flex-1 py-3 bg-primary-500 text-white font-bold rounded-xl hover:bg-primary-600 transition-colors shadow-lg shadow-primary-500/30">다음</button>
    </div>
  </div>

  <!-- Step 2: Method -->
  <div id="step-2" class="w-full max-w-md animate-fade-in text-center hidden">
    <h1 class="text-2xl font-bold text-slate-900 mb-2">가입 방식 선택</h1>
    <p class="text-slate-500 mb-8">원하시는 회원가입 방식을 선택해주세요.</p>
    <div class="space-y-4">
      <button type="button" onclick="selectMethod('email')" class="w-full p-6 bg-white border border-slate-200 rounded-2xl hover:border-primary-500 hover:shadow-md transition-all group text-left flex items-center gap-4">
        <div class="w-12 h-12 bg-primary-50 text-primary-500 rounded-full flex items-center justify-center">
          <i data-lucide="mail" class="w-6 h-6"></i>
        </div>
        <div>
          <div class="font-bold text-slate-900 group-hover:text-primary-500 transition-colors">이메일로 가입하기</div>
          <div class="text-sm text-slate-400">자주 사용하는 이메일로 시작하세요</div>
        </div>
        <i data-lucide="chevron-right" class="ml-auto text-slate-300 group-hover:text-primary-500"></i>
      </button>
      <button type="button" onclick="selectMethod('kakao')" class="w-full p-6 bg-[#FEE500] border border-[#FEE500] rounded-2xl hover:bg-[#FDD835] transition-all group text-left flex items-center gap-4">
        <div class="w-12 h-12 bg-white/50 text-black/80 rounded-full flex items-center justify-center">
          <i data-lucide="message-circle" class="w-6 h-6"></i>
        </div>
        <div>
          <div class="font-bold text-black/90">카카오로 가입하기</div>
          <div class="text-sm text-black/60">카카오 계정으로 간편하게 시작하세요</div>
        </div>
        <i data-lucide="chevron-right" class="ml-auto text-black/40"></i>
      </button>
    </div>
    <button type="button" onclick="goToStep(1)" class="mt-8 text-sm text-slate-400 hover:text-slate-600 underline">이전으로</button>
  </div>

  <!-- Step 3: Form -->
  <div id="step-3" class="w-full max-w-md animate-fade-in hidden">
    <h1 class="text-2xl font-bold text-slate-900 mb-6">정보 입력</h1>

    <c:if test="${not empty error}">
      <div class="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm">
        <c:out value="${error}" />
      </div>
    </c:if>

    <form id="signupForm" action="${pageContext.request.contextPath}/auth/signup" method="post" class="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 space-y-5">

      <!-- Name -->
      <div>
        <label class="block text-sm font-medium text-slate-700 mb-1.5">이름</label>
        <input type="text" id="name" name="name" class="w-full px-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-primary-100 focus:border-primary-500 outline-none" placeholder="실명을 입력해주세요" required>
      </div>

      <!-- Email -->
      <div>
        <label class="block text-sm font-medium text-slate-700 mb-1.5">이메일</label>
        <div class="flex gap-2">
          <div class="relative flex-1">
            <input type="email" id="email" name="email" class="w-full pl-10 px-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-primary-100 focus:border-primary-500 outline-none" placeholder="example@email.com" value="${signupEmail}" ${oauthSignUp ? 'readonly="readonly"' : ''} required>
            <i data-lucide="mail" class="absolute left-3.5 top-3 text-slate-400 w-[18px] h-[18px]"></i>
          </div>
          <c:if test="${not oauthSignUp}">
            <button type="button" id="sendVerifyBtn" class="px-4 py-2 bg-slate-800 text-white text-sm font-bold rounded-xl hover:bg-slate-900 disabled:bg-slate-300 whitespace-nowrap">인증메일</button>
            <button type="button" id="changeEmailBtn" class="hidden px-4 py-2 text-sm font-semibold text-slate-500 border border-slate-200 rounded-xl hover:text-slate-700 hover:border-slate-300 whitespace-nowrap">이메일 변경</button>
          </c:if>
        </div>

        <c:if test="${not oauthSignUp}">
          <div id="sendErrorStatus" class="mt-2 text-xs text-red-600 hidden"></div>
          <div id="verifySentBox" class="mt-3 hidden w-full rounded-xl border border-blue-100 bg-blue-50 px-4 py-3 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span id="sendStatus" class="text-sm text-[#2559a3]">인증 메일이 발송되었습니다.</span>
              <button type="button" id="resendBtn" class="text-sm font-semibold text-blue-500 underline hover:text-blue-600">재전송</button>
            </div>
            <button type="button" id="verifyBtn" class="text-sm font-semibold text-white bg-primary-600 px-3 py-1.5 rounded-lg hover:bg-primary-700">인증 확인</button>
          </div>
          <div id="verifySuccessBox" class="mt-3 hidden items-center gap-2 text-green-600 text-sm flex">
            <i data-lucide="check-circle-2" class="w-4 h-4"></i>
            <span id="verifyStatus">인증되었습니다.</span>
          </div>
          <div id="verifyErrorStatus" class="mt-2 text-xs text-red-600 hidden"></div>
          <input type="hidden" id="emailVerified" name="emailVerified" value="false">
          <input type="hidden" id="attemptId" name="attemptId" value="">
        </c:if>
      </div>

      <div>
        <label class="block text-sm font-medium text-slate-700 mb-1.5">휴대전화</label>
        <div class="relative">
          <input type="tel" id="phoneNumber" name="phoneNumber" class="w-full pl-10 px-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-primary-100 focus:border-primary-500 outline-none" placeholder="01000000000" required>
          <i data-lucide="smartphone" class="absolute left-3.5 top-3 text-slate-400 w-[18px] h-[18px]"></i>
        </div>
      </div>

      <c:if test="${oauthSignUp}">
        <input type="hidden" name="oauthSignUp" value="true">
      </c:if>

      <!-- Password (Email Only) -->
      <c:if test="${not oauthSignUp}">
        <div id="password-section">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">비밀번호</label>
            <div class="relative">
              <input type="password" id="rawPassword" name="rawPassword" class="w-full px-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-primary-100 focus:border-primary-500 outline-none" placeholder="영문, 숫자, 특수문자 포함 8자 이상" required>
              <button type="button" onclick="togglePasswordVisibility('rawPassword', this)" class="absolute right-3 top-3 text-slate-400 hover:text-slate-600">
                <i data-lucide="eye" class="w-[18px] h-[18px]"></i>
              </button>
            </div>
            <p class="mt-1 text-xs text-slate-400">영문, 숫자, 특수문자 포함 8자 이상 입력해주세요.</p>
            <p id="passwordRuleStatus" class="mt-2 text-xs hidden"></p>
          </div>
          <div class="mt-4">
            <label class="block text-sm font-medium text-slate-700 mb-1.5">비밀번호 확인</label>
            <input type="password" id="passwordConfirm" class="w-full px-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-primary-100 focus:border-primary-500 outline-none" placeholder="비밀번호 재입력" required>
            <p id="passwordMatchStatus" class="mt-2 text-xs hidden"></p>
          </div>
        </div>
      </c:if>

      <button type="submit" class="w-full py-3 bg-primary-500 hover:bg-primary-600 text-white font-bold rounded-xl transition-all shadow-lg shadow-primary-500/30 mt-4">
        가입하기
      </button>
    </form>

    <c:if test="${not oauthSignUp}">
      <button type="button" onclick="goToStep(2)" class="w-full mt-4 text-sm text-slate-400 hover:text-slate-600 underline">이전으로</button>
    </c:if>
  </div>
</main>

<jsp:include page="/WEB-INF/views/auth/include/toast.jsp" />

<script>
    window.APP = {
        contextPath: "${pageContext.request.contextPath}"
    };
</script>
<script src="${pageContext.request.contextPath}/resources/signup/js/signup.js"></script>

</body>
</html>

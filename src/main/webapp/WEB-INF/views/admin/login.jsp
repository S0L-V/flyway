<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Flyway Admin - 로그인</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="bg-[#f1f5f9] flex flex-col items-center justify-center min-h-screen p-4">
<div class="mb-10 text-center">
    <img src="${pageContext.request.contextPath}/resources/images/logo.png"
         alt="Flyway Logo"
         class="h-16 w-auto mx-auto mb-2 drop-shadow-sm">
    <p class="text-sm text-slate-500 font-medium">실시간 항공 통합 관리 포털</p>
</div>

<div class="w-full max-w-md bg-white rounded-[2rem] shadow-2xl p-10 border border-white">
    <div class="mb-10 text-center">
        <h2 class="text-2xl font-bold text-slate-800 mb-2">관리자 로그인</h2>
        <p class="text-slate-500 text-sm">운영 계정 정보를 입력하여 접속하세요.</p>
    </div>

    <!-- 에러 메시지 영역 -->
    <div id="errorMessage" class="hidden mb-6 bg-red-50 border border-red-200 rounded-2xl p-4">
        <div class="flex items-start">
            <i data-lucide="alert-circle" class="text-red-500 w-5 h-5 mr-3 flex-shrink-0 mt-0.5"></i>
            <div>
                <p class="text-sm font-semibold text-red-800">로그인 실패</p>
                <p id="errorText" class="text-sm text-red-600 mt-1"></p>
            </div>
        </div>
    </div>

    <!-- form submit 막고 JavaScript로 처리 -->
    <form id="loginForm" class="space-y-6">
        <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-2 px-1">ID / Email</label>
            <div class="relative">
                <i data-lucide="user" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                <!-- name을 adminId로 변경 -->
                <input type="text"
                       id="adminId"
                       name="adminId"
                       class="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 pl-12 pr-4 text-sm focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 outline-none font-medium"
                       placeholder="admin@flyway.io"
                       required>
            </div>
        </div>

        <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-2 px-1">Password</label>
            <div class="relative">
                <i data-lucide="lock" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                <!-- name을 password로 변경 -->
                <input type="password"
                       id="password"
                       name="password"
                       class="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 pl-12 pr-4 text-sm focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 outline-none font-medium"
                       placeholder="••••••••"
                       required>
            </div>
        </div>

        <!-- 로딩 버튼 -->
        <button type="submit"
                id="loginButton"
                class="w-full bg-slate-900 hover:bg-blue-600 text-white font-bold py-4 rounded-2xl transition-all shadow-xl shadow-slate-900/10 mt-2">
            관리 시스템 접속
        </button>
    </form>
</div>

<script>
    lucide.createIcons();

    // 로그인 처리
    document.getElementById('loginForm').addEventListener('submit', async function(e) {
        e.preventDefault(); // 기본 submit 막기

        const adminId = document.getElementById('adminId').value.trim();
        const password = document.getElementById('password').value;
        const errorDiv = document.getElementById('errorMessage');
        const errorText = document.getElementById('errorText');
        const loginButton = document.getElementById('loginButton');

        // 에러 메시지 숨기기
        errorDiv.classList.add('hidden');

        // 로딩 상태
        loginButton.disabled = true;
        loginButton.innerHTML = '<span class="animate-pulse">로그인 중...</span>';

        try {
            // AJAX POST 요청
            const response = await fetch('${pageContext.request.contextPath}/admin/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: adminId,
                    password: password
                })
            });

            const data = await response.json();

            if (response.ok) {
                // 성공: dashboard로 리다이렉트
                window.location.href = '${pageContext.request.contextPath}/admin/dashboard';
            } else {
                // 실패: 에러 메시지 표시
                errorDiv.classList.remove('hidden');
                errorText.textContent = data.message || '로그인에 실패했습니다.';

                // 아이콘 재생성
                lucide.createIcons();
            }
        } catch (error) {
            // 네트워크 에러
            errorDiv.classList.remove('hidden');
            errorText.textContent = '서버와 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
            lucide.createIcons();
        } finally {
            // 로딩 해제
            loginButton.disabled = false;
            loginButton.innerHTML = '관리 시스템 접속';
        }
    });

    // Enter 키 처리
    document.getElementById('password').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            document.getElementById('loginForm').dispatchEvent(new Event('submit'));
        }
    });
</script>
</body>
</html>
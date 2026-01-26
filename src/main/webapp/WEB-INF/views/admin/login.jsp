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
    <img src="${pageContext.request.contextPath}/resources/common/img/logo.svg"
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
                <p id="errorDetails" class="text-sm text-red-700 font-bold mt-2"></p>
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

    // 타이머 변수
    let countdownTimer = null;

    // 로그인 처리
    document.getElementById('loginForm').addEventListener('submit', async function(e) {
        e.preventDefault(); // 기본 submit 막기

        const adminIdInput = document.getElementById('adminId');
        const passwordInput = document.getElementById('password');
        const loginButton = document.getElementById('loginButton');

        const email = adminIdInput.value.trim();
        const password = passwordInput.value;

        const errorDiv = document.getElementById('errorMessage');
        const errorText = document.getElementById('errorText');
        const errorDetails = document.getElementById('errorDetails');


        // 이전 상태 초기화
        errorDiv.classList.add('hidden');
        errorText.textContent = '';
        errorDetails.textContent = '';
        if (countdownTimer) {
            clearInterval(countdownTimer);
            countdownTimer = null;
        }

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
                body: JSON.stringify({ email: email, password: password })
            });

            const result = await response.json();

            if (result.success) {
                // 성공: dashboard로 리다이렉트
                window.location.href = '${pageContext.request.contextPath}/admin/dashboard';
            } else {
                // 실패: 에러 메시지 표시
                errorDiv.classList.remove('hidden');
                errorText.textContent = result.message || '로그인에 실패했습니다.';

                const errorData = result.data;
                if (errorData) {
                    if (errorData.failCount) {
                        errorDetails.textContent = '로그인 실패 횟수: ' + errorData.failCount + '/5회';
                    } else if (errorData.lockedUntil) {
                        // 1. 날짜 파싱 안정성 확보
                        const lockedUntilString = errorData.lockedUntil;
                        const lockedUntil = new Date(lockedUntilString.replace('T', ' '));

                        // 2. 메인 에러 메시지 시간 포맷 변경
                        const timeFormat = { hour: 'numeric', minute: 'numeric', second: 'numeric', hour12: true };
                        const formattedLockTime = lockedUntil.toLocaleTimeString('ko-KR', timeFormat);
                        errorText.textContent = '계정이 잠겼습니다. ' + formattedLockTime + ' 이후 다시 시도해주세요.';

                        // 3. 카운트다운 시작
                        adminIdInput.disabled = true;
                        passwordInput.disabled = true;
                        loginButton.disabled = true;

                        countdownTimer = setInterval(function() {
                            const now = new Date();
                            const diff = Math.round((lockedUntil - now) / 1000);

                            if (diff <= 0) {
                                clearInterval(countdownTimer);
                                errorDetails.textContent = '';
                                adminIdInput.disabled = false;
                                passwordInput.disabled = false;
                                loginButton.disabled = false;
                                errorText.textContent = '계정 잠금이 해제되었습니다. 다시 로그인 해주세요.'
                            } else {
                                const minutes = Math.floor(diff / 60);
                                const seconds = diff % 60;
                                errorDetails.textContent = '남은 시간: ' + minutes + '분 ' + seconds + '초';
                            }
                        }, 1000);
                    }
                }
                // 아이콘 재생성
                lucide.createIcons();
            }
        } catch (error) {
            // 네트워크 에러
            errorDiv.classList.remove('hidden');
            errorText.textContent = '서버와 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
            lucide.createIcons();
        } finally {
            // 잠금 상태가 아닐 때만 버튼 활성화
            if (!countdownTimer) {
                loginButton.disabled = false;
                loginButton.innerHTML = '관리 시스템 접속';
            }
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
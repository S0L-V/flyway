<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>서버 오류가 발생했습니다 - Flyway</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        * {
            font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
        }

        body {
            background: linear-gradient(135deg, #fee2e2 0%, #fef2f2 50%, #fff5f5 100%);
            min-height: 100vh;
            overflow: hidden;
        }

        /* 연기 애니메이션 */
        .smoke {
            position: absolute;
            width: 20px;
            height: 20px;
            background: rgba(156, 163, 175, 0.6);
            border-radius: 50%;
            animation: smoke-rise 3s ease-out infinite;
        }

        .smoke-1 { left: 45%; animation-delay: 0s; }
        .smoke-2 { left: 48%; animation-delay: 0.5s; }
        .smoke-3 { left: 51%; animation-delay: 1s; }
        .smoke-4 { left: 54%; animation-delay: 1.5s; }

        @keyframes smoke-rise {
            0% {
                opacity: 0.8;
                transform: translateY(0) scale(1);
            }
            100% {
                opacity: 0;
                transform: translateY(-100px) scale(2);
            }
        }

        /* 비행기 추락 효과 */
        .plane-crash {
            animation: plane-fall 4s ease-in-out infinite;
        }

        @keyframes plane-fall {
            0% { transform: rotate(0deg) translateY(0); }
            25% { transform: rotate(15deg) translateY(5px); }
            50% { transform: rotate(-10deg) translateY(-5px); }
            75% { transform: rotate(20deg) translateY(10px); }
            100% { transform: rotate(0deg) translateY(0); }
        }

        /* 경고등 깜빡임 */
        .warning-blink {
            animation: blink-warning 1s ease-in-out infinite;
        }

        @keyframes blink-warning {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.3; }
        }

        /* 기어 회전 */
        .gear-spin {
            animation: gear-rotate 4s linear infinite;
        }

        .gear-spin-reverse {
            animation: gear-rotate 4s linear infinite reverse;
        }

        @keyframes gear-rotate {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }

        .btn-primary {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
            box-shadow: 0 4px 15px rgba(239, 68, 68, 0.4);
            transition: all 0.3s ease;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 25px rgba(239, 68, 68, 0.5);
        }

        .btn-secondary {
            background: white;
            border: 2px solid #fca5a5;
            transition: all 0.3s ease;
        }

        .btn-secondary:hover {
            border-color: #ef4444;
            background: #fef2f2;
        }
    </style>
</head>
<body class="flex items-center justify-center p-4">
    <!-- 연기 효과 -->
    <div class="smoke smoke-1"></div>
    <div class="smoke smoke-2"></div>
    <div class="smoke smoke-3"></div>
    <div class="smoke smoke-4"></div>

    <div class="text-center z-10 max-w-lg mx-auto">
        <!-- 로고 -->
        <a href="${pageContext.request.contextPath}/" class="inline-block mb-8">
            <img src="${pageContext.request.contextPath}/resources/common/img/logo.svg"
                 alt="Flyway" class="h-10 mx-auto">
        </a>

        <!-- 일러스트 -->
        <div class="relative mb-8">
            <!-- 고장난 비행기 -->
            <div class="relative inline-block">
                <svg class="plane-crash w-28 h-28 text-red-500" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M85 45L55 35L50 10L45 35L15 45L45 50L40 90L50 70L60 90L55 50L85 45Z"
                          fill="currentColor" opacity="0.9"/>
                    <circle cx="50" cy="42" r="3" fill="white"/>
                </svg>

                <!-- 기어 아이콘들 -->
                <div class="absolute -bottom-2 -left-4">
                    <svg class="gear-spin w-8 h-8 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/>
                    </svg>
                </div>
                <div class="absolute -top-2 -right-4">
                    <svg class="gear-spin-reverse w-6 h-6 text-gray-300" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/>
                    </svg>
                </div>
            </div>

            <!-- 경고 아이콘 -->
            <div class="absolute -top-2 left-1/2 transform -translate-x-1/2 -translate-y-full">
                <div class="warning-blink w-12 h-12 bg-red-500 rounded-full flex items-center justify-center shadow-lg">
                    <svg class="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                    </svg>
                </div>
            </div>
        </div>

        <!-- 에러 코드 -->
        <div class="mb-4">
            <span class="text-8xl font-black text-transparent bg-clip-text bg-gradient-to-r from-red-500 to-red-600">
                500
            </span>
        </div>

        <!-- 메시지 -->
        <h1 class="text-2xl font-bold text-gray-800 mb-3">
            기술적 결함이 발생했습니다
        </h1>
        <p class="text-gray-500 mb-8 leading-relaxed">
            서버에 문제가 발생하여 요청을 처리할 수 없습니다.<br>
            잠시 후 다시 시도해 주세요.
        </p>

        <!-- 버튼 -->
        <div class="flex flex-col sm:flex-row gap-3 justify-center">
            <button onclick="location.reload()"
                    class="btn-primary px-8 py-3 rounded-full text-white font-semibold inline-flex items-center justify-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                </svg>
                다시 시도
            </button>
            <a href="${pageContext.request.contextPath}/"
               class="btn-secondary px-8 py-3 rounded-full text-gray-600 font-semibold inline-flex items-center justify-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
                </svg>
                홈으로 가기
            </a>
        </div>

        <!-- 추가 도움 -->
        <p class="mt-8 text-sm text-gray-400">
            문제가 계속되면
            <a href="mailto:support@flyway.io" class="text-red-500 hover:underline">support@flyway.io</a>
            로 문의해 주세요.
        </p>
    </div>
</body>
</html>

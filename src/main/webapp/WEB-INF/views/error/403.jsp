<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>접근 권한이 없습니다 - Flyway</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        * {
            font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
        }

        body {
            background: linear-gradient(135deg, #fef3c7 0%, #fef9c3 50%, #fffbeb 100%);
            min-height: 100vh;
            overflow: hidden;
        }

        /* 경고 테이프 패턴 */
        .warning-tape {
            position: absolute;
            width: 200%;
            height: 30px;
            background: repeating-linear-gradient(
                45deg,
                #fbbf24,
                #fbbf24 20px,
                #1f2937 20px,
                #1f2937 40px
            );
            opacity: 0.3;
        }

        .tape-top {
            top: 10%;
            left: -50%;
            transform: rotate(-5deg);
            animation: tape-scroll 20s linear infinite;
        }

        .tape-bottom {
            bottom: 15%;
            left: -50%;
            transform: rotate(3deg);
            animation: tape-scroll 25s linear infinite reverse;
        }

        @keyframes tape-scroll {
            from { transform: translateX(0) rotate(-5deg); }
            to { transform: translateX(50%) rotate(-5deg); }
        }

        /* 자물쇠 흔들기 */
        .lock-shake {
            animation: shake 0.5s ease-in-out infinite;
        }

        @keyframes shake {
            0%, 100% { transform: rotate(0deg); }
            25% { transform: rotate(-5deg); }
            75% { transform: rotate(5deg); }
        }

        /* 방패 효과 */
        .shield-pulse {
            animation: pulse-ring 2s ease-out infinite;
        }

        @keyframes pulse-ring {
            0% { box-shadow: 0 0 0 0 rgba(251, 191, 36, 0.4); }
            70% { box-shadow: 0 0 0 20px rgba(251, 191, 36, 0); }
            100% { box-shadow: 0 0 0 0 rgba(251, 191, 36, 0); }
        }

        .btn-primary {
            background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
            box-shadow: 0 4px 15px rgba(245, 158, 11, 0.4);
            transition: all 0.3s ease;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 25px rgba(245, 158, 11, 0.5);
        }

        .btn-secondary {
            background: white;
            border: 2px solid #fcd34d;
            transition: all 0.3s ease;
        }

        .btn-secondary:hover {
            border-color: #f59e0b;
            background: #fffbeb;
        }
    </style>
</head>
<body class="flex items-center justify-center p-4">
    <!-- 경고 테이프 -->
    <div class="warning-tape tape-top"></div>
    <div class="warning-tape tape-bottom"></div>

    <div class="text-center z-10 max-w-lg mx-auto">
        <!-- 로고 -->
        <a href="${pageContext.request.contextPath}/" class="inline-block mb-8">
            <img src="${pageContext.request.contextPath}/resources/common/img/logo.svg"
                 alt="Flyway" class="h-10 mx-auto">
        </a>

        <!-- 일러스트 -->
        <div class="relative mb-8">
            <!-- 방패 + 자물쇠 -->
            <div class="w-32 h-32 mx-auto bg-gradient-to-br from-amber-400 to-amber-500 rounded-full
                        flex items-center justify-center shield-pulse">
                <svg class="w-16 h-16 text-white lock-shake" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/>
                    <path d="M11 7h2v2h-2zm0 4h2v4h-2z"/>
                </svg>
            </div>

            <!-- 경고 아이콘 -->
            <div class="absolute -top-1 -right-1 w-10 h-10 bg-red-500 rounded-full flex items-center justify-center
                        border-4 border-white shadow-lg">
                <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
                </svg>
            </div>
        </div>

        <!-- 에러 코드 -->
        <div class="mb-4">
            <span class="text-8xl font-black text-transparent bg-clip-text bg-gradient-to-r from-amber-500 to-orange-500">
                403
            </span>
        </div>

        <!-- 메시지 -->
        <h1 class="text-2xl font-bold text-gray-800 mb-3">
            탑승이 거부되었습니다
        </h1>
        <p class="text-gray-500 mb-8 leading-relaxed">
            이 페이지에 접근할 권한이 없습니다.<br>
            로그인이 필요하거나 접근이 제한된 구역입니다.
        </p>

        <!-- 버튼 -->
        <div class="flex flex-col sm:flex-row gap-3 justify-center">
            <a href="${pageContext.request.contextPath}/login"
               class="btn-primary px-8 py-3 rounded-full text-white font-semibold inline-flex items-center justify-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/>
                </svg>
                로그인하기
            </a>
            <a href="${pageContext.request.contextPath}/"
               class="btn-secondary px-8 py-3 rounded-full text-gray-600 font-semibold inline-flex items-center justify-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
                </svg>
                홈으로 가기
            </a>
        </div>
    </div>
</body>
</html>

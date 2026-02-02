<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>페이지를 찾을 수 없습니다 - Flyway</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        * {
            font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
        }

        body {
            background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 50%, #f0f9ff 100%);
            min-height: 100vh;
            overflow: hidden;
        }

        /* 구름 애니메이션 */
        .cloud {
            position: absolute;
            background: white;
            border-radius: 100px;
            opacity: 0.8;
            animation: float-cloud 20s linear infinite;
        }

        .cloud::before, .cloud::after {
            content: '';
            position: absolute;
            background: white;
            border-radius: 50%;
        }

        .cloud-1 {
            width: 120px;
            height: 40px;
            top: 15%;
            left: -150px;
            animation-duration: 25s;
        }
        .cloud-1::before { width: 50px; height: 50px; top: -25px; left: 15px; }
        .cloud-1::after { width: 70px; height: 70px; top: -35px; left: 45px; }

        .cloud-2 {
            width: 100px;
            height: 35px;
            top: 30%;
            left: -120px;
            animation-duration: 30s;
            animation-delay: 5s;
        }
        .cloud-2::before { width: 45px; height: 45px; top: -22px; left: 12px; }
        .cloud-2::after { width: 60px; height: 60px; top: -30px; left: 38px; }

        .cloud-3 {
            width: 80px;
            height: 30px;
            top: 60%;
            left: -100px;
            animation-duration: 22s;
            animation-delay: 10s;
        }
        .cloud-3::before { width: 35px; height: 35px; top: -18px; left: 10px; }
        .cloud-3::after { width: 50px; height: 50px; top: -25px; left: 30px; }

        @keyframes float-cloud {
            from { transform: translateX(0); }
            to { transform: translateX(calc(100vw + 200px)); }
        }

        /* 비행기 애니메이션 - 길 잃은 느낌 */
        .plane-lost {
            animation: plane-wander 8s ease-in-out infinite;
        }

        @keyframes plane-wander {
            0% { transform: translate(0, 0) rotate(-15deg); }
            25% { transform: translate(20px, -30px) rotate(5deg); }
            50% { transform: translate(-10px, 10px) rotate(-25deg); }
            75% { transform: translate(30px, 20px) rotate(10deg); }
            100% { transform: translate(0, 0) rotate(-15deg); }
        }

        /* 물음표 떠다니기 */
        .question-float {
            animation: question-bounce 2s ease-in-out infinite;
        }

        @keyframes question-bounce {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-10px); }
        }

        /* 버튼 호버 효과 */
        .btn-primary {
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
            box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
            transition: all 0.3s ease;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 25px rgba(59, 130, 246, 0.5);
        }

        .btn-secondary {
            background: white;
            border: 2px solid #e2e8f0;
            transition: all 0.3s ease;
        }

        .btn-secondary:hover {
            border-color: #3b82f6;
            background: #f0f9ff;
        }
    </style>
</head>
<body class="flex items-center justify-center p-4">
    <!-- 구름들 -->
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>

    <div class="text-center z-10 max-w-lg mx-auto">
        <!-- 로고 -->
        <a href="${pageContext.request.contextPath}/" class="inline-block mb-8">
            <img src="${pageContext.request.contextPath}/resources/common/img/logo.svg"
                 alt="Flyway" class="h-10 mx-auto">
        </a>

        <!-- 일러스트 -->
        <div class="relative mb-8">
            <!-- 비행기 SVG -->
            <svg class="plane-lost w-32 h-32 mx-auto text-blue-500" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M85 45L55 35L50 10L45 35L15 45L45 50L40 90L50 70L60 90L55 50L85 45Z"
                      fill="currentColor" opacity="0.9"/>
                <circle cx="50" cy="42" r="3" fill="white"/>
            </svg>

            <!-- 물음표들 -->
            <div class="absolute -top-2 -right-4 question-float" style="animation-delay: 0s;">
                <span class="text-3xl font-bold text-blue-400">?</span>
            </div>
            <div class="absolute top-8 -left-2 question-float" style="animation-delay: 0.5s;">
                <span class="text-2xl font-bold text-blue-300">?</span>
            </div>
            <div class="absolute -bottom-2 right-4 question-float" style="animation-delay: 1s;">
                <span class="text-xl font-bold text-blue-200">?</span>
            </div>
        </div>

        <!-- 에러 코드 -->
        <div class="mb-4">
            <span class="text-8xl font-black text-transparent bg-clip-text bg-gradient-to-r from-blue-500 to-blue-600">
                404
            </span>
        </div>

        <!-- 메시지 -->
        <h1 class="text-2xl font-bold text-gray-800 mb-3">
            목적지를 찾을 수 없어요
        </h1>
        <p class="text-gray-500 mb-8 leading-relaxed">
            요청하신 페이지가 이동했거나 존재하지 않습니다.<br>
            주소를 다시 확인해 주세요.
        </p>

        <!-- 버튼 -->
        <div class="flex flex-col sm:flex-row gap-3 justify-center">
            <a href="${pageContext.request.contextPath}/"
               class="btn-primary px-8 py-3 rounded-full text-white font-semibold inline-flex items-center justify-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
                </svg>
                홈으로 돌아가기
            </a>
            <button onclick="history.back()"
                    class="btn-secondary px-8 py-3 rounded-full text-gray-600 font-semibold inline-flex items-center justify-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
                </svg>
                이전 페이지
            </button>
        </div>
    </div>
</body>
</html>

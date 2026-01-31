<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Flyway Admin - Dashboard</title>

    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>

    <!-- Lucide Icons -->
    <script src="https://unpkg.com/lucide@latest"></script>

    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Flatpickr (달력) -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/themes/dark.css">
    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <script src="https://cdn.jsdelivr.net/npm/flatpickr/dist/l10n/ko.js"></script>

    <!-- SockJS (WebSocket 폴백) -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>

    <!-- Custom Admin CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/admin/admin.css">

    <!-- Custom Admin JS (defer로 body 로드 후 실행) -->
    <script src="${pageContext.request.contextPath}/resources/admin/admin.js" defer></script>

    <!-- WebSocket & Dashboard JS -->
    <script src="${pageContext.request.contextPath}/resources/admin/admin-websocket.js" defer></script>
    <script src="${pageContext.request.contextPath}/resources/admin/admin-dashboard.js" defer></script>

    <!-- Context Path 전달 -->
    <script>
        window.CONTEXT_PATH = '${pageContext.request.contextPath}';
    </script>
</head>
<body class="bg-[#f1f5f9]">
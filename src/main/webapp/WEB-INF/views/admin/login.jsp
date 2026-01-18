
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

    <form action="login.do" method="POST" class="space-y-6">
        <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-2 px-1">ID / Email</label>
            <div class="relative">
                <i data-lucide="user" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                <input type="text" name="id" class="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 pl-12 pr-4 text-sm focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 outline-none font-medium" placeholder="admin@flyway.io" required>
            </div>
        </div>

        <div>
            <label class="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-2 px-1">Password</label>
            <div class="relative">
                <i data-lucide="lock" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                <input type="password" name="pw" class="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 pl-12 pr-4 text-sm focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 outline-none font-medium" placeholder="••••••••" required>
            </div>
        </div>

        <button type="submit" class="w-full bg-slate-900 hover:bg-blue-600 text-white font-bold py-4 rounded-2xl transition-all shadow-xl shadow-slate-900/10 mt-2">
            관리 시스템 접속
        </button>
    </form>
</div>

<script>lucide.createIcons();</script>
</body>
</html>

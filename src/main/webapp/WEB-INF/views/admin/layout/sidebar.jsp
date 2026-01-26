<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="w-64 bg-[#1a1f26] text-slate-300 flex flex-col h-screen fixed left-0 top-0 z-50 shadow-2xl">
    <div class="p-8 flex items-center justify-center border-b border-slate-700/30">
        <a href="${pageContext.request.contextPath}/admin/dashboard">
            <img src="${pageContext.request.contextPath}/resources/search/img/logo.svg"
                 alt="Flyway Admin"
                 class="h-10 w-auto brightness-0 invert opacity-90 hover:opacity-100 transition-opacity">
        </a>
    </div>

    <nav class="flex-1 overflow-y-auto px-4 py-8 space-y-8">
        <div>
            <p class="text-[10px] font-bold text-slate-500 tracking-wider mb-4 px-2 uppercase">주요 메뉴</p>
            <div class="space-y-1">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all hover:bg-white/5 hover:text-white">
                    <i data-lucide="layout-dashboard" class="w-5 h-5"></i>
                    <span class="text-sm font-medium">대시보드</span>
                </a>
                <a href="javascript:void(0)" onclick="alert('준비 중입니다.')" class="nav-link flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all hover:bg-white/5 hover:text-white">
                    <i data-lucide="plane" class="w-5 h-5"></i>
                    <span class="text-sm font-medium">항공편 관리</span>
                </a>
                <a href="javascript:void(0)" onclick="alert('준비 중입니다.')" class="nav-link flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all hover:bg-white/5 hover:text-white">
                    <i data-lucide="users" class="w-5 h-5"></i>
                    <span class="text-sm font-medium">회원 관리</span>
                </a>
            </div>
        </div>

        <div>
            <p class="text-[10px] font-bold text-slate-500 tracking-wider mb-4 px-2 uppercase">시스템</p>
            <div class="space-y-1">
                <a href="javascript:void(0)" onclick="alert('준비 중입니다.')" class="nav-link flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all hover:bg-white/5 hover:text-white">
                    <i data-lucide="scroll-text" class="w-5 h-5"></i>
                    <span class="text-sm font-medium">활동 로그</span>
                </a>
                <form id="logout-form" action="${pageContext.request.contextPath}/admin/logout" method="POST" style="display: inline;">
                    <button type="submit" class="flex items-center gap-3 px-3 py-3 text-sm text-slate-400 hover:text-white rounded-xl transition-all w-full text-left" onclick="return confirm('로그아웃 하시겠습니까?')">
                        <i data-lucide="log-out" class="w-5 h-5"></i>
                        <span>로그아웃</span>
                    </button>
                </form>
            </div>
        </div>
    </nav>
</div>

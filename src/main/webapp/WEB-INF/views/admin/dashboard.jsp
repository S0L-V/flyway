
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<main class="pl-64 pt-16 min-h-screen">
    <div class="p-8 max-w-[1600px] mx-auto space-y-8">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-slate-900">Flyway 관리 현황</h1>
                <p class="text-slate-500">실시간 항공권 예약 및 시스템 지표입니다.</p>
            </div>
            <button class="px-4 py-2 bg-blue-600 text-white text-sm font-bold rounded-lg shadow-lg shadow-blue-500/20">
                데이터 새로고침
            </button>
        </div>

        <!-- 통계 카드 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">일일 방문자</span>
                    <div class="p-2 bg-blue-50 text-blue-600 rounded-lg"><i data-lucide="users" class="w-5 h-5"></i></div>
                </div>
                <div class="text-2xl font-bold text-slate-900">24,532</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">예약 건수</span>
                    <div class="p-2 bg-orange-50 text-orange-600 rounded-lg"><i data-lucide="ticket" class="w-5 h-5"></i></div>
                </div>
                <div class="text-2xl font-bold text-slate-900">1,204</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">취소/환불</span>
                    <div class="p-2 bg-rose-50 text-rose-600 rounded-lg"><i data-lucide="alert-circle" class="w-5 h-5"></i></div>
                </div>
                <div class="text-2xl font-bold text-slate-900">12</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">오늘의 매출</span>
                    <div class="p-2 bg-emerald-50 text-emerald-600 rounded-lg"><i data-lucide="credit-card" class="w-5 h-5"></i></div>
                </div>
                <div class="text-2xl font-bold text-slate-900">₩ 1.2억</div>
            </div>
        </div>

        <div class="bg-white p-12 rounded-2xl border border-slate-200 text-center">
            <i data-lucide="bar-chart-3" class="w-12 h-12 text-slate-200 mx-auto mb-4"></i>
            <h2 class="text-lg font-bold text-slate-800">통계 차트 준비 중</h2>
            <p class="text-slate-500 text-sm mt-1">Chart.js 라이브러리를 추가하여 여기에 그래프를 렌더링하세요.</p>
        </div>
    </div>
</main>
</body>
</html>

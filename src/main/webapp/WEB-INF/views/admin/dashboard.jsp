
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
            <button id="refresh-button" class="px-4 py-2 bg-blue-600 text-white text-sm font-bold rounded-lg shadow-lg shadow-blue-500/20 hover:bg-blue-700 transition-colors">
                데이터 새로고침
            </button>
        </div>

        <!-- 오늘 통계 카드 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">일일 방문자</span>
                    <div class="p-2 bg-blue-50 text-blue-600 rounded-lg"><i data-lucide="users" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-daily-visitors" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">예약 건수</span>
                    <div class="p-2 bg-orange-50 text-orange-600 rounded-lg"><i data-lucide="ticket" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-daily-reservations" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">취소/환불</span>
                    <div class="p-2 bg-rose-50 text-rose-600 rounded-lg"><i data-lucide="alert-circle" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-daily-cancellations" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">오늘의 매출</span>
                    <div class="p-2 bg-emerald-50 text-emerald-600 rounded-lg"><i data-lucide="credit-card" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-daily-revenue" class="text-2xl font-bold text-slate-900">-</div>
            </div>
        </div>

        <!-- 전체 통계 및 실시간 상태 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">결제 완료</span>
                    <div class="p-2 bg-teal-50 text-teal-600 rounded-lg"><i data-lucide="check-circle" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-daily-payments" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">총 회원 수</span>
                    <div class="p-2 bg-indigo-50 text-indigo-600 rounded-lg"><i data-lucide="user-check" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-total-users" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">대기 중 예약</span>
                    <div class="p-2 bg-yellow-50 text-yellow-600 rounded-lg"><i data-lucide="clock" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-pending-reservations" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">운항 중 항공편</span>
                    <div class="p-2 bg-sky-50 text-sky-600 rounded-lg"><i data-lucide="plane" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-active-flights" class="text-2xl font-bold text-slate-900">-</div>
            </div>
        </div>

        <!-- 최근 활동 -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm">
            <div class="flex items-center justify-between p-6 border-b border-slate-100">
                <div>
                    <h2 class="text-lg font-bold text-slate-800">최근 활동</h2>
                    <p class="text-sm text-slate-500">최근 24시간 예약, 결제, 환불 내역</p>
                </div>
                <span class="text-xs text-slate-400">실시간 업데이트</span>
            </div>
            <div id="activity-list" class="divide-y divide-slate-100">
                <div class="text-center text-slate-400 py-12">
                    <i data-lucide="loader-2" class="w-8 h-8 animate-spin mx-auto mb-2"></i>
                    <p>데이터를 불러오는 중...</p>
                </div>
            </div>
        </div>
    </div>
</main>

<!-- Dashboard 초기화 -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        // WebSocket 대시보드 초기화
        if (typeof AdminDashboard !== 'undefined') {
            AdminDashboard.init(window.CONTEXT_PATH || '');
        }
    });
</script>
</body>
</html>

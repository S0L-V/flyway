<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<main class="pl-0 lg:pl-[72px] pt-16 min-h-screen bg-slate-50/50 transition-all duration-300">
    <div class="p-4 sm:p-6 lg:p-8 max-w-[1600px] mx-auto space-y-6">
        <!-- 헤더 -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
                <h1 class="text-xl sm:text-2xl font-bold text-slate-900">회원 관리</h1>
                <p class="text-sm text-slate-500 mt-1">회원 목록 조회 및 상태 관리</p>
            </div>
            <button id="refresh-button" class="inline-flex items-center justify-center px-4 py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-xl shadow-lg shadow-blue-500/25 hover:bg-blue-700 hover:shadow-blue-500/30 transition-all">
                <i data-lucide="refresh-cw" class="w-4 h-4 mr-2"></i>
                <span>새로고침</span>
            </button>
        </div>

        <!-- 회원 통계 카드 -->
        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 sm:gap-4">
            <div class="bg-white p-4 sm:p-5 rounded-xl border border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-slate-500">전체 회원</span>
                    <div class="p-1.5 sm:p-2 bg-blue-50 text-blue-600 rounded-lg">
                        <i data-lucide="users" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-total" class="text-xl sm:text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-4 sm:p-5 rounded-xl border border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-slate-500">활성 회원</span>
                    <div class="p-1.5 sm:p-2 bg-emerald-50 text-emerald-600 rounded-lg">
                        <i data-lucide="user-check" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-active" class="text-xl sm:text-2xl font-bold text-emerald-600">-</div>
            </div>
            <div class="bg-white p-4 sm:p-5 rounded-xl border border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-slate-500">차단 회원</span>
                    <div class="p-1.5 sm:p-2 bg-rose-50 text-rose-600 rounded-lg">
                        <i data-lucide="user-x" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-blocked" class="text-xl sm:text-2xl font-bold text-rose-600">-</div>
            </div>
            <div class="bg-white p-4 sm:p-5 rounded-xl border border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-slate-500">온보딩 중</span>
                    <div class="p-1.5 sm:p-2 bg-amber-50 text-amber-600 rounded-lg">
                        <i data-lucide="user-plus" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-onboarding" class="text-xl sm:text-2xl font-bold text-amber-600">-</div>
            </div>
            <div class="bg-white p-4 sm:p-5 rounded-xl border border-slate-200/80 shadow-sm hover:shadow-md transition-shadow col-span-2 sm:col-span-1">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-slate-500">탈퇴 회원</span>
                    <div class="p-1.5 sm:p-2 bg-slate-100 text-slate-500 rounded-lg">
                        <i data-lucide="user-minus" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-withdrawn" class="text-xl sm:text-2xl font-bold text-slate-500">-</div>
            </div>
        </div>

        <!-- 회원 목록 -->
        <div class="bg-white rounded-xl border border-slate-200/80 shadow-sm overflow-hidden">
            <!-- 검색/필터 영역 -->
            <div class="p-4 sm:p-5 border-b border-slate-100 bg-slate-50/50">
                <div class="flex flex-col sm:flex-row gap-3">
                    <div class="flex-1 relative">
                        <i data-lucide="search" class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2"></i>
                        <input type="text" id="search-keyword" placeholder="이메일 또는 이름으로 검색..."
                               class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all">
                    </div>
                    <div class="flex gap-2">
                        <select id="filter-status" class="px-3 py-2.5 bg-white border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all min-w-[120px]">
                            <option value="">전체 상태</option>
                            <option value="ACTIVE">활성</option>
                            <option value="BLOCKED">차단</option>
                            <option value="ONBOARDING">온보딩</option>
                            <option value="WITHDRAWN">탈퇴</option>
                        </select>
                        <button id="search-button" class="px-4 py-2.5 bg-slate-800 text-white rounded-lg text-sm font-medium hover:bg-slate-900 transition-colors whitespace-nowrap">
                            검색
                        </button>
                    </div>
                </div>
            </div>

            <!-- 테이블 (데스크톱) -->
            <div class="hidden sm:block overflow-x-auto">
                <table class="min-w-full">
                    <thead>
                    <tr class="bg-slate-50 border-b border-slate-100">
                        <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">회원 정보</th>
                        <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">가입 경로</th>
                        <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">예약</th>
                        <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">상태</th>
                        <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">가입일</th>
                        <th class="px-4 py-3 text-center text-xs font-semibold text-slate-500 uppercase tracking-wider">관리</th>
                    </tr>
                    </thead>
                    <tbody id="user-list-body" class="divide-y divide-slate-100">
                    <tr><td colspan="6" class="text-center py-12 text-slate-400">회원 목록을 불러오는 중...</td></tr>
                    </tbody>
                </table>
            </div>

            <!-- 카드 리스트 (모바일) -->
            <div id="user-list-mobile" class="sm:hidden divide-y divide-slate-100">
                <div class="p-4 text-center text-slate-400">회원 목록을 불러오는 중...</div>
            </div>

            <!-- 페이지네이션 -->
            <div id="pagination-controls" class="p-4 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-3 bg-slate-50/30">
                <!-- 페이지네이션 컨트롤 -->
            </div>
        </div>
    </div>
</main>

<!-- 회원 상세 모달 -->
<div id="user-detail-modal" class="fixed inset-0 bg-black/60 backdrop-blur-sm hidden z-50 flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md transform transition-all">
        <div class="p-5 border-b border-slate-100 flex items-center justify-between">
            <h3 class="text-lg font-bold text-slate-800">회원 상세 정보</h3>
            <button id="close-modal-btn" class="p-2 hover:bg-slate-100 rounded-lg transition-colors">
                <i data-lucide="x" class="w-5 h-5 text-slate-400"></i>
            </button>
        </div>
        <div id="modal-content" class="p-5">
            <!-- 모달 내용 -->
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/resources/admin/users.js"></script>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<main class="pl-64 pt-16 min-h-screen">
    <div class="p-8 max-w-[1600px] mx-auto space-y-8">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-slate-900">회원 관리</h1>
                <p class="text-slate-500">회원 목록 조회 및 상태 관리를 수행합니다.</p>
            </div>
            <button id="refresh-button" class="px-4 py-2 bg-blue-600 text-white text-sm font-bold rounded-lg shadow-lg shadow-blue-500/20 hover:bg-blue-700 transition-colors">
                <i data-lucide="refresh-cw" class="w-4 h-4 mr-2 inline-block"></i> 새로고침
            </button>
        </div>

        <!-- 회원 통계 카드 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">전체 회원</span>
                    <div class="p-2 bg-blue-50 text-blue-600 rounded-lg"><i data-lucide="users" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-total" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">활성 회원</span>
                    <div class="p-2 bg-green-50 text-green-600 rounded-lg"><i data-lucide="user-check" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-active" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">차단 회원</span>
                    <div class="p-2 bg-red-50 text-red-600 rounded-lg"><i data-lucide="user-x" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-blocked" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">온보딩 중</span>
                    <div class="p-2 bg-yellow-50 text-yellow-600 rounded-lg"><i data-lucide="user-plus" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-onboarding" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">탈퇴 회원</span>
                    <div class="p-2 bg-slate-100 text-slate-600 rounded-lg"><i data-lucide="user-minus" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-withdrawn" class="text-2xl font-bold text-slate-900">-</div>
            </div>
        </div>

        <!-- 회원 목록 테이블 -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm">
            <div class="p-6 border-b border-slate-100 flex items-center justify-between">
                <h2 class="text-lg font-bold text-slate-800">회원 목록</h2>
                <div class="flex items-center space-x-3">
                    <input type="text" id="search-keyword" placeholder="이메일 또는 이름 검색"
                           class="px-3 py-2 border border-slate-300 rounded-lg text-sm w-64 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none">
                    <select id="filter-status" class="p-2 border border-slate-300 rounded-lg text-sm">
                        <option value="">전체 상태</option>
                        <option value="ACTIVE">활성</option>
                        <option value="BLOCKED">차단</option>
                        <option value="ONBOARDING">온보딩</option>
                        <option value="WITHDRAWN">탈퇴</option>
                    </select>
                    <button id="search-button" class="px-3 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600">
                        <i data-lucide="search" class="w-4 h-4 inline-block mr-1"></i> 검색
                    </button>
                </div>
            </div>
            <div class="p-6">
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-slate-200">
                        <thead class="bg-slate-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">회원 정보</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">가입 경로</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">예약 건수</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">상태</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">가입일</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-semibold text-slate-500 uppercase">관리</th>
                        </tr>
                        </thead>
                        <tbody id="user-list-body" class="bg-white divide-y divide-slate-100">
                        <tr><td colspan="6" class="text-center py-12 text-slate-500">회원 목록을 불러오는 중...</td></tr>
                        </tbody>
                    </table>
                </div>
                <nav id="pagination-controls" class="flex items-center justify-between pt-4">
                    <!-- 페이지네이션 컨트롤이 여기에 렌더링됩니다. -->
                </nav>
            </div>
        </div>
    </div>
</main>

<!-- 회원 상세 모달 -->
<div id="user-detail-modal" class="fixed inset-0 bg-black/50 hidden z-50 flex items-center justify-center">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4">
        <div class="p-6 border-b border-slate-100 flex items-center justify-between">
            <h3 class="text-lg font-bold text-slate-800">회원 상세 정보</h3>
            <button id="close-modal-btn" class="p-2 hover:bg-slate-100 rounded-lg transition-colors">
                <i data-lucide="x" class="w-5 h-5 text-slate-500"></i>
            </button>
        </div>
        <div id="modal-content" class="p-6">
            <!-- 모달 내용이 여기에 렌더링됩니다. -->
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/resources/admin/users.js"></script>
</body>
</html>

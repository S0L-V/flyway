<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<main class="admin-bg pl-0 lg:pl-[72px] pt-16 min-h-screen transition-all duration-300 relative">
    <div class="p-4 sm:p-6 lg:p-8 max-w-[1600px] mx-auto space-y-6 relative z-10">
        <!-- 헤더 -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
                <h1 class="text-xl sm:text-2xl font-bold text-glass-primary">회원 관리</h1>
                <p class="text-sm text-glass-muted mt-1">회원 목록 조회 및 상태 관리</p>
            </div>
            <button id="refresh-button" class="glass-btn inline-flex items-center justify-center px-4 py-2.5 text-white text-sm font-semibold">
                <i data-lucide="refresh-cw" class="w-4 h-4 mr-2"></i>
                <span>새로고침</span>
            </button>
        </div>

        <!-- 회원 통계 카드 -->
        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 sm:gap-4">
            <div class="glass-card p-4 sm:p-5">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-glass-muted">전체 회원</span>
                    <div class="p-1.5 sm:p-2 glass-icon-blue text-blue-400 rounded-lg">
                        <i data-lucide="users" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-total" class="text-xl sm:text-2xl font-bold stat-number">-</div>
            </div>
            <div class="glass-card p-4 sm:p-5">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-glass-muted">활성 회원</span>
                    <div class="p-1.5 sm:p-2 glass-icon-emerald text-emerald-400 rounded-lg">
                        <i data-lucide="user-check" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-active" class="text-xl sm:text-2xl font-bold text-emerald-400">-</div>
            </div>
            <div class="glass-card p-4 sm:p-5">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-glass-muted">차단 회원</span>
                    <div class="p-1.5 sm:p-2 glass-icon-rose text-rose-400 rounded-lg">
                        <i data-lucide="user-x" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-blocked" class="text-xl sm:text-2xl font-bold text-rose-400">-</div>
            </div>
            <div class="glass-card p-4 sm:p-5">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-glass-muted">온보딩 중</span>
                    <div class="p-1.5 sm:p-2 glass-icon-yellow text-yellow-400 rounded-lg">
                        <i data-lucide="user-plus" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-onboarding" class="text-xl sm:text-2xl font-bold text-yellow-400">-</div>
            </div>
            <div class="glass-card p-4 sm:p-5 col-span-2 sm:col-span-1">
                <div class="flex items-center justify-between mb-3">
                    <span class="text-xs sm:text-sm font-medium text-glass-muted">탈퇴 회원</span>
                    <div class="p-1.5 sm:p-2 bg-white/10 text-glass-secondary rounded-lg border border-white/10">
                        <i data-lucide="user-minus" class="w-4 h-4 sm:w-5 sm:h-5"></i>
                    </div>
                </div>
                <div id="stat-withdrawn" class="text-xl sm:text-2xl font-bold text-glass-secondary">-</div>
            </div>
        </div>

        <!-- 회원 목록 -->
        <div class="glass-section overflow-hidden">
            <!-- 검색/필터 영역 -->
            <div class="p-4 sm:p-5 border-b border-white/5">
                <div class="flex flex-col sm:flex-row gap-3">
                    <div class="glass-search flex-1 flex items-center px-3 py-2">
                        <i data-lucide="search" class="w-4 h-4 text-glass-muted mr-2"></i>
                        <input type="text" id="search-keyword" placeholder="이메일 또는 이름으로 검색..."
                               class="w-full bg-transparent border-none text-sm text-glass-primary placeholder:text-glass-muted focus:outline-none">
                    </div>
                    <div class="flex gap-2">
                        <select id="filter-status" class="px-3 py-2.5 bg-white/5 border border-white/10 rounded-lg text-sm text-glass-primary focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500/50 outline-none transition-all min-w-[120px]">
                            <option value="" class="bg-slate-800">전체 상태</option>
                            <option value="ACTIVE" class="bg-slate-800">활성</option>
                            <option value="BLOCKED" class="bg-slate-800">차단</option>
                            <option value="ONBOARDING" class="bg-slate-800">온보딩</option>
                            <option value="WITHDRAWN" class="bg-slate-800">탈퇴</option>
                        </select>
                        <button id="search-button" class="glass-btn px-4 py-2.5 text-white text-sm font-medium whitespace-nowrap">
                            검색
                        </button>
                    </div>
                </div>
            </div>

            <!-- 테이블 (데스크톱) -->
            <div class="hidden sm:block overflow-x-auto">
                <table class="min-w-full">
                    <thead>
                    <tr class="bg-white/5 border-b border-white/5">
                        <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">회원 정보</th>
                        <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">가입 경로</th>
                        <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">예약</th>
                        <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">상태</th>
                        <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">가입일</th>
                        <th class="px-4 py-3 text-center text-xs font-semibold text-glass-muted uppercase tracking-wider">관리</th>
                    </tr>
                    </thead>
                    <tbody id="user-list-body" class="divide-y divide-white/5">
                    <tr><td colspan="6" class="text-center py-12 text-glass-muted">회원 목록을 불러오는 중...</td></tr>
                    </tbody>
                </table>
            </div>

            <!-- 카드 리스트 (모바일) -->
            <div id="user-list-mobile" class="sm:hidden divide-y divide-white/5">
                <div class="p-4 text-center text-glass-muted">회원 목록을 불러오는 중...</div>
            </div>

            <!-- 페이지네이션 -->
            <div id="pagination-controls" class="p-4 border-t border-white/5 flex flex-col sm:flex-row items-center justify-between gap-3 bg-white/3">
                <!-- 페이지네이션 컨트롤 -->
            </div>
        </div>
    </div>
</main>

<!-- 회원 상세 모달 (글래스) -->
<div id="user-detail-modal" class="fixed inset-0 bg-black/60 backdrop-blur-md hidden z-50 flex items-center justify-center p-4">
    <div class="glass-modal w-full max-w-md transform transition-all">
        <div class="p-5 border-b border-white/10 flex items-center justify-between">
            <h3 class="text-lg font-bold text-glass-primary">회원 상세 정보</h3>
            <button id="close-modal-btn" class="p-2 hover:bg-white/10 rounded-lg transition-colors">
                <i data-lucide="x" class="w-5 h-5 text-glass-muted"></i>
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

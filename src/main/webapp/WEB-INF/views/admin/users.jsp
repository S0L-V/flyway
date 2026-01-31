<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<style>
    /* 보기형 토글 (2버튼) */
    #view-toggle .ios-segment-slider {
        width: calc(50% - 2px);
    }
    #view-toggle[data-active="0"] .ios-segment-slider {
        transform: translateX(0);
    }
    #view-toggle[data-active="1"] .ios-segment-slider {
        transform: translateX(100%);
    }
    #view-toggle .ios-segment-btn {
        min-width: 40px;
        padding: 0.4rem 0.6rem;
    }

    .user-card {
        position: relative;
        overflow: hidden;
    }
    .user-card::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 3px;
        background: linear-gradient(90deg, #3b82f6, #8b5cf6);
        opacity: 0;
        transition: opacity 0.3s;
    }
    .user-card:hover::before {
        opacity: 1;
    }
    .user-card:hover {
        transform: translateY(-2px);
    }

    @keyframes cardFadeIn {
        from {
            opacity: 0;
            transform: translateY(10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
    .user-card {
        animation: cardFadeIn 0.3s ease-out forwards;
    }
    .user-card:nth-child(1) { animation-delay: 0.02s; }
    .user-card:nth-child(2) { animation-delay: 0.04s; }
    .user-card:nth-child(3) { animation-delay: 0.06s; }
    .user-card:nth-child(4) { animation-delay: 0.08s; }
    .user-card:nth-child(5) { animation-delay: 0.10s; }
    .user-card:nth-child(6) { animation-delay: 0.12s; }
    .user-card:nth-child(7) { animation-delay: 0.14s; }
    .user-card:nth-child(8) { animation-delay: 0.16s; }
    .user-card:nth-child(9) { animation-delay: 0.18s; }
</style>

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
            <div class="p-6 border-b border-white/5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <h2 class="text-lg font-bold text-glass-primary">회원 목록</h2>
                <div class="flex flex-wrap items-center gap-3">
                    <div class="glass-search flex items-center px-3 py-2">
                        <i data-lucide="search" class="w-4 h-4 text-glass-muted mr-2"></i>
                        <input type="text" id="search-keyword" placeholder="이름, 이메일 검색"
                               class="bg-transparent border-none text-sm text-glass-primary placeholder:text-glass-muted w-40 focus:outline-none">
                    </div>
                    <select id="filter-status" class="px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-sm text-glass-primary focus:outline-none focus:ring-2 focus:ring-blue-500/20">
                        <option value="" class="bg-slate-800">전체 상태</option>
                        <option value="ACTIVE" class="bg-slate-800">활성</option>
                        <option value="BLOCKED" class="bg-slate-800">차단</option>
                        <option value="ONBOARDING" class="bg-slate-800">온보딩</option>
                        <option value="WITHDRAWN" class="bg-slate-800">탈퇴</option>
                    </select>
                    <!-- 보기형 토글 (iOS 스타일) -->
                    <div class="ios-segment-group" id="view-toggle">
                        <div class="ios-segment-slider" id="view-slider"></div>
                        <button type="button" class="ios-segment-btn active" data-view="card" data-index="0" title="카드형">
                            <i data-lucide="layout-grid" class="w-4 h-4"></i>
                        </button>
                        <button type="button" class="ios-segment-btn" data-view="list" data-index="1" title="목록형">
                            <i data-lucide="list" class="w-4 h-4"></i>
                        </button>
                    </div>
                </div>
            </div>

            <div class="p-6">
                <!-- 카드 그리드 -->
                <div id="user-card-grid" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                    <div class="col-span-full flex flex-col items-center justify-center py-16 text-glass-muted">
                        <i data-lucide="loader-2" class="w-8 h-8 animate-spin mb-3"></i>
                        <p>회원 목록을 불러오는 중...</p>
                    </div>
                </div>

                <!-- 목록 테이블 (숨김 상태) -->
                <div id="user-list-table" class="hidden overflow-x-auto">
                    <table class="min-w-full divide-y divide-white/5">
                        <thead class="bg-white/5">
                        <tr>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">회원 정보</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">가입 경로</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">예약</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">상태</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase tracking-wider">가입일</th>
                            <th class="px-4 py-3 text-center text-xs font-semibold text-glass-muted uppercase tracking-wider">관리</th>
                        </tr>
                        </thead>
                        <tbody id="user-list-body" class="divide-y divide-white/5"></tbody>
                    </table>
                </div>

                <!-- 페이지네이션 -->
                <nav id="pagination-controls" class="flex items-center justify-between pt-6 mt-6 border-t border-white/5">
                    <!-- 페이지네이션 컨트롤 -->
                </nav>
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

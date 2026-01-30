<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 모바일 오버레이 배경 -->
<div id="sidebar-overlay" class="fixed inset-0 bg-black/50 z-40 lg:hidden hidden" onclick="toggleSidebar()"></div>

<!-- 사이드바: 모바일 토글 / 데스크톱 호버 확장 -->
<div id="sidebar" class="sidebar-collapsed bg-[#1a1f26] text-slate-300 flex flex-col h-screen fixed left-0 top-0 z-50 shadow-2xl transform -translate-x-full lg:translate-x-0 transition-all duration-300">
    <!-- 로고 영역 -->
    <div class="p-4 flex items-center justify-center border-b border-slate-700/30 h-16 relative">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="flex items-center justify-center">
            <!-- 축소 시: 아이콘 로고 -->
            <img src="${pageContext.request.contextPath}/resources/seat/img/logo-icon.svg"
                 alt="Flyway"
                 class="logo-icon w-8 h-8 brightness-0 invert opacity-90">
            <!-- 확장 시: 전체 로고 -->
            <img src="${pageContext.request.contextPath}/resources/common/img/logo.svg"
                 alt="Flyway"
                 class="logo-full h-8 brightness-0 invert opacity-90">
        </a>
        <!-- 모바일 닫기 버튼 -->
        <button class="lg:hidden absolute right-3 p-2 hover:bg-white/10 rounded-lg" onclick="toggleSidebar()">
            <i data-lucide="x" class="w-5 h-5"></i>
        </button>
    </div>

    <nav class="flex-1 overflow-y-auto px-2 lg:px-3 py-6 space-y-6">
        <div>
            <p class="sidebar-text text-[10px] font-bold text-slate-500 tracking-wider mb-3 px-2 uppercase whitespace-nowrap overflow-hidden">주요 메뉴</p>
            <div class="space-y-1">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all hover:bg-white/5 hover:text-white" title="대시보드">
                    <i data-lucide="layout-dashboard" class="w-5 h-5 flex-shrink-0"></i>
                    <span class="sidebar-text text-sm font-medium whitespace-nowrap overflow-hidden">대시보드</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/promotions" class="nav-link flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all hover:bg-white/5 hover:text-white" title="항공편 및 특가 관리">
                    <i data-lucide="plane" class="w-5 h-5 flex-shrink-0"></i>
                    <span class="sidebar-text text-sm font-medium whitespace-nowrap overflow-hidden">항공편 및 특가</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/users" class="nav-link flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all hover:bg-white/5 hover:text-white" title="회원 관리">
                    <i data-lucide="users" class="w-5 h-5 flex-shrink-0"></i>
                    <span class="sidebar-text text-sm font-medium whitespace-nowrap overflow-hidden">회원 관리</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/payments" class="nav-link flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all hover:bg-white/5 hover:text-white" title="결제 내역">
                    <i data-lucide="credit-card" class="w-5 h-5 flex-shrink-0"></i>
                    <span class="sidebar-text text-sm font-medium whitespace-nowrap overflow-hidden">결제 내역</span>
                </a>
            </div>
        </div>

        <div>
            <p class="sidebar-text text-[10px] font-bold text-slate-500 tracking-wider mb-3 px-2 uppercase whitespace-nowrap overflow-hidden">시스템</p>
            <div class="space-y-1">
                <form id="logout-form" action="${pageContext.request.contextPath}/admin/logout" method="POST" class="w-full">
                    <button type="submit" class="flex items-center gap-3 px-3 py-2.5 text-sm text-slate-400 hover:text-white hover:bg-white/5 rounded-lg transition-all w-full text-left" onclick="return confirm('로그아웃 하시겠습니까?')" title="로그아웃">
                        <i data-lucide="log-out" class="w-5 h-5 flex-shrink-0"></i>
                        <span class="sidebar-text whitespace-nowrap overflow-hidden">로그아웃</span>
                    </button>
                </form>
            </div>
        </div>
    </nav>
</div>

<style>
    /* 데스크톱: 축소 상태 (아이콘만) */
    @media (min-width: 1024px) {
        .sidebar-collapsed {
            width: 72px;
        }
        .sidebar-collapsed .sidebar-text {
            opacity: 0;
            width: 0;
            transition: opacity 0.2s, width 0.2s;
        }
        /* 축소 시: 아이콘 로고만 표시 */
        .sidebar-collapsed .logo-icon {
            display: block;
        }
        .sidebar-collapsed .logo-full {
            display: none;
        }
        /* 데스크톱: 호버 시 확장 */
        .sidebar-collapsed:hover {
            width: 240px;
        }
        .sidebar-collapsed:hover .sidebar-text {
            opacity: 1;
            width: auto;
        }
        /* 확장 시: 전체 로고 표시 */
        .sidebar-collapsed:hover .logo-icon {
            display: none;
        }
        .sidebar-collapsed:hover .logo-full {
            display: block;
        }
    }
    /* 모바일: 전체 너비 + 전체 로고 */
    @media (max-width: 1023px) {
        #sidebar {
            width: 280px;
        }
        #sidebar .sidebar-text {
            opacity: 1;
            width: auto;
        }
        #sidebar .logo-icon {
            display: none;
        }
        #sidebar .logo-full {
            display: block;
        }
    }
</style>

<!-- 사이드바 스크립트 -->
<script>
    // 모바일 토글
    function toggleSidebar() {
        var sidebar = document.getElementById('sidebar');
        var overlay = document.getElementById('sidebar-overlay');

        if (sidebar.classList.contains('-translate-x-full')) {
            sidebar.classList.remove('-translate-x-full');
            sidebar.classList.add('translate-x-0');
            overlay.classList.remove('hidden');
        } else {
            sidebar.classList.add('-translate-x-full');
            sidebar.classList.remove('translate-x-0');
            overlay.classList.add('hidden');
        }
    }
</script>

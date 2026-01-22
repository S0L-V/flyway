<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 fixed top-0 left-64 right-0 z-40">
    <div class="flex items-center gap-6">
        <!-- 검색 -->
        <div class="flex items-center w-80 relative">
            <i data-lucide="search" class="absolute left-3 text-slate-400 w-4 h-4"></i>
            <input type="text" placeholder="검색어를 입력하세요..."
                   class="w-full bg-slate-50 border-none rounded-md py-2 pl-10 pr-4 text-sm focus:ring-2 focus:ring-blue-500/20 outline-none">
        </div>

        <!-- 연결 상태 -->
        <div id="connection-status" class="flex items-center gap-1.5">
            <span class="w-2 h-2 bg-slate-300 rounded-full"></span>
            <span class="text-slate-400 text-xs">연결 중...</span>
        </div>
    </div>

    <div class="flex items-center gap-4">
        <!-- 알림 버튼 및 드롭다운 -->
        <div class="relative" id="notification-container">
            <button id="notification-button" class="relative p-2 text-slate-500 hover:bg-slate-100 rounded-full">
                <i data-lucide="bell" class="w-5 h-5"></i>
                <span id="notification-badge" class="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] flex items-center justify-center text-[10px] font-bold text-white bg-red-500 rounded-full border-2 border-white hidden">0</span>
            </button>

            <!-- 알림 드롭다운 -->
            <div id="notification-dropdown" class="hidden absolute right-0 top-12 w-80 bg-white rounded-xl shadow-xl border border-slate-200 z-50">
                <div class="flex items-center justify-between p-4 border-b border-slate-100">
                    <span class="font-semibold text-slate-800">알림</span>
                    <button onclick="AdminDashboard.markAllAsRead()" class="text-xs text-blue-600 hover:text-blue-700">모두 읽음</button>
                </div>
                <div id="notification-list" class="max-h-80 overflow-y-auto">
                    <div class="text-center text-slate-400 py-8">
                        알림을 불러오는 중...
                    </div>
                </div>
            </div>
        </div>

        <!-- 관리자 정보 -->
        <div class="flex items-center gap-3 ml-2 pl-4 border-l border-slate-200">
            <div class="text-right hidden sm:block">
                <p class="text-sm font-semibold text-slate-700">${sessionScope.adminName != null ? sessionScope.adminName : '관리자'}</p>
                <p class="text-[10px] text-slate-400 uppercase font-extrabold tracking-tight">${sessionScope.role != null ? sessionScope.role : 'ADMIN'}</p>
            </div>
            <div class="w-9 h-9 bg-slate-200 rounded-xl overflow-hidden border-2 border-slate-100">
                <img src="https://picsum.photos/100" alt="profile" class="w-full h-full object-cover">
            </div>
        </div>
    </div>
</header>

<!-- 알림 드롭다운 토글 스크립트 -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const btn = document.getElementById('notification-button');
        const dropdown = document.getElementById('notification-dropdown');
        const container = document.getElementById('notification-container');

        if (btn && dropdown) {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                dropdown.classList.toggle('hidden');
            });

            document.addEventListener('click', function(e) {
                if (!container.contains(e.target)) {
                    dropdown.classList.add('hidden');
                }
            });
        }
    });
</script>
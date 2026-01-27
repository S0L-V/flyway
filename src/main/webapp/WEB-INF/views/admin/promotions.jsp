<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<style>
    /* Add styles for the dropdown similar to search.jsp */
    .filter-dropdown .dropdown-panel { display: none; }
    .filter-dropdown.open .dropdown-panel { display: block; }
</style>

<main class="pl-64 pt-16 min-h-screen">
    <div class="p-8 max-w-[1600px] mx-auto space-y-8">
        <!-- Header -->
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-slate-900">항공편 및 특가 관리</h1>
                <p class="text-slate-500">항공편을 조회, 생성/수정/삭제하고 특가 상품을 생성합니다.</p>
            </div>
            <button id="add-flight-btn" class="px-4 py-2 bg-green-600 text-white text-sm font-bold rounded-lg shadow-lg shadow-green-500/20 hover:bg-green-700 transition-colors">
                <i data-lucide="plus" class="w-4 h-4 mr-2 inline-block"></i> 새 항공편 등록
            </button>
        </div>

        <!-- Top Panel: Flight Management -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm">
            <div class="p-6 border-b border-slate-100 flex items-center justify-between flex-wrap gap-4">
                <h2 class="text-lg font-bold text-slate-800">항공편 목록</h2>
                <div id="flight-filters" class="flex items-center space-x-2">
                    <!-- Departure Airport -->
                    <div class="relative filter-dropdown" data-field="from">
                        <button type="button" class="dropdown-toggle p-2 border border-slate-300 rounded-lg text-sm w-44 text-left flex justify-between items-center">
                            <span data-value>출발 공항</span> <i data-lucide="chevron-down" class="w-4 h-4"></i>
                        </button>
                        <div class="dropdown-panel absolute z-20 w-64 mt-1 bg-white border border-slate-300 rounded-md shadow-lg">
                            <input class="dropdown-search w-full p-2 border-b" type="text" placeholder="공항 검색 (ICN, 인천)" autocomplete="off">
                            <ul class="dropdown-list max-h-60 overflow-y-auto" data-list></ul>
                        </div>
                    </div>
                    <!-- Arrival Airport -->
                    <div class="relative filter-dropdown" data-field="to">
                        <button type="button" class="dropdown-toggle p-2 border border-slate-300 rounded-lg text-sm w-44 text-left flex justify-between items-center">
                            <span data-value>도착 공항</span> <i data-lucide="chevron-down" class="w-4 h-4"></i>
                        </button>
                        <div class="dropdown-panel absolute z-20 w-64 mt-1 bg-white border border-slate-300 rounded-md shadow-lg">
                            <input class="dropdown-search w-full p-2 border-b" type="text" placeholder="공항 검색 (NRT, 나리타)" autocomplete="off">
                            <ul class="dropdown-list max-h-60 overflow-y-auto" data-list></ul>
                        </div>
                    </div>
                    <button id="search-flights-btn" class="px-3 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600">
                        <i data-lucide="search" class="w-4 h-4 inline-block mr-1"></i> 검색
                    </button>
                </div>
            </div>
            <div class="p-6">
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-slate-200">
                        <thead class="bg-slate-50">
                        <tr>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">항공편 번호</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">경로</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">출발 시각</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">관리</th>
                        </tr>
                        </thead>
                        <tbody id="flight-list-body"></tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Bottom Panel: Promotion Management -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm">
            <div class="p-6 border-b border-slate-100"><h2 class="text-lg font-bold text-slate-800">생성된 특가 상품 목록</h2></div>
            <div class="p-6">
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-slate-200">
                        <thead class="bg-slate-50">
                        <tr>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">제목</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">항공편</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">인원</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">할인가(총)</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">상태</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">관리</th>
                        </tr>
                        </thead>
                        <tbody id="promotion-list-body"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</main>

<!-- Modals -->
<%@ include file="include/promotion_modal.jsp" %>
<%@ include file="include/flight_crud_modal.jsp" %>

<script>
    window.CONTEXT_PATH = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/resources/admin/promotions.js"></script>
</body>
</html>

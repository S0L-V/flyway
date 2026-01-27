<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Modal for Flight CRUD -->
<div id="flight-crud-modal" class="fixed inset-0 z-50 hidden bg-black/50 backdrop-blur-sm transition-opacity duration-300">
    <div class="fixed inset-0 flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl transform transition-all duration-300 scale-100">
            <form id="flight-form">
                <input type="hidden" id="flight-crud-id" name="flightId">

                <!-- Header -->
                <div class="p-6 border-b border-slate-200 flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center">
                            <i data-lucide="plane" class="w-5 h-5 text-blue-600"></i>
                        </div>
                        <div>
                            <h2 id="flight-modal-title" class="text-xl font-bold text-slate-900">새 항공편 등록</h2>
                            <p class="text-sm text-slate-500">항공편 정보를 입력해주세요</p>
                        </div>
                    </div>
                    <button type="button" class="modal-close-btn p-2 hover:bg-slate-100 rounded-lg transition-colors">
                        <i data-lucide="x" class="w-5 h-5 text-slate-500"></i>
                    </button>
                </div>

                <!-- Body -->
                <div class="p-6 space-y-6 max-h-[65vh] overflow-y-auto">

                    <!-- Flight Number -->
                    <div class="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl p-4 border border-blue-100">
                        <label for="flightNumber" class="flex items-center gap-2 text-sm font-semibold text-blue-800 mb-2">
                            <i data-lucide="hash" class="w-4 h-4"></i>
                            항공편 번호
                        </label>
                        <input type="text" id="flightNumber" name="flightNumber" required
                               placeholder="예: KE123, OZ456"
                               class="block w-full px-4 py-3 rounded-lg border-0 bg-white shadow-sm ring-1 ring-inset ring-blue-200 placeholder:text-slate-400 focus:ring-2 focus:ring-blue-500 text-lg font-semibold tracking-wider">
                    </div>

                    <!-- Route Section -->
                    <div class="space-y-3">
                        <h3 class="flex items-center gap-2 text-sm font-semibold text-slate-700">
                            <i data-lucide="map-pin" class="w-4 h-4 text-slate-500"></i>
                            노선 정보
                        </h3>
                        <div class="grid grid-cols-2 gap-4">
                            <!-- Departure -->
                            <div class="relative">
                                <label for="departureAirport" class="block text-xs font-medium text-slate-500 mb-1.5">출발 공항</label>
                                <div class="relative">
                                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <i data-lucide="plane-takeoff" class="w-4 h-4 text-emerald-500"></i>
                                    </div>
                                    <input type="text" id="departureAirport" name="departureAirport" required
                                           placeholder="ICN"
                                           maxlength="3"
                                           class="block w-full pl-10 pr-4 py-2.5 rounded-lg border border-slate-200 bg-white shadow-sm placeholder:text-slate-400 focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 uppercase font-medium">
                                </div>
                            </div>
                            <!-- Arrival -->
                            <div class="relative">
                                <label for="arrivalAirport" class="block text-xs font-medium text-slate-500 mb-1.5">도착 공항</label>
                                <div class="relative">
                                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <i data-lucide="plane-landing" class="w-4 h-4 text-rose-500"></i>
                                    </div>
                                    <input type="text" id="arrivalAirport" name="arrivalAirport" required
                                           placeholder="NRT"
                                           maxlength="3"
                                           class="block w-full pl-10 pr-4 py-2.5 rounded-lg border border-slate-200 bg-white shadow-sm placeholder:text-slate-400 focus:border-rose-500 focus:ring-1 focus:ring-rose-500 uppercase font-medium">
                                </div>
                            </div>
                        </div>
                        <!-- Route Type -->
                        <div>
                            <label for="routeType" class="block text-xs font-medium text-slate-500 mb-1.5">노선 타입</label>
                            <div class="grid grid-cols-2 gap-3">
                                <label class="route-type-option relative flex items-center justify-center gap-2 p-3 rounded-lg border-2 border-slate-200 cursor-pointer hover:border-slate-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50">
                                    <input type="radio" name="routeType" value="DOMESTIC" class="sr-only" checked>
                                    <i data-lucide="home" class="w-4 h-4"></i>
                                    <span class="font-medium text-sm">국내선</span>
                                </label>
                                <label class="route-type-option relative flex items-center justify-center gap-2 p-3 rounded-lg border-2 border-slate-200 cursor-pointer hover:border-slate-300 transition-colors has-[:checked]:border-blue-500 has-[:checked]:bg-blue-50">
                                    <input type="radio" name="routeType" value="INTERNATIONAL" class="sr-only">
                                    <i data-lucide="globe" class="w-4 h-4"></i>
                                    <span class="font-medium text-sm">국제선</span>
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- Schedule Section -->
                    <div class="space-y-3">
                        <h3 class="flex items-center gap-2 text-sm font-semibold text-slate-700">
                            <i data-lucide="clock" class="w-4 h-4 text-slate-500"></i>
                            운항 스케줄
                        </h3>
                        <div class="grid grid-cols-2 gap-4">
                            <!-- Departure Time -->
                            <div>
                                <label for="departureTime" class="block text-xs font-medium text-slate-500 mb-1.5">출발 시각</label>
                                <div class="relative">
                                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <i data-lucide="calendar" class="w-4 h-4 text-slate-400"></i>
                                    </div>
                                    <input type="datetime-local" id="departureTime" name="departureTime" required
                                           class="block w-full pl-10 pr-4 py-2.5 rounded-lg border border-slate-200 bg-white shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500">
                                </div>
                            </div>
                            <!-- Arrival Time -->
                            <div>
                                <label for="arrivalTime" class="block text-xs font-medium text-slate-500 mb-1.5">도착 시각</label>
                                <div class="relative">
                                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <i data-lucide="calendar-check" class="w-4 h-4 text-slate-400"></i>
                                    </div>
                                    <input type="datetime-local" id="arrivalTime" name="arrivalTime" required
                                           class="block w-full pl-10 pr-4 py-2.5 rounded-lg border border-slate-200 bg-white shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Terminal Section -->
                    <div class="space-y-3">
                        <h3 class="flex items-center gap-2 text-sm font-semibold text-slate-700">
                            <i data-lucide="building-2" class="w-4 h-4 text-slate-500"></i>
                            터미널 정보
                        </h3>
                        <div class="grid grid-cols-3 gap-3">
                            <label class="terminal-option relative flex items-center justify-center p-3 rounded-lg border-2 border-slate-200 cursor-pointer hover:border-slate-300 transition-colors has-[:checked]:border-violet-500 has-[:checked]:bg-violet-50">
                                <input type="radio" name="terminalNo" value="T1" class="sr-only">
                                <span class="font-bold text-lg">T1</span>
                            </label>
                            <label class="terminal-option relative flex items-center justify-center p-3 rounded-lg border-2 border-slate-200 cursor-pointer hover:border-slate-300 transition-colors has-[:checked]:border-violet-500 has-[:checked]:bg-violet-50">
                                <input type="radio" name="terminalNo" value="T2" class="sr-only">
                                <span class="font-bold text-lg">T2</span>
                            </label>
                            <label class="terminal-option relative flex items-center justify-center p-3 rounded-lg border-2 border-slate-200 cursor-pointer hover:border-slate-300 transition-colors has-[:checked]:border-violet-500 has-[:checked]:bg-violet-50">
                                <input type="radio" name="terminalNo" value="" class="sr-only" checked>
                                <span class="font-medium text-sm text-slate-500">미지정</span>
                            </label>
                        </div>
                    </div>

                </div>

                <!-- Footer -->
                <div class="p-6 bg-slate-50 rounded-b-2xl flex items-center justify-between border-t border-slate-200">
                    <p class="text-xs text-slate-500">
                        <i data-lucide="info" class="w-3 h-3 inline-block mr-1"></i>
                        필수 항목을 모두 입력해주세요
                    </p>
                    <div class="flex gap-3">
                        <button type="button" class="modal-close-btn px-5 py-2.5 rounded-xl bg-white border border-slate-300 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-colors">
                            취소
                        </button>
                        <button type="submit" class="px-6 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 text-white text-sm font-semibold hover:from-blue-700 hover:to-indigo-700 transition-all shadow-lg shadow-blue-500/25 flex items-center gap-2">
                            <i data-lucide="check" class="w-4 h-4"></i>
                            저장하기
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<style>
    /* Radio button styling for route type and terminal */
    .route-type-option:has(:checked),
    .terminal-option:has(:checked) {
        border-color: #3b82f6;
        background-color: #eff6ff;
    }
    .route-type-option:has(:checked) i,
    .terminal-option:has(:checked) span {
        color: #2563eb;
    }

    /* For browsers that don't support :has() */
    @supports not selector(:has(*)) {
        .route-type-option input:checked + i,
        .route-type-option input:checked ~ span,
        .terminal-option input:checked + span {
            color: #2563eb;
        }
    }
</style>

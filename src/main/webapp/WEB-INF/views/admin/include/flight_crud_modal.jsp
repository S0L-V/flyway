<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Modal for Flight CRUD -->
<div id="flight-crud-modal" class="fixed inset-0 z-50 hidden bg-black/60 backdrop-blur-md transition-opacity duration-300">
    <div class="fixed inset-0 flex items-center justify-center p-4">
        <div class="glass-modal w-full max-w-2xl transform transition-all duration-300 scale-100">
            <form id="flight-form">
                <input type="hidden" id="flight-crud-id" name="flightId">

                <!-- Header -->
                <div class="p-6 border-b border-white/5 flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 bg-blue-500/20 rounded-xl flex items-center justify-center border border-blue-500/30">
                            <i data-lucide="plane" class="w-5 h-5 text-blue-400"></i>
                        </div>
                        <div>
                            <h2 id="flight-modal-title" class="text-xl font-bold text-glass-primary">새 항공편 등록</h2>
                            <p class="text-sm text-glass-muted">항공편 정보를 입력해주세요</p>
                        </div>
                    </div>
                    <button type="button" class="modal-close-btn p-2 hover:bg-white/10 rounded-lg transition-colors" aria-label="닫기" title="닫기">
                        <i data-lucide="x" class="w-5 h-5 text-glass-muted"></i>
                    </button>
                </div>

                <!-- Body -->
                <div class="p-6 space-y-6 max-h-[65vh] overflow-y-auto">

                    <!-- Flight Number -->
                    <div class="bg-white/5 rounded-xl p-4 border border-white/10">
                        <label for="flightNumber" class="flex items-center gap-2 text-sm font-semibold text-blue-400 mb-2">
                            <i data-lucide="hash" class="w-4 h-4"></i>
                            항공편 번호
                        </label>
                        <input type="text" id="flightNumber" name="flightNumber" required
                               placeholder="예: KE123, OZ456"
                               class="block w-full px-4 py-3 rounded-lg bg-white/5 border border-white/10 text-glass-primary placeholder:text-white/30 focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/50 focus:bg-white/10 text-lg font-semibold tracking-wider transition-all">
                    </div>

                    <!-- Route Section -->
                    <div class="space-y-3">
                        <h3 class="flex items-center gap-2 text-sm font-semibold text-glass-secondary">
                            <i data-lucide="map-pin" class="w-4 h-4 text-glass-muted"></i>
                            노선 정보
                        </h3>
                        <div class="grid grid-cols-2 gap-4">
                            <!-- Departure -->
                            <div class="relative">
                                <label for="departureAirport" class="block text-xs font-medium text-glass-muted mb-1.5">출발 공항</label>
                                <div class="relative">
                                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <i data-lucide="plane-takeoff" class="w-4 h-4 text-emerald-400"></i>
                                    </div>
                                    <input type="text" id="departureAirport" name="departureAirport" required
                                           placeholder="ICN"
                                           maxlength="3"
                                           class="block w-full pl-10 pr-4 py-2.5 rounded-lg bg-white/5 border border-white/10 text-glass-primary placeholder:text-white/30 focus:border-emerald-500/50 focus:ring-1 focus:ring-emerald-500/50 focus:bg-white/10 uppercase font-medium transition-all">
                                </div>
                            </div>
                            <!-- Arrival -->
                            <div class="relative">
                                <label for="arrivalAirport" class="block text-xs font-medium text-glass-muted mb-1.5">도착 공항</label>
                                <div class="relative">
                                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <i data-lucide="plane-landing" class="w-4 h-4 text-rose-400"></i>
                                    </div>
                                    <input type="text" id="arrivalAirport" name="arrivalAirport" required
                                           placeholder="NRT"
                                           maxlength="3"
                                           class="block w-full pl-10 pr-4 py-2.5 rounded-lg bg-white/5 border border-white/10 text-glass-primary placeholder:text-white/30 focus:border-rose-500/50 focus:ring-1 focus:ring-rose-500/50 focus:bg-white/10 uppercase font-medium transition-all">
                                </div>
                            </div>
                        </div>
                        <!-- Route Type -->
                        <div>
                            <label for="routeType" class="block text-xs font-medium text-glass-muted mb-1.5">노선 타입</label>
                            <div class="grid grid-cols-2 gap-3">
                                <label class="route-type-option relative flex items-center justify-center gap-2 p-3 rounded-lg border border-white/10 bg-white/5 cursor-pointer hover:bg-white/10 transition-all has-[:checked]:border-blue-500/50 has-[:checked]:bg-blue-500/10">
                                    <input type="radio" name="routeType" value="DOMESTIC" class="sr-only" checked>
                                    <i data-lucide="home" class="w-4 h-4 text-glass-secondary"></i>
                                    <span class="font-medium text-sm text-glass-secondary">국내선</span>
                                </label>
                                <label class="route-type-option relative flex items-center justify-center gap-2 p-3 rounded-lg border border-white/10 bg-white/5 cursor-pointer hover:bg-white/10 transition-all has-[:checked]:border-blue-500/50 has-[:checked]:bg-blue-500/10">
                                    <input type="radio" name="routeType" value="INTERNATIONAL" class="sr-only">
                                    <i data-lucide="globe" class="w-4 h-4 text-glass-secondary"></i>
                                    <span class="font-medium text-sm text-glass-secondary">국제선</span>
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- Schedule Section -->
                    <div class="space-y-3">
                        <h3 class="flex items-center gap-2 text-sm font-semibold text-glass-secondary">
                            <i data-lucide="clock" class="w-4 h-4 text-glass-muted"></i>
                            운항 스케줄
                        </h3>
                        <div class="grid grid-cols-2 gap-4">
                            <!-- Departure Time -->
                            <div>
                                <label for="departureTime" class="block text-xs font-medium text-glass-muted mb-1.5">출발 일시</label>
                                <input type="text" id="departureTime" name="departureTime" required readonly
                                       placeholder="날짜 및 시간 선택"
                                       class="flight-datetime-picker block w-full px-4 py-2.5 rounded-lg bg-white/5 border border-white/10 text-glass-primary placeholder:text-white/30 focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/50 focus:bg-white/10 transition-all cursor-pointer">
                            </div>
                            <!-- Arrival Time -->
                            <div>
                                <label for="arrivalTime" class="block text-xs font-medium text-glass-muted mb-1.5">도착 일시</label>
                                <input type="text" id="arrivalTime" name="arrivalTime" required readonly
                                       placeholder="날짜 및 시간 선택"
                                       class="flight-datetime-picker block w-full px-4 py-2.5 rounded-lg bg-white/5 border border-white/10 text-glass-primary placeholder:text-white/30 focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/50 focus:bg-white/10 transition-all cursor-pointer">
                            </div>
                        </div>
                    </div>

                    <!-- Terminal Section -->
                    <div class="space-y-3">
                        <h3 class="flex items-center gap-2 text-sm font-semibold text-glass-secondary">
                            <i data-lucide="building-2" class="w-4 h-4 text-glass-muted"></i>
                            터미널 정보
                        </h3>
                        <div class="grid grid-cols-3 gap-3">
                            <label class="terminal-option relative flex items-center justify-center p-3 rounded-lg border border-white/10 bg-white/5 cursor-pointer hover:bg-white/10 transition-all has-[:checked]:border-violet-500/50 has-[:checked]:bg-violet-500/10">
                                <input type="radio" name="terminalNo" value="T1" class="sr-only">
                                <span class="font-bold text-lg text-glass-secondary">T1</span>
                            </label>
                            <label class="terminal-option relative flex items-center justify-center p-3 rounded-lg border border-white/10 bg-white/5 cursor-pointer hover:bg-white/10 transition-all has-[:checked]:border-violet-500/50 has-[:checked]:bg-violet-500/10">
                                <input type="radio" name="terminalNo" value="T2" class="sr-only">
                                <span class="font-bold text-lg text-glass-secondary">T2</span>
                            </label>
                            <label class="terminal-option relative flex items-center justify-center p-3 rounded-lg border border-white/10 bg-white/5 cursor-pointer hover:bg-white/10 transition-all has-[:checked]:border-violet-500/50 has-[:checked]:bg-violet-500/10">
                                <input type="radio" name="terminalNo" value="" class="sr-only" checked>
                                <span class="font-medium text-sm text-glass-muted">미지정</span>
                            </label>
                        </div>
                    </div>

                </div>

                <!-- Footer -->
                <div class="p-6 bg-white/3 rounded-b-2xl flex items-center justify-between border-t border-white/5">
                    <p class="text-xs text-glass-muted">
                        <i data-lucide="info" class="w-3 h-3 inline-block mr-1"></i>
                        필수 항목을 모두 입력해주세요
                    </p>
                    <div class="flex gap-3">
                        <button type="button" class="modal-close-btn px-5 py-2.5 rounded-xl bg-white/5 border border-white/10 text-sm font-semibold text-glass-secondary hover:bg-white/10 transition-all">
                            취소
                        </button>
                        <button type="submit" class="px-6 py-2.5 rounded-xl bg-[#0a84ff] text-white text-sm font-semibold hover:bg-[#409cff] transition-all flex items-center gap-2">
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
    /* Radio button styling */
    .route-type-option:has(:checked),
    .terminal-option:has(:checked) {
        border-color: rgba(59, 130, 246, 0.5);
        background-color: rgba(59, 130, 246, 0.1);
    }
    .route-type-option:has(:checked) i,
    .route-type-option:has(:checked) span,
    .terminal-option:has(:checked) span {
        color: #60a5fa;
    }

    @supports not selector(:has(*)) {
        .route-type-option input:checked + i,
        .route-type-option input:checked ~ span,
        .terminal-option input:checked + span {
            color: #60a5fa;
        }
    }
</style>

<script>
    (function() {
        var departurePicker = null;
        var arrivalPicker = null;

        function initFlightDatePickers() {
            if (typeof flatpickr === 'undefined') return;

            // 기존 인스턴스 제거
            if (departurePicker) departurePicker.destroy();
            if (arrivalPicker) arrivalPicker.destroy();

            // 출발 시간
            departurePicker = flatpickr('#departureTime', {
                locale: 'ko',
                enableTime: true,
                dateFormat: 'Y-m-d H:i',
                minDate: 'today',
                time_24hr: true,
                disableMobile: true,
                onChange: function(selectedDates) {
                    if (selectedDates[0] && arrivalPicker) {
                        arrivalPicker.set('minDate', selectedDates[0]);
                    }
                }
            });

            // 도착 시간
            arrivalPicker = flatpickr('#arrivalTime', {
                locale: 'ko',
                enableTime: true,
                dateFormat: 'Y-m-d H:i',
                minDate: 'today',
                time_24hr: true,
                disableMobile: true
            });
        }

        // 모달 열릴 때 초기화
        var modal = document.getElementById('flight-crud-modal');
        if (modal) {
            var observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                        if (!modal.classList.contains('hidden')) {
                            setTimeout(initFlightDatePickers, 50);
                        }
                    }
                });
            });
            observer.observe(modal, { attributes: true });
        }

        // DOMContentLoaded
        document.addEventListener('DOMContentLoaded', function() {
            if (modal && !modal.classList.contains('hidden')) {
                initFlightDatePickers();
            }
        });
    })();
</script>

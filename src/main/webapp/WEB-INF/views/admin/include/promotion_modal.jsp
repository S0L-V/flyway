<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Modal for Promotion Creation -->
<div id="promotion-modal" class="fixed inset-0 z-50 hidden bg-black/60 backdrop-blur-md transition-opacity duration-300">
    <div class="fixed inset-0 flex items-center justify-center p-4">
        <div class="glass-modal w-full max-w-lg transform transition-all duration-300">
            <form id="promotion-form">
                <input type="hidden" id="flightId" name="flightId" required>

                <!-- Header -->
                <div class="p-6 border-b border-white/5 flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 bg-amber-500/20 rounded-xl flex items-center justify-center border border-amber-500/30">
                            <i data-lucide="sparkles" class="w-5 h-5 text-amber-400"></i>
                        </div>
                        <div>
                            <h2 id="modal-title" class="text-xl font-bold text-glass-primary">특가 상품 만들기</h2>
                            <p class="text-sm text-glass-muted">프로모션 정보를 입력해주세요</p>
                        </div>
                    </div>
                    <button type="button" class="modal-close-btn p-2 hover:bg-white/10 rounded-lg transition-colors">
                        <i data-lucide="x" class="w-5 h-5 text-glass-muted"></i>
                    </button>
                </div>

                <!-- Body -->
                <div class="p-6 space-y-5">

                    <!-- Target Flight Info -->
                    <div class="bg-white/5 rounded-xl p-4 border border-white/10">
                        <div class="flex items-center gap-2 text-sm font-semibold text-blue-400 mb-2">
                            <i data-lucide="plane" class="w-4 h-4"></i>
                            대상 항공편
                        </div>
                        <p id="modal-flight-info" class="text-lg font-bold text-glass-primary"></p>
                    </div>

                    <!-- Promotion Title -->
                    <div>
                        <label for="title" class="flex items-center gap-2 text-sm font-semibold text-glass-secondary mb-2">
                            <i data-lucide="tag" class="w-4 h-4 text-glass-muted"></i>
                            프로모션 제목
                        </label>
                        <input type="text" id="title" name="title" required
                               placeholder="예: 4인 가족 제주 특가, 여름휴가 오사카 할인"
                               class="block w-full px-4 py-3 rounded-lg bg-white/5 border border-white/10 text-glass-primary placeholder:text-white/30 focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/50 focus:bg-white/10 transition-all">
                    </div>

                    <!-- Passenger Count - Stepper -->
                    <div>
                        <label class="flex items-center gap-2 text-sm font-semibold text-glass-secondary mb-3">
                            <i data-lucide="users" class="w-4 h-4 text-glass-muted"></i>
                            인원수
                        </label>
                        <div class="flex items-center justify-center gap-4">
                            <button type="button" id="passenger-minus" class="w-12 h-12 rounded-xl bg-white/5 border border-white/10 text-glass-secondary hover:bg-white/10 hover:border-white/20 transition-all flex items-center justify-center">
                                <i data-lucide="minus" class="w-5 h-5"></i>
                            </button>
                            <div class="flex items-baseline gap-1">
                                <input type="number" id="passengerCount" name="passengerCount" required
                                       min="1" max="10" value="1" readonly
                                       class="w-16 bg-transparent text-center text-4xl font-bold text-glass-primary focus:outline-none">
                                <span class="text-lg text-glass-muted">명</span>
                            </div>
                            <button type="button" id="passenger-plus" class="w-12 h-12 rounded-xl bg-blue-500/20 border border-blue-500/30 text-blue-400 hover:bg-blue-500/30 transition-all flex items-center justify-center">
                                <i data-lucide="plus" class="w-5 h-5"></i>
                            </button>
                        </div>
                        <!-- Quick Select Chips -->
                        <div class="flex justify-center gap-2 mt-3">
                            <button type="button" data-passenger="1" class="passenger-chip px-3 py-1 rounded-full text-xs font-medium bg-white/10 text-glass-secondary hover:bg-white/15 transition-all">1명</button>
                            <button type="button" data-passenger="2" class="passenger-chip px-3 py-1 rounded-full text-xs font-medium bg-white/10 text-glass-secondary hover:bg-white/15 transition-all">2명</button>
                            <button type="button" data-passenger="4" class="passenger-chip px-3 py-1 rounded-full text-xs font-medium bg-white/10 text-glass-secondary hover:bg-white/15 transition-all">4명</button>
                            <button type="button" data-passenger="6" class="passenger-chip px-3 py-1 rounded-full text-xs font-medium bg-white/10 text-glass-secondary hover:bg-white/15 transition-all">6명</button>
                        </div>
                    </div>

                    <!-- Discount Percentage - Segment Buttons -->
                    <div>
                        <label class="flex items-center gap-2 text-sm font-semibold text-glass-secondary mb-3">
                            <i data-lucide="percent" class="w-4 h-4 text-glass-muted"></i>
                            할인율
                        </label>
                        <input type="hidden" id="discountPercentage" name="discountPercentage" value="10" required>
                        <div class="grid grid-cols-5 gap-2">
                            <button type="button" data-discount="5" class="discount-btn py-3 rounded-xl bg-white/5 border border-white/10 text-sm font-semibold text-glass-secondary hover:bg-white/10 transition-all">
                                5%
                            </button>
                            <button type="button" data-discount="10" class="discount-btn active py-3 rounded-xl bg-blue-500/20 border border-blue-500/40 text-sm font-semibold text-blue-400 transition-all">
                                10%
                            </button>
                            <button type="button" data-discount="20" class="discount-btn py-3 rounded-xl bg-white/5 border border-white/10 text-sm font-semibold text-glass-secondary hover:bg-white/10 transition-all">
                                20%
                            </button>
                            <button type="button" data-discount="30" class="discount-btn py-3 rounded-xl bg-white/5 border border-white/10 text-sm font-semibold text-amber-400 hover:bg-amber-500/10 transition-all">
                                30%
                            </button>
                            <button type="button" data-discount="50" class="discount-btn py-3 rounded-xl bg-white/5 border border-white/10 text-sm font-semibold text-rose-400 hover:bg-rose-500/10 transition-all">
                                50%
                            </button>
                        </div>
                        <!-- Custom Input -->
                        <div class="flex items-center gap-2 mt-3">
                            <span class="text-xs text-glass-muted">직접 입력:</span>
                            <input type="number" id="discountCustom" min="1" max="99" placeholder="15"
                                   class="w-16 px-2 py-1 rounded-lg bg-white/5 border border-white/10 text-sm text-center text-glass-primary placeholder:text-white/30 focus:border-blue-500/50 focus:outline-none transition-all">
                            <span class="text-xs text-glass-muted">%</span>
                        </div>
                    </div>

                    <!-- Tags -->
                    <div>
                        <label for="tags" class="flex items-center gap-2 text-sm font-semibold text-glass-secondary mb-2">
                            <i data-lucide="hash" class="w-4 h-4 text-glass-muted"></i>
                            태그
                            <span class="text-xs font-normal text-glass-muted">(콤마로 구분)</span>
                        </label>
                        <input type="text" id="tags" name="tags"
                               placeholder="예: 가족여행, 얼리버드, 주말특가"
                               class="block w-full px-4 py-3 rounded-lg bg-white/5 border border-white/10 text-glass-primary placeholder:text-white/30 focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/50 focus:bg-white/10 transition-all">
                    </div>

                </div>

                <!-- Footer -->
                <div class="p-6 bg-white/3 rounded-b-2xl flex items-center justify-between border-t border-white/5">
                    <p class="text-xs text-glass-muted">
                        <i data-lucide="info" class="w-3 h-3 inline-block mr-1"></i>
                        할인가는 자동 계산됩니다
                    </p>
                    <div class="flex gap-3">
                        <button type="button" class="modal-close-btn px-5 py-2.5 rounded-xl bg-white/5 border border-white/10 text-sm font-semibold text-glass-secondary hover:bg-white/10 transition-all">
                            취소
                        </button>
                        <button type="submit" class="px-6 py-2.5 rounded-xl bg-amber-500 text-white text-sm font-semibold hover:bg-amber-400 transition-all flex items-center gap-2">
                            <i data-lucide="sparkles" class="w-4 h-4"></i>
                            특가 생성
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

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

                    <!-- Passenger & Discount Grid -->
                    <div class="grid grid-cols-2 gap-4">
                        <!-- Passenger Count -->
                        <div>
                            <label for="passengerCount" class="flex items-center gap-2 text-sm font-semibold text-glass-secondary mb-2">
                                <i data-lucide="users" class="w-4 h-4 text-glass-muted"></i>
                                인원수
                            </label>
                            <div class="relative">
                                <input type="number" id="passengerCount" name="passengerCount" required
                                       min="1" max="10" value="1"
                                       class="block w-full px-4 py-3 rounded-lg bg-white/5 border border-white/10 text-glass-primary focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/50 focus:bg-white/10 text-center text-lg font-semibold transition-all">
                                <span class="absolute right-4 top-1/2 -translate-y-1/2 text-glass-muted text-sm">명</span>
                            </div>
                        </div>

                        <!-- Discount Percentage -->
                        <div>
                            <label for="discountPercentage" class="flex items-center gap-2 text-sm font-semibold text-glass-secondary mb-2">
                                <i data-lucide="percent" class="w-4 h-4 text-glass-muted"></i>
                                할인율
                            </label>
                            <div class="relative">
                                <input type="number" id="discountPercentage" name="discountPercentage" required
                                       min="0" max="100" placeholder="10"
                                       class="block w-full px-4 py-3 rounded-lg bg-white/5 border border-white/10 text-glass-primary placeholder:text-white/30 focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/50 focus:bg-white/10 text-center text-lg font-semibold transition-all">
                                <span class="absolute right-4 top-1/2 -translate-y-1/2 text-glass-muted text-sm">%</span>
                            </div>
                        </div>
                    </div>

                    <!-- Quick Discount Buttons -->
                    <div class="flex gap-2">
                        <button type="button" onclick="document.getElementById('discountPercentage').value='10'" class="flex-1 py-2 px-3 rounded-lg bg-white/5 hover:bg-white/10 border border-white/10 text-sm font-medium text-glass-secondary transition-all">10%</button>
                        <button type="button" onclick="document.getElementById('discountPercentage').value='20'" class="flex-1 py-2 px-3 rounded-lg bg-white/5 hover:bg-white/10 border border-white/10 text-sm font-medium text-glass-secondary transition-all">20%</button>
                        <button type="button" onclick="document.getElementById('discountPercentage').value='30'" class="flex-1 py-2 px-3 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/20 text-sm font-medium text-amber-400 transition-all">30%</button>
                        <button type="button" onclick="document.getElementById('discountPercentage').value='50'" class="flex-1 py-2 px-3 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/20 text-sm font-medium text-rose-400 transition-all">50%</button>
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

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Modal for Promotion Creation -->
<div id="promotion-modal" class="fixed inset-0 z-50 hidden bg-black/50 backdrop-blur-sm transition-opacity duration-300">
    <div class="fixed inset-0 flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg transform transition-all duration-300">
            <form id="promotion-form">
                <input type="hidden" id="flightId" name="flightId" required>

                <!-- Header -->
                <div class="p-6 border-b border-slate-200 flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 bg-gradient-to-br from-amber-400 to-orange-500 rounded-xl flex items-center justify-center">
                            <i data-lucide="sparkles" class="w-5 h-5 text-white"></i>
                        </div>
                        <div>
                            <h2 id="modal-title" class="text-xl font-bold text-slate-900">특가 상품 만들기</h2>
                            <p class="text-sm text-slate-500">프로모션 정보를 입력해주세요</p>
                        </div>
                    </div>
                    <button type="button" class="modal-close-btn p-2 hover:bg-slate-100 rounded-lg transition-colors">
                        <i data-lucide="x" class="w-5 h-5 text-slate-500"></i>
                    </button>
                </div>

                <!-- Body -->
                <div class="p-6 space-y-5">

                    <!-- Target Flight Info -->
                    <div class="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl p-4 border border-blue-100">
                        <div class="flex items-center gap-2 text-sm font-semibold text-blue-800 mb-2">
                            <i data-lucide="plane" class="w-4 h-4"></i>
                            대상 항공편
                        </div>
                        <p id="modal-flight-info" class="text-lg font-bold text-slate-900"></p>
                    </div>

                    <!-- Promotion Title -->
                    <div>
                        <label for="title" class="flex items-center gap-2 text-sm font-semibold text-slate-700 mb-2">
                            <i data-lucide="tag" class="w-4 h-4 text-slate-500"></i>
                            프로모션 제목
                        </label>
                        <input type="text" id="title" name="title" required
                               placeholder="예: 4인 가족 제주 특가, 여름휴가 오사카 할인"
                               class="block w-full px-4 py-3 rounded-lg border border-slate-200 shadow-sm placeholder:text-slate-400 focus:border-blue-500 focus:ring-1 focus:ring-blue-500">
                    </div>

                    <!-- Passenger & Discount Grid -->
                    <div class="grid grid-cols-2 gap-4">
                        <!-- Passenger Count -->
                        <div>
                            <label for="passengerCount" class="flex items-center gap-2 text-sm font-semibold text-slate-700 mb-2">
                                <i data-lucide="users" class="w-4 h-4 text-slate-500"></i>
                                인원수
                            </label>
                            <div class="relative">
                                <input type="number" id="passengerCount" name="passengerCount" required
                                       min="1" max="10" value="1"
                                       class="block w-full px-4 py-3 rounded-lg border border-slate-200 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 text-center text-lg font-semibold">
                                <span class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 text-sm">명</span>
                            </div>
                        </div>

                        <!-- Discount Percentage -->
                        <div>
                            <label for="discountPercentage" class="flex items-center gap-2 text-sm font-semibold text-slate-700 mb-2">
                                <i data-lucide="percent" class="w-4 h-4 text-slate-500"></i>
                                할인율
                            </label>
                            <div class="relative">
                                <input type="number" id="discountPercentage" name="discountPercentage" required
                                       min="0" max="100" placeholder="10"
                                       class="block w-full px-4 py-3 rounded-lg border border-slate-200 shadow-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 text-center text-lg font-semibold">
                                <span class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 text-sm">%</span>
                            </div>
                        </div>
                    </div>

                    <!-- Quick Discount Buttons -->
                    <div class="flex gap-2">
                        <button type="button" onclick="document.getElementById('discountPercentage').value='10'" class="flex-1 py-2 px-3 rounded-lg bg-slate-100 hover:bg-slate-200 text-sm font-medium text-slate-700 transition-colors">10%</button>
                        <button type="button" onclick="document.getElementById('discountPercentage').value='20'" class="flex-1 py-2 px-3 rounded-lg bg-slate-100 hover:bg-slate-200 text-sm font-medium text-slate-700 transition-colors">20%</button>
                        <button type="button" onclick="document.getElementById('discountPercentage').value='30'" class="flex-1 py-2 px-3 rounded-lg bg-amber-100 hover:bg-amber-200 text-sm font-medium text-amber-700 transition-colors">30%</button>
                        <button type="button" onclick="document.getElementById('discountPercentage').value='50'" class="flex-1 py-2 px-3 rounded-lg bg-rose-100 hover:bg-rose-200 text-sm font-medium text-rose-700 transition-colors">50%</button>
                    </div>

                    <!-- Tags -->
                    <div>
                        <label for="tags" class="flex items-center gap-2 text-sm font-semibold text-slate-700 mb-2">
                            <i data-lucide="hash" class="w-4 h-4 text-slate-500"></i>
                            태그
                            <span class="text-xs font-normal text-slate-400">(콤마로 구분)</span>
                        </label>
                        <input type="text" id="tags" name="tags"
                               placeholder="예: 가족여행, 얼리버드, 주말특가"
                               class="block w-full px-4 py-3 rounded-lg border border-slate-200 shadow-sm placeholder:text-slate-400 focus:border-blue-500 focus:ring-1 focus:ring-blue-500">
                    </div>

                </div>

                <!-- Footer -->
                <div class="p-6 bg-slate-50 rounded-b-2xl flex items-center justify-between border-t border-slate-200">
                    <p class="text-xs text-slate-500">
                        <i data-lucide="info" class="w-3 h-3 inline-block mr-1"></i>
                        할인가는 자동 계산됩니다
                    </p>
                    <div class="flex gap-3">
                        <button type="button" class="modal-close-btn px-5 py-2.5 rounded-xl bg-white border border-slate-300 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-colors">
                            취소
                        </button>
                        <button type="submit" class="px-6 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-orange-500 text-white text-sm font-semibold hover:from-amber-600 hover:to-orange-600 transition-all shadow-lg shadow-orange-500/25 flex items-center gap-2">
                            <i data-lucide="sparkles" class="w-4 h-4"></i>
                            특가 생성
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

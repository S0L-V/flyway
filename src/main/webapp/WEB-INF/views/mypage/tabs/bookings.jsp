<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="space-y-6 animate-fade-in">
  <div class="flex items-center justify-between">
    <h3 class="text-lg font-bold text-slate-800 flex items-center gap-2">
      <i data-lucide="ticket" class="text-primary-500 w-[18px] h-[18px]"></i> 예약 목록
    </h3>
  </div>

  <div id="bookingList" class="space-y-6"></div>
  <div id="bookingListEmpty" class="hidden bg-white rounded-xl border border-slate-200 p-5 text-sm text-slate-500">
    예약 내역이 없습니다.
  </div>
  <div id="bookingPagination" class="hidden mt-6 flex items-center justify-center gap-2"></div>
  <template id="bookingItemTemplate">
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md hover:border-primary-400 transition-all duration-200 cursor-pointer group">
      <div class="px-5 py-3 bg-slate-50 border-b border-slate-100 flex justify-between items-center">
        <div class="flex flex-col">
          <span data-field="reservedAt" class="text-sm font-semibold text-slate-700">예약일: -</span>
          <span data-field="reservationNo" class="text-xs text-slate-400 mt-0.5">-</span>
        </div>
        <span data-field="status" class="px-2.5 py-0.5 rounded-full text-xs font-semibold border bg-slate-50 text-slate-700 border-slate-200">-</span>
      </div>
      <div class="p-5 space-y-4">
        <div class="flex flex-col gap-2">
          <div class="flex items-center gap-2 mb-1">
            <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-primary-50 text-primary-600">가는 편</span>
            <span data-field="outDateTime" class="text-sm font-semibold text-slate-700">-</span>
            <div data-field="outAirlineBlock" class="ml-auto flex items-center gap-2 text-xs text-slate-500">
              <img data-field="outAirlineLogo" class="w-5 h-5 object-contain hidden" alt="airline logo" />
              <span data-field="outAirlineName" class="font-medium">-</span>
              <span data-field="outFlightNumber">-</span>
            </div>
          </div>
          <div class="flex items-center gap-4 bg-slate-50 p-3 rounded-lg border border-slate-100">
            <div class="flex flex-col items-center min-w-[70px]">
              <span data-field="outDepartTime" class="text-lg font-bold text-slate-800 leading-none">-</span>
              <span data-field="outDepartAirport" class="text-xs font-semibold text-slate-500 mt-1">-</span>
            </div>
            <div class="flex-1 flex flex-col items-center">
              <span data-field="outDuration" class="text-xs font-medium text-slate-500 mb-1">-</span>
              <div class="w-full h-[1px] bg-slate-300 relative">
                <i data-lucide="plane" class="text-slate-400 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 transform rotate-45 w-3.5 h-3.5"></i>
              </div>
            </div>
            <div class="flex flex-col items-center min-w-[70px]">
              <span data-field="outArrivalTime" class="text-lg font-bold text-slate-800 leading-none">-</span>
              <span data-field="outArrivalAirport" class="text-xs font-semibold text-slate-500 mt-1">-</span>
            </div>
          </div>
        </div>
        <div data-field="inboundBlock" class="flex flex-col gap-2 hidden">
          <div class="flex items-center gap-2 mb-1">
            <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-rose-50 text-rose-600">오는 편</span>
            <span data-field="inDateTime" class="text-sm font-semibold text-slate-700">-</span>
            <div data-field="inAirlineBlock" class="ml-auto flex items-center gap-2 text-xs text-slate-500">
              <img data-field="inAirlineLogo" class="w-5 h-5 object-contain hidden" alt="airline logo" />
              <span data-field="inAirlineName" class="font-medium">-</span>
              <span data-field="inFlightNumber">-</span>
            </div>
          </div>
          <div class="flex items-center gap-4 bg-slate-50 p-3 rounded-lg border border-slate-100">
            <div class="flex flex-col items-center min-w-[70px]">
              <span data-field="inDepartTime" class="text-lg font-bold text-slate-800 leading-none">-</span>
              <span data-field="inDepartAirport" class="text-xs font-semibold text-slate-500 mt-1">-</span>
            </div>
            <div class="flex-1 flex flex-col items-center">
              <span data-field="inDuration" class="text-xs font-medium text-slate-500 mb-1">-</span>
              <div class="w-full h-[1px] bg-slate-300 relative">
                <i data-lucide="plane" class="text-slate-400 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 transform rotate-45 w-3.5 h-3.5"></i>
              </div>
            </div>
            <div class="flex flex-col items-center min-w-[70px]">
              <span data-field="inArrivalTime" class="text-lg font-bold text-slate-800 leading-none">-</span>
              <span data-field="inArrivalAirport" class="text-xs font-semibold text-slate-500 mt-1">-</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
</div>

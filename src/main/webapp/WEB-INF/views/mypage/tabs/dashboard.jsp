<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="space-y-8 animate-fade-in">
  <div class="bg-white rounded-2xl p-8 shadow-sm border border-slate-100 flex items-center justify-between transition-all hover:shadow-md">
    <div class="flex items-center gap-6">
      <div id="dashboardInitials" class="w-20 h-20 bg-primary-50 text-primary-600 rounded-full flex items-center justify-center text-2xl font-bold border-4 border-white shadow-sm">
        -
      </div>
      <div>
        <div class="flex items-center gap-2 mb-1">
          <h2 id="dashboardKrName" class="text-2xl font-bold text-slate-800">-</h2>
        </div>
        <div class="text-slate-500 text-sm flex items-center gap-2">
          <span id="dashboardEmail">-</span>
        </div>
      </div>
    </div>
    <a href="?tab=profile" class="px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm text-slate-600 font-medium hover:text-primary-600 hover:border-primary-500 transition-all flex items-center gap-2">
      <i data-lucide="user" class="w-4 h-4"></i> 프로필 수정
    </a>
  </div>

  <div>
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-bold text-slate-800 flex items-center gap-2">
        <i data-lucide="clock" class="text-primary-500 w-[18px] h-[18px]"></i> 최근 예약 내역
      </h3>
      <a href="${pageContext.request.contextPath}/mypage?tab=bookings" class="text-sm text-slate-500 hover:text-primary-600 font-medium flex items-center transition-colors">
        전체보기 <i data-lucide="chevron-right" class="w-4 h-4"></i>
      </a>
    </div>

    <!-- Mock Recent Booking Item -->
    <div id="recentBookings" class="space-y-3"></div>
    <div id="recentBookingsEmpty" class="hidden bg-white rounded-xl p-5 border border-slate-200 text-sm text-slate-500">
      최근 예약 내역이 없습니다.
    </div>
    <template id="recentBookingTemplate">
      <div class="bg-white rounded-xl p-5 border border-slate-200 shadow-sm cursor-pointer hover:border-primary-400 hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 group flex items-center">
        <div class="flex-1 space-y-3">
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-3">
              <span data-field="status" class="px-2.5 py-0.5 rounded-full text-xs font-semibold border bg-slate-50 text-slate-700 border-slate-200">-</span>
              <span data-field="reservationNo" class="text-xs font-mono text-slate-400">-</span>
            </div>
            <span data-field="reservedAt" class="text-xs font-semibold text-slate-400">-</span>
          </div>
          <div class="flex items-center justify-between gap-4 font-semibold text-slate-800">
            <div class="flex items-center gap-2">
              <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-primary-50 text-primary-600">가는 편</span>
              <span data-field="depAirport">-</span>
              <span data-field="depCity" class="text-slate-400 font-normal text-xs">-</span>
              <i data-lucide="arrow-right" class="text-slate-300 w-3.5 h-3.5"></i>
              <span data-field="arrAirport">-</span>
              <span data-field="arrCity" class="text-slate-400 font-normal text-xs">-</span>
            </div>
            <div class="flex items-center gap-3 text-xs text-slate-500 font-normal">
              <div data-field="outAirlineBlock" class="flex items-center gap-2">
                <img data-field="outAirlineLogo" class="w-4 h-4 object-contain hidden" alt="airline logo" />
                <span data-field="outAirlineName" class="font-medium">-</span>
                <span data-field="outFlightNumber">-</span>
              </div>
              <span data-field="outDepartAt" class="text-xs font-semibold text-slate-500">-</span>
            </div>
          </div>
          <div data-field="inboundRow" class="flex items-center justify-between gap-4 font-semibold text-slate-800 hidden">
            <div class="flex items-center gap-2">
              <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-rose-50 text-rose-600">오는 편</span>
              <span data-field="inDepAirport">-</span>
              <span data-field="inDepCity" class="text-slate-400 font-normal text-xs">-</span>
              <i data-lucide="arrow-right" class="text-slate-300 w-3.5 h-3.5"></i>
              <span data-field="inArrAirport">-</span>
              <span data-field="inArrCity" class="text-slate-400 font-normal text-xs">-</span>
            </div>
            <div class="flex items-center gap-3 text-xs text-slate-500 font-normal">
              <div data-field="inAirlineBlock" class="flex items-center gap-2">
                <img data-field="inAirlineLogo" class="w-4 h-4 object-contain hidden" alt="airline logo" />
                <span data-field="inAirlineName" class="font-medium">-</span>
                <span data-field="inFlightNumber">-</span>
              </div>
              <span data-field="inDepartAt" class="text-xs font-semibold text-slate-500">-</span>
            </div>
          </div>
        </div>
        <div class="pl-6 text-slate-300 group-hover:text-primary-500 transition-colors">
          <i data-lucide="chevron-right" class="w-6 h-6"></i>
        </div>
      </div>
    </template>
  </div>
</div>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="seatModal" class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm hidden animate-fade-in">
  <div class="bg-white rounded-2xl w-full max-w-5xl h-[85vh] flex flex-col shadow-2xl overflow-hidden relative">
    <!-- Header -->
    <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-white z-10">
      <div>
        <h2 class="text-xl font-bold text-slate-900 flex items-center gap-2">
          <i data-lucide="armchair" class="text-primary-500 w-5 h-5"></i> 좌석 선택
        </h2>
        <p class="text-sm text-slate-500 mt-1">원하시는 좌석을 선택해주세요.</p>
      </div>
      <button onclick="toggleSeatModal(false)" class="p-2 hover:bg-slate-100 rounded-full transition-colors"><i data-lucide="x" class="text-slate-400 w-5 h-5"></i></button>
    </div>

    <!-- Body -->
    <div class="flex-1 overflow-hidden flex flex-col md:flex-row">
      <!-- Sidebar -->
      <div class="w-full md:w-80 bg-slate-50 border-r border-slate-200 p-6 flex flex-col gap-6 overflow-y-auto shrink-0 z-10">
        <!-- Tabs -->
        <div class="bg-white p-1.5 rounded-xl border border-slate-200 shadow-sm flex">
          <button id="btn-seg-out" onclick="setSeatSegment('outbound')" class="flex-1 py-2 text-xs font-bold rounded-lg transition-all flex flex-col items-center gap-1 bg-primary-50 text-primary-700 shadow-sm border border-primary-100">
            <span>가는 편</span>
            <span id="seatOutboundRoute" class="font-normal text-[10px] opacity-80">-</span>
          </button>
          <button id="btn-seg-in" onclick="setSeatSegment('inbound')" class="flex-1 py-2 text-xs font-bold rounded-lg transition-all flex flex-col items-center gap-1 text-slate-400 hover:bg-slate-50">
            <span>오는 편</span>
            <span id="seatInboundRoute" class="font-normal text-[10px] opacity-80">-</span>
          </button>
        </div>

        <div class="space-y-3">
          <h4 class="text-xs font-bold text-slate-400 uppercase tracking-wider">탑승객</h4>
          <div id="seatModalPassengers" class="space-y-2"></div>
          <div id="seatModalPassengersEmpty" class="hidden text-xs text-slate-500 bg-white border border-slate-200 rounded-xl p-3">
            탑승객 정보가 없습니다.
          </div>
          <template id="seatModalPassengerTemplate">
            <div class="w-full text-left p-3 rounded-xl border bg-white border-primary-500 shadow-md ring-1 ring-primary-100 flex items-center justify-between cursor-pointer">
              <div class="flex items-center gap-3">
                <div data-field="initial" class="w-8 h-8 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center text-xs font-bold">-</div>
                <div data-field="name" class="font-bold text-sm text-slate-900">-</div>
              </div>
              <div data-field="seat" class="px-2 py-1 rounded text-xs font-bold bg-slate-100 text-slate-700">-</div>
            </div>
          </template>
        </div>
      </div>

      <!-- Map -->
      <div class="flex-1 bg-slate-100/50 relative overflow-y-auto flex justify-center py-10">
        <div class="inline-block bg-white px-6 py-10 rounded-[3rem] rounded-b-3xl shadow-xl border border-slate-200 relative h-fit">
          <div class="absolute -top-4 left-1/2 -translate-x-1/2 w-20 h-12 bg-gradient-to-b from-slate-200 to-white rounded-t-[40px] border-t border-x border-slate-200 opacity-60"></div>
          <div class="cabin relative z-10">
            <div class="seat-headers mb-4"><span>A</span><span>B</span><span>C</span><span class="aisle"></span><span>D</span><span>E</span><span>F</span></div>
            <!-- Simplified rows generated via JS loop or JSP loop. Here simplified for demo -->
            <% for(int i=28; i<=35; i++) { %>
            <div class="seat-row gap-x-2 mb-3 !grid-cols-[repeat(7,32px)]">
              <button class="w-[32px] h-[42px] rounded-t-xl border-b-[3px] flex items-center justify-center text-[11px] font-bold bg-white text-slate-500 border-slate-200 hover:border-primary-400 hover:text-primary-600 hover:-translate-y-0.5 transition-all">A</button>
              <button class="w-[32px] h-[42px] rounded-t-xl border-b-[3px] flex items-center justify-center text-[11px] font-bold bg-white text-slate-500 border-slate-200 hover:border-primary-400 hover:text-primary-600 hover:-translate-y-0.5 transition-all">B</button>
              <button class="w-[32px] h-[42px] rounded-t-xl border-b-[3px] flex items-center justify-center text-[11px] font-bold bg-white text-slate-500 border-slate-200 hover:border-primary-400 hover:text-primary-600 hover:-translate-y-0.5 transition-all">C</button>
              <div class="aisle flex items-center justify-center text-[10px] text-slate-300 font-mono"><%= i %></div>
              <button class="w-[32px] h-[42px] rounded-t-xl border-b-[3px] flex items-center justify-center text-[11px] font-bold bg-white text-slate-500 border-slate-200 hover:border-primary-400 hover:text-primary-600 hover:-translate-y-0.5 transition-all">D</button>
              <button class="w-[32px] h-[42px] rounded-t-xl border-b-[3px] flex items-center justify-center text-[11px] font-bold bg-white text-slate-500 border-slate-200 hover:border-primary-400 hover:text-primary-600 hover:-translate-y-0.5 transition-all">E</button>
              <button class="w-[32px] h-[42px] rounded-t-xl border-b-[3px] flex items-center justify-center text-[11px] font-bold bg-white text-slate-500 border-slate-200 hover:border-primary-400 hover:text-primary-600 hover:-translate-y-0.5 transition-all">F</button>
            </div>
            <% } %>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="px-6 py-4 border-t border-slate-100 flex justify-end bg-white z-10">
      <button onclick="toggleSeatModal(false)" class="px-6 py-2.5 bg-slate-800 hover:bg-slate-900 text-white font-bold rounded-xl transition-all shadow-lg shadow-slate-500/20">선택 완료</button>
    </div>
  </div>
</div>

<div class="animate-fade-in space-y-6">
  <div class="flex justify-between items-center pb-4 border-b border-slate-200">
    <div class="flex items-center gap-4">
      <a href="${pageContext.request.contextPath}/mypage?tab=bookings" class="w-8 h-8 rounded-full border border-slate-200 flex items-center justify-center text-slate-500 hover:bg-slate-50 hover:text-primary-600 hover:border-primary-200 transition-all">
        <i data-lucide="chevron-left" class="w-5 h-5"></i>
      </a>
      <h2 class="text-2xl font-bold text-slate-900">예약 상세</h2>
    </div>
    <div class="flex flex-col items-end">
      <div class="flex items-center gap-2">
        <span id="detailStatus" class="px-2.5 py-0.5 rounded-full text-xs font-semibold border bg-slate-50 text-slate-700 border-slate-200">-</span>
        <span id="detailReservedAt" class="text-xs text-slate-400">예약일: -</span>
      </div>
      <span id="detailReservationNo" class="text-xs font-mono font-medium text-slate-500 mt-1">-</span>
    </div>
  </div>

  <!-- Layout Grid: Left (2/3), Right (1/3) -->
  <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <!-- Left Section -->
    <div class="space-y-8 lg:col-span-2">
      <section>
        <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
          <i data-lucide="map-pin" class="text-primary-500 w-[18px] h-[18px]"></i> 여정 정보
        </h3>
        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md transition-all">
          <div class="bg-slate-50 px-6 py-3 border-b border-slate-100 flex justify-between items-center">
            <span class="flex items-center gap-2 text-sm font-semibold text-slate-700">
               <i data-lucide="plane" class="text-primary-500 w-4 h-4"></i> 가는 편
            </span>
            <div class="flex items-center gap-3 text-xs text-slate-500">
              <span id="detailOutboundDate" class="font-medium">-</span>
              <div class="flex items-center gap-2">
                <img id="detailOutboundAirlineLogo" class="w-4 h-4 object-contain hidden" alt="airline logo" />
                <span id="detailOutboundAirlineName" class="font-medium">-</span>
                <span id="detailOutboundFlightNo">-</span>
              </div>
            </div>
          </div>
          <div class="p-6 flex items-center justify-between">
            <div class="flex flex-col items-center min-w-[80px]">
              <span id="detailOutboundDepartTime" class="text-2xl font-light text-slate-900 leading-none mb-2">-</span>
              <span id="detailOutboundDepartAirport" class="text-xl font-bold text-slate-800">-</span>
              <span id="detailOutboundDepartCity" class="text-xs text-slate-500 mt-1">-</span>
              <span id="detailOutboundTerminalNo" class="text-[10px] text-slate-400">-</span>
            </div>
            <div class="flex-1 flex flex-col items-center px-4">
              <span id="detailOutboundDuration" class="text-xs text-slate-400 mb-1">-</span>
              <div class="w-full h-[2px] bg-slate-200 relative flight-path-line">
                <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white px-2">
                  <i data-lucide="plane" class="text-slate-400 transform rotate-45 w-4 h-4"></i>
                </div>
              </div>
            </div>
            <div class="flex flex-col items-center min-w-[80px]">
              <span id="detailOutboundArrivalTime" class="text-2xl font-light text-slate-900 leading-none mb-2">-</span>
              <span id="detailOutboundArrivalAirport" class="text-xl font-bold text-slate-800">-</span>
              <span id="detailOutboundArrivalCity" class="text-xs text-slate-500 mt-1">-</span>
            </div>
          </div>
        </div>
      </section>
      <section id="detailInboundSection" class="hidden">
        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md transition-all">
          <div class="bg-slate-50 px-6 py-3 border-b border-slate-100 flex justify-between items-center">
            <span class="flex items-center gap-2 text-sm font-semibold text-slate-700">
               <i data-lucide="plane" class="text-primary-500 w-4 h-4"></i> 오는 편
            </span>
            <div class="flex items-center gap-3 text-xs text-slate-500">
              <span id="detailInboundDate" class="font-medium">-</span>
              <div class="flex items-center gap-2">
                <img id="detailInboundAirlineLogo" class="w-4 h-4 object-contain hidden" alt="airline logo" />
                <span id="detailInboundAirlineName" class="font-medium">-</span>
                <span id="detailInboundFlightNo">-</span>
              </div>
            </div>
          </div>
          <div class="p-6 flex items-center justify-between">
            <div class="flex flex-col items-center min-w-[80px]">
              <span id="detailInboundDepartTime" class="text-2xl font-light text-slate-900 leading-none mb-2">-</span>
              <span id="detailInboundDepartAirport" class="text-xl font-bold text-slate-800">-</span>
              <span id="detailInboundDepartCity" class="text-xs text-slate-500 mt-1">-</span>
              <span id="detailInboundTerminalNo" class="text-[10px] text-slate-400">-</span>
            </div>
            <div class="flex-1 flex flex-col items-center px-4">
              <span id="detailInboundDuration" class="text-xs text-slate-400 mb-1">-</span>
              <div class="w-full h-[2px] bg-slate-200 relative flight-path-line">
                <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white px-2">
                  <i data-lucide="plane" class="text-slate-400 transform rotate-45 w-4 h-4"></i>
                </div>
              </div>
            </div>
            <div class="flex flex-col items-center min-w-[80px]">
              <span id="detailInboundArrivalTime" class="text-2xl font-light text-slate-900 leading-none mb-2">-</span>
              <span id="detailInboundArrivalAirport" class="text-xl font-bold text-slate-800">-</span>
              <span id="detailInboundArrivalCity" class="text-xs text-slate-500 mt-1">-</span>
            </div>
          </div>
        </div>
      </section>

      <section>
        <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
          <i data-lucide="user" class="text-primary-500 w-[18px] h-[18px]"></i> 탑승객 및 부가서비스
        </h3>
        <div id="passengerInfoList" class="space-y-4"></div>
        <div id="passengerInfoEmpty" class="hidden text-sm text-slate-500 bg-slate-50 rounded-2xl border border-slate-100 p-5">
          탑승객 정보가 없습니다.
        </div>
        <template id="passengerInfoTemplate">
          <article class="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all">
            <div class="flex items-start justify-between gap-4">
              <div class="flex items-center gap-3">
                <div data-field="initial" class="w-12 h-12 rounded-full bg-primary-50 text-primary-700 flex items-center justify-center font-bold text-sm">-</div>
                <div>
                  <div data-field="nameEn" class="text-lg font-bold text-slate-900">-</div>
                  <div data-field="nameKr" class="text-xs text-slate-400 mt-1">-</div>
                </div>
              </div>
              <span data-field="passportStatus" class="px-2.5 py-1 rounded-full text-xs font-semibold border bg-slate-50 text-slate-700 border-slate-200">-</span>
            </div>

            <div class="mt-5 grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <div class="text-xs text-slate-400">성별/생년월일</div>
                <div data-field="genderBirth" class="font-medium text-slate-800 mt-1">-</div>
              </div>
              <div>
                <div class="text-xs text-slate-400">연락처</div>
                <div data-field="phone" class="font-medium text-slate-800 mt-1">-</div>
              </div>
              <div>
                <div class="text-xs text-slate-400">이메일</div>
                <div data-field="email" class="font-medium text-slate-800 mt-1">-</div>
              </div>
              <div data-field="seatRow">
                <div class="text-xs text-slate-400">좌석</div>
                <div data-field="seats" class="font-medium text-slate-800 mt-1">-</div>
              </div>
            </div>

            <div class="mt-5 bg-slate-50 rounded-2xl border border-primary-100 p-5" data-field="passportSection">
              <div class="flex items-center justify-between gap-3 mb-4">
                <div class="flex items-center gap-2 text-sm font-semibold text-primary-700">
                  <i data-lucide="id-card" class="w-4 h-4 text-primary-700"></i>
                  <span>여권 정보</span>
                  <span class="text-xs font-medium text-primary-500">(수정 가능)</span>
                </div>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div>
                  <label class="text-xs text-slate-400">국적</label>
                  <select data-field="passportCountryInput" class="mt-2 w-full h-11 rounded-xl border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary-200">
                    <option value="">선택</option>
                    <option value="KOR">대한민국 (KOR)</option>
                    <option value="USA">미국 (USA)</option>
                    <option value="JPN">일본 (JPN)</option>
                    <option value="CHN">중국 (CHN)</option>
                    <option value="VNM">베트남 (VNM)</option>
                  </select>
                </div>
                <div>
                  <label class="text-xs text-slate-400">발행 국가</label>
                  <select data-field="passportIssueCountryInput" class="mt-2 w-full h-11 rounded-xl border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary-200">
                    <option value="">선택</option>
                    <option value="KOR">대한민국 (KOR)</option>
                    <option value="USA">미국 (USA)</option>
                    <option value="JPN">일본 (JPN)</option>
                    <option value="CHN">중국 (CHN)</option>
                    <option value="VNM">베트남 (VNM)</option>
                  </select>
                </div>
                <div>
                  <label class="text-xs text-slate-400">여권 번호</label>
                  <input data-field="passportNoInput" type="text" placeholder="여권번호" class="mt-2 w-full h-11 rounded-xl border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary-200"/>
                </div>
                <div>
                  <label class="text-xs text-slate-400">만료일</label>
                  <input data-field="passportExpiryInput" type="date" class="mt-2 w-full h-11 rounded-xl border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 focus:outline-none focus:ring-2 focus:ring-primary-200"/>
                </div>
              </div>
              <button data-action="savePassport" class="mt-4 w-full h-11 rounded-xl bg-primary-600 text-white text-sm font-bold shadow-lg shadow-primary-500/20 hover:bg-primary-700 transition-all">
                정보 저장
              </button>
              <div data-field="passportMessage" class="hidden text-xs text-slate-400 mt-2"></div>
            </div>

            <div class="mt-5 grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div class="bg-slate-50 rounded-2xl border border-slate-100 p-4">
                <div class="flex items-center gap-2 text-xs font-semibold text-slate-400">
                  <i data-lucide="briefcase" class="w-4 h-4"></i> 수하물
                </div>
                <div data-field="baggage" class="font-medium text-slate-800 mt-2">-</div>
              </div>
              <div class="bg-slate-50 rounded-2xl border border-slate-100 p-4">
                <div class="flex items-center gap-2 text-xs font-semibold text-slate-400">
                  <i data-lucide="utensils" class="w-4 h-4"></i> 기내식
                </div>
                <div data-field="meal" class="font-medium text-slate-800 mt-2">-</div>
              </div>
            </div>
          </article>
        </template>
      </section>
    </div>

    <!-- Right Section -->
    <div class="relative lg:col-span-1">
      <div class="space-y-6">
        <!-- Seat Info Summary -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-200" id="detailSeatInfoCard">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-bold text-slate-800 flex items-center gap-2">
              <i data-lucide="armchair" class="text-slate-400 w-[18px] h-[18px]"></i> 좌석 정보
            </h3>
            <button onclick="toggleSeatModal(true)" class="px-4 py-2 text-sm font-bold rounded-xl bg-primary-600 text-white shadow-lg shadow-primary-500/30 hover:bg-primary-700 transition-all flex items-center gap-2">
              좌석 선택 <i data-lucide="chevron-right" class="w-4 h-4"></i>
            </button>
          </div>
          <div id="seatSummaryList" class="space-y-3"></div>
          <div id="seatSummaryEmpty" class="hidden text-xs text-slate-500 bg-slate-50 rounded-xl border border-slate-100 p-4">
            좌석 정보가 없습니다.
          </div>
          <template id="seatSummaryTemplate">
            <div class="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-100">
              <div class="flex items-center gap-3">
                <div data-field="initial" class="w-10 h-10 rounded-full bg-white border border-slate-200 flex items-center justify-center font-bold text-slate-600 shadow-sm">-</div>
                <div>
                  <div data-field="name" class="font-bold text-slate-800">-</div>
                  <div class="text-xs text-slate-400">탑승객</div>
                </div>
              </div>
              <div class="flex flex-col items-end">
                <span data-field="segmentLabel" class="text-[10px] text-slate-400 font-bold uppercase tracking-wider">-</span>
                <span data-field="seat" class="text-sm font-bold text-primary-600">-</span>
              </div>
            </div>
          </template>
        </div>

        <!-- Payment Info -->
        <div class="bg-slate-50 rounded-2xl p-6 border border-slate-100">
          <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
            <i data-lucide="credit-card" class="text-slate-400 w-[18px] h-[18px]"></i> 결제 정보
          </h3>
          <div class="space-y-3 text-sm mb-6">
            <div class="flex justify-between"><span class="text-slate-500">결제 수단</span><span id="detailPaymentMethod" class="font-medium text-slate-800">-</span></div>
            <div class="flex justify-between"><span class="text-slate-500">결제 상태</span><span id="detailPaymentStatus" class="font-medium text-slate-800">-</span></div>
            <div class="flex justify-between"><span class="text-slate-500">결제 일시</span><span id="detailPaidAt" class="font-medium text-slate-800">-</span></div>
          </div>
          <div class="bg-white rounded-xl border border-slate-200 p-4 mb-6">
            <div class="text-xs font-semibold text-slate-500 mb-3">결제 금액 상세</div>
            <div id="detailPaymentFareList" class="space-y-2 text-sm"></div>
            <div class="border-t border-dashed border-slate-200 my-3"></div>
            <div id="detailPaymentServiceList" class="space-y-2 text-sm"></div>
          </div>
          <div class="flex justify-between items-center mt-4 pt-2 border-t border-dashed border-slate-300">
            <span class="font-bold text-slate-800">총 결제금액</span>
            <span id="detailPaymentTotalAmount" class="text-xl font-bold text-primary-600 tracking-tight">-</span>
          </div>
          <div class="flex justify-between items-center mt-2">
            <span class="text-sm text-slate-500">실 결제금액</span>
            <span id="detailPaidAmount" class="text-lg font-bold text-primary-600">-</span>
          </div>
          <button id="detailRefundButton" class="mt-4 w-full h-11 rounded-xl border border-red-200 text-red-600 text-sm font-bold hover:bg-red-50 transition-all disabled:opacity-50 disabled:cursor-not-allowed">
            예약 취소 및 환불
          </button>
          <div id="detailRefundHint" class="hidden text-xs text-slate-400 mt-2"></div>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
  function toggleSeatModal(show) {
    const modal = document.getElementById('seatModal');
    if (show) {
      modal.classList.remove('hidden');
    } else {
      modal.classList.add('hidden');
    }
  }

  function setSeatSegment(seg) {
    // Simple class toggle for demo purposes
    const btnOut = document.getElementById('btn-seg-out');
    const btnIn = document.getElementById('btn-seg-in');

    if (seg === 'outbound') {
      btnOut.className = 'flex-1 py-2 text-xs font-bold rounded-lg transition-all flex flex-col items-center gap-1 bg-primary-50 text-primary-700 shadow-sm border border-primary-100';
      btnIn.className = 'flex-1 py-2 text-xs font-bold rounded-lg transition-all flex flex-col items-center gap-1 text-slate-400 hover:bg-slate-50';
    } else {
      btnIn.className = 'flex-1 py-2 text-xs font-bold rounded-lg transition-all flex flex-col items-center gap-1 bg-primary-50 text-primary-700 shadow-sm border border-primary-100';
      btnOut.className = 'flex-1 py-2 text-xs font-bold rounded-lg transition-all flex flex-col items-center gap-1 text-slate-400 hover:bg-slate-50';
    }

    if (window.setSeatModalSegment) {
      window.setSeatModalSegment(seg);
    }
  }
</script>

<style>
  .service-summary {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .service-summary-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 8px 12px;
    border-radius: 12px;
    background: #ffffff;
    border: 1px solid #e2e8f0;
  }

  .service-summary-label {
    font-size: 11px;
    font-weight: 700;
    color: #475569;
    background: #eef2ff;
    padding: 4px 8px;
    border-radius: 999px;
    white-space: nowrap;
  }

  .service-summary-value {
    font-size: 13px;
    font-weight: 600;
    color: #1f2937;
    text-align: right;
  }
</style>

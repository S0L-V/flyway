<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="max-w-5xl mx-auto space-y-8 animate-fade-in pb-10">
  <!-- Header Card -->
  <div class="bg-gradient-to-r from-[#5a7cfa] to-[#6c67f5] rounded-2xl p-7 text-white shadow-lg shadow-blue-200/60 flex flex-col md:flex-row items-center gap-6">
    <div id="profileHeaderInitial" class="w-20 h-20 rounded-full bg-white/20 border border-white/30 flex items-center justify-center text-2xl font-bold">-</div>
    <div class="flex-1 space-y-2 text-center md:text-left">
      <div id="profileHeaderName" class="text-2xl font-bold">-</div>
      <div class="flex flex-col md:flex-row md:items-center gap-2 text-sm text-white/90 justify-center md:justify-start">
        <div class="flex items-center gap-2 justify-center md:justify-start">
          <i data-lucide="mail" class="w-4 h-4"></i>
          <span id="profileHeaderEmail">-</span>
        </div>
        <div class="hidden md:block w-1 h-1 bg-white/70 rounded-full"></div>
        <div class="flex items-center gap-2 justify-center md:justify-start">
          <i data-lucide="phone" class="w-4 h-4"></i>
          <span id="profileHeaderPhone">-</span>
        </div>
      </div>
    </div>
  </div>

  <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
    <!-- Passenger Info -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-200">
      <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
        <i data-lucide="user-round" class="text-primary-500 w-[18px] h-[18px]"></i> 회원정보
      </h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-500">한글 성</label>
          <input id="profileKrLastName" type="text" class="w-full px-4 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none" value="" autocomplete="off" />
        </div>
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-500">한글 이름</label>
          <input id="profileKrFirstName" type="text" class="w-full px-4 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none" value="" autocomplete="off" />
        </div>
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-500">영문 성</label>
          <input id="profileLastName" type="text" class="w-full px-4 py-2 border border-slate-200 rounded-lg uppercase focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none" value="" autocomplete="off" />
        </div>
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-500">영문 이름</label>
          <input id="profileFirstName" type="text" class="w-full px-4 py-2 border border-slate-200 rounded-lg uppercase focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none" value="" autocomplete="off" />
        </div>
        <div class="space-y-1.5 md:col-span-2">
          <label class="text-sm font-medium text-slate-500">성별</label>
          <select id="profileGender" class="w-full px-4 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none">
            <option value="">선택</option>
            <option value="M">남성</option>
            <option value="F">여성</option>
          </select>
        </div>
        <div class="space-y-1.5 md:col-span-2">
          <label class="text-sm font-medium text-slate-500">생년월일</label>
          <input id="profileBirth" type="date" class="w-full px-4 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none" value="" />
        </div>
      </div>
    </div>

    <!-- Passport Info -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-200">
      <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
        <i data-lucide="briefcase" class="text-primary-500 w-[18px] h-[18px]"></i> 여권 정보
      </h3>
      <div class="grid grid-cols-1 gap-4">
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-500">국적 (Nationality)</label>
          <select id="profileCountry" class="w-full px-4 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none bg-white">
            <option value="">선택</option>
            <option value="KOR">대한민국 (KOR)</option>
            <option value="USA">미국 (USA)</option>
            <option value="JPN">일본 (JPN)</option>
            <option value="CHN">중국 (CHN)</option>
            <option value="VNM">베트남 (VNM)</option>
          </select>
        </div>
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-500">발급 국가</label>
          <select id="profilePassportIssueCountry" class="w-full px-4 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none bg-white">
            <option value="">선택</option>
            <option value="KOR">대한민국 (KOR)</option>
            <option value="USA">미국 (USA)</option>
            <option value="JPN">일본 (JPN)</option>
            <option value="CHN">중국 (CHN)</option>
            <option value="VNM">베트남 (VNM)</option>
          </select>
        </div>
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-500">여권 번호</label>
          <input id="profilePassportNo" type="text" class="w-full px-4 py-2 border border-slate-200 rounded-lg uppercase focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none" value="" placeholder="M12345678" autocomplete="off" />
        </div>
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-slate-500">만료일</label>
          <input id="profilePassportExpiryDate" type="date" class="w-full px-4 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-primary-100 focus:border-primary-500 transition-all outline-none" value="" />
        </div>
      </div>
    </div>
  </div>

  <div class="flex justify-end pt-2">
    <button id="profileSaveButton" class="px-10 py-3 bg-primary-600 hover:bg-primary-700 text-white rounded-2xl font-bold shadow-lg hover:shadow-primary-500/30 transition-all transform hover:-translate-y-0.5 active:translate-y-0">
      저장하기
    </button>
  </div>

  <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-200 flex items-center justify-between">
    <div>
      <div class="font-bold text-slate-800">계정 관리</div>
      <div class="text-sm text-slate-500">서비스 이용을 중단하고 싶으신가요?</div>
    </div>
    <button id="withdrawButton" type="button" class="text-slate-400 hover:text-slate-600 text-sm font-semibold">
      회원 탈퇴
    </button>
  </div>
</div>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>정보 입력 및 결제 - Flyway</title>

    <jsp:include page="/WEB-INF/views/common/head.jsp" />
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary: '#1f6feb',
                        'primary-hover': '#165bca',
                    }
                }
            }
        }
    </script>
    <style>
        /* Custom overrides or specific styles not easily done with Tailwind utilities */
        body { font-family: 'Pretendard', Arial, sans-serif; background-color: #f5f7fb; }
        .stepper__item.is-active .stepper__circle { background-color: #1f6feb; border-color: #1f6feb; color: white; }
        .stepper__item.is-completed .stepper__circle { background-color: #333; border-color: #333; color: white; }

        /* Hide default radio */
        .gender-input { display: none; }
        .gender-input:checked + .gender-box {
            background-color: #1f6feb;
            color: white;
            border-color: #1f6feb;
        }

        /* Flight Card Styles */
        .flight-card {
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.04);
            padding: 20px;
            margin-bottom: 16px;
        }
        .flight-time { font-size: 24px; font-weight: 800; color: #1a1a1a; line-height: 1; }
        .airport-code { font-size: 16px; font-weight: 700; color: #1a1a1a; margin-top: 4px; }
        .airport-name { font-size: 12px; color: #8a8a8a; margin-top: 2px; }
        .flight-duration { font-size: 12px; color: #9aa4b2; text-align: center; margin-bottom: 6px; }
        .timeline-line {
            height: 1px;
            background-image: linear-gradient(to right, #e2e8f0 50%, transparent 50%);
            background-size: 6px 1px;
            background-repeat: repeat-x;
            position: relative;
            width: 100%;
        }
        .timeline-icon {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: #fff;
            padding: 0 6px;
            color: #1f6feb;
        }
        .timeline-icon svg {
            width: 14px;
            height: 14px;
        }

        /* Passport Toggle Checkbox */
        .passport-toggle:checked + span {
            border-color: #1f6feb;
            background-color: #1f6feb;
        }
        .passport-toggle:checked + span svg {
            opacity: 1;
        }
        .passport-toggle:focus + span {
            box-shadow: 0 0 0 3px rgba(31, 111, 235, 0.2);
        }

        /* Wheel Date Picker - Inline Dropdown Style */
        .wheel-picker-wrapper {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            z-index: 100;
            padding-top: 8px;
            opacity: 0;
            visibility: hidden;
            transform: translateY(-10px);
            transition: all 0.2s ease;
        }
        .wheel-picker-wrapper.active {
            opacity: 1;
            visibility: visible;
            transform: translateY(0);
        }
        .wheel-picker {
            background: white;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
            border: 1px solid #e5e7eb;
            overflow: hidden;
        }
        .wheel-picker__header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 16px;
            border-bottom: 1px solid #f1f5f9;
            background: #f8fafc;
        }
        .wheel-picker__title {
            font-size: 13px;
            font-weight: 700;
            color: #475569;
        }
        .wheel-picker__btn {
            font-size: 12px;
            font-weight: 600;
            padding: 6px 12px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;
        }
        .wheel-picker__btn--cancel {
            background: #e2e8f0;
            color: #64748b;
        }
        .wheel-picker__btn--cancel:hover {
            background: #cbd5e1;
        }
        .wheel-picker__btn--confirm {
            background: #1f6feb;
            color: white;
        }
        .wheel-picker__btn--confirm:hover {
            background: #1a5fd1;
        }
        .wheel-picker__wheels {
            display: flex;
            padding: 0 8px;
            position: relative;
        }
        .wheel-picker__wheel {
            flex: 1;
            height: 150px;
            overflow-y: scroll;
            scroll-snap-type: y mandatory;
            -webkit-overflow-scrolling: touch;
            scrollbar-width: none;
        }
        .wheel-picker__wheel::-webkit-scrollbar {
            display: none;
        }
        .wheel-picker__wheel::before,
        .wheel-picker__wheel::after {
            content: '';
            display: block;
            height: 55px;
        }
        .wheel-picker__item {
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            font-weight: 500;
            color: #9ca3af;
            scroll-snap-align: center;
            transition: all 0.15s;
            cursor: pointer;
        }
        .wheel-picker__item:hover {
            color: #64748b;
        }
        .wheel-picker__item.selected {
            font-size: 18px;
            font-weight: 700;
            color: #1f6feb;
        }
        .wheel-picker__highlight {
            position: absolute;
            top: 50%;
            left: 12px;
            right: 12px;
            height: 40px;
            transform: translateY(-50%);
            background: rgba(31, 111, 235, 0.06);
            border-radius: 8px;
            border-top: 1.5px solid #1f6feb;
            border-bottom: 1.5px solid #1f6feb;
            pointer-events: none;
        }
        /* 생년월일 필드 wrapper */
        .birth-input-wrapper {
            position: relative;
        }
    </style>
</head>

<body class="text-gray-900">
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="max-w-[1100px] mx-auto px-4 pb-20">

    <!-- Header & Stepper -->
    <div class="flex flex-col sm:flex-row items-center justify-between py-6 gap-4">
        <div class="text-2xl font-extrabold text-gray-900">예약하기</div>
        <ol class="flex gap-4 items-center m-0 p-0 list-none">
            <li class="flex flex-col items-center gap-1.5 text-xs font-bold text-gray-400 is-completed">
                <span class="stepper__circle w-6 h-6 rounded-full border border-gray-300 flex items-center justify-center text-xs font-extrabold bg-white">✓</span>
                <span>약관동의</span>
            </li>
            <li class="flex flex-col items-center gap-1.5 text-xs font-bold text-primary is-active">
                <span class="stepper__circle w-6 h-6 rounded-full border border-gray-300 flex items-center justify-center text-xs font-extrabold bg-white">2</span>
                <span>정보입력/결제</span>
            </li>
            <li class="flex flex-col items-center gap-1.5 text-xs font-bold text-gray-400">
                <span class="stepper__circle w-6 h-6 rounded-full border border-gray-300 flex items-center justify-center text-xs font-extrabold bg-white">3</span>
                <span>예약결과</span>
            </li>
        </ol>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-[1fr_340px] gap-6 items-start">

        <!-- Left Column: Forms -->
        <div class="flex flex-col gap-4">

            <!-- Flight Summary (Redesigned & Compact) -->
            <div class="flex flex-col gap-3">
                <c:forEach var="s" items="${vm.segments}">
                    <div class="flight-card">
                        <!-- Card Header -->
                        <div class="flex justify-between items-center mb-4 text-xs text-[#8a8a8a]">
                            <div class="flex items-center gap-1.5 font-bold text-[#1a1a1a]">
                                <span class="text-primary">✈</span>
                                <span>${s.segmentOrder == 1 ? '가는 편' : '오는 편'}</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <fmt:parseDate value="${s.snapDepartureTime}" pattern="yyyy-MM-dd'T'HH:mm" var="depDate"/>
                                <fmt:formatDate value="${depDate}" pattern="yyyy-MM-dd"/>
                                <span class="w-0.5 h-0.5 bg-gray-300 rounded-full"></span>
                                <span>${s.snapAirlineName} ${s.snapFlightNumber}</span>
                            </div>
                        </div>

                        <!-- Main Layout (3 Columns) -->
                        <div class="grid grid-cols-[1fr_1.2fr_1fr] items-center gap-2">
                            <!-- Left: Departure -->
                            <div class="text-left">
                                <div class="flight-time">
                                    <fmt:formatDate value="${depDate}" pattern="HH:mm"/>
                                </div>
                                <div class="airport-code uppercase">${s.snapDepartureAirport}</div>
                                <div class="airport-name">${s.snapDepartureCity}</div>
                            </div>

                            <!-- Center: Timeline -->
                            <div class="flex flex-col items-center w-full px-2">
                                <div class="flight-duration">
                                    <!-- Duration calculation logic (simplified for display) -->
                                    <fmt:parseDate value="${s.snapArrivalTime}" pattern="yyyy-MM-dd'T'HH:mm" var="arrDate"/>
                                    <c:set var="durationMillis" value="${arrDate.time - depDate.time}" />
                                    <c:set var="durationHours" value="${durationMillis / (1000 * 60 * 60)}" />
                                    <c:set var="durationMinutes" value="${(durationMillis / (1000 * 60)) % 60}" />
                                    <fmt:formatNumber value="${durationHours}" pattern="#,##0" maxFractionDigits="0" />시간
                                    <fmt:formatNumber value="${durationMinutes}" pattern="#,##0" />분
                                </div>
                                <div class="timeline-line">
                                    <div class="timeline-icon">
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" class="transform rotate-90">
                                            <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
                                        </svg>
                                    </div>
                                </div>
                            </div>

                            <!-- Right: Arrival -->
                            <div class="text-right">
                                <div class="flight-time">
                                    <fmt:formatDate value="${arrDate}" pattern="HH:mm"/>
                                </div>
                                <div class="airport-code uppercase">${s.snapArrivalAirport}</div>
                                <div class="airport-name">
                                        ${s.snapArrivalCity}
                                    <fmt:formatDate value="${depDate}" pattern="yyyyMMdd" var="depDay"/>
                                    <fmt:formatDate value="${arrDate}" pattern="yyyyMMdd" var="arrDay"/>
                                    <c:set var="dayDiff" value="${arrDay - depDay}"/>
                                    <c:if test="${dayDiff > 0}">
                                        <span class="text-red-500 font-bold ml-1">+${dayDiff}일</span>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <!-- Passenger Form -->
            <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
                <div class="flex items-center gap-2.5 mb-5 pb-4 border-b border-gray-100">
                    <div class="w-10 h-10 rounded-xl bg-blue-50 text-primary flex items-center justify-center">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    </div>
                    <div>
                        <div class="font-bold text-base text-gray-900">탑승자 정보</div>
                        <div class="text-xs text-gray-500">여권 정보와 동일하게 입력해주세요.</div>
                    </div>
                </div>

                <c:url var="saveUrl" value="/reservations/${vm.reservationId}/passengers"/>
                <form id="passengerForm">
                    <c:forEach var="p" items="${vm.passengers}" varStatus="st">
                        <div class="mb-8 last:mb-0 pb-8 last:pb-0">
                            <h4 class="font-bold text-sm text-gray-800 mb-4">탑승자 ${st.index + 1}</h4>
                            <input type="hidden" name="passengers[${st.index}].passengerId" value="${p.passengerId}"/>

                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <!-- Row 1: Korean Name -->
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">한글 성</label>
                                    <input name="passengers[${st.index}].krLastName" value="${p.krLastName}" placeholder="홍" pattern="^[가-힣]+$" required
                                           oninput="handleKoreanInput(this)"
                                           class="kr-name-input w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                </div>
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">한글 이름</label>
                                    <input name="passengers[${st.index}].krFirstName" value="${p.krFirstName}" placeholder="길동" pattern="^[가-힣]+$" required
                                           oninput="handleKoreanInput(this)"
                                           class="kr-name-input w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                </div>

                                <!-- Row 2: English Name -->
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">영문 성 (Last Name)</label>
                                    <input name="passengers[${st.index}].lastName" value="${p.lastName}" placeholder="HONG" pattern="^[A-Z\s]+$" required
                                           oninput="handleEnglishInput(this)"
                                           class="en-name-input w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all uppercase"/>
                                </div>
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">영문 이름 (First Name)</label>
                                    <input name="passengers[${st.index}].firstName" value="${p.firstName}" placeholder="GILDONG" pattern="^[A-Z\s]+$" required
                                           oninput="handleEnglishInput(this)"
                                           class="en-name-input w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all uppercase"/>
                                </div>

                                <!-- Row 3: Birth & Gender -->
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">생년월일</label>
                                    <div class="birth-input-wrapper">
                                        <input type="text" name="passengers[${st.index}].birth" value="${p.birth}"
                                               placeholder="생년월일 선택" readonly required
                                               data-passenger-index="${st.index}"
                                               class="birth-input w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all cursor-pointer"/>
                                    </div>
                                </div>
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">성별</label>
                                    <div class="flex gap-2">
                                        <label class="flex-1 cursor-pointer">
                                            <input type="radio" name="passengers[${st.index}].gender" value="M" class="gender-input" <c:if test="${p.gender == 'M'}">checked</c:if>>
                                            <span class="gender-box flex items-center justify-center h-[46px] border border-gray-200 rounded-xl bg-gray-50 text-slate-500 text-sm font-bold transition-all">남성</span>
                                        </label>
                                        <label class="flex-1 cursor-pointer">
                                            <input type="radio" name="passengers[${st.index}].gender" value="F" class="gender-input" <c:if test="${p.gender == 'F'}">checked</c:if>>
                                            <span class="gender-box flex items-center justify-center h-[46px] border border-gray-200 rounded-xl bg-gray-50 text-slate-500 text-sm font-bold transition-all">여성</span>
                                        </label>
                                    </div>
                                </div>

                                <!-- Row 4: Contact -->
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">연락처</label>
                                    <input name="passengers[${st.index}].phoneNumber" value="${p.phoneNumber}" placeholder="- 제외 연락처" pattern="^[0-9]{10,11}$" maxlength="11" required
                                           oninput="this.value = this.value.replace(/[^0-9]/g, '')"
                                           class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                </div>
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">이메일</label>
                                    <input type="email" name="passengers[${st.index}].email" value="${p.email}" placeholder="example@email.com" required
                                           class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                </div>

                                <!-- Row 5: Passport Info -->
                                <div class="md:col-span-2 mt-2">
                                    <div class="flex items-center gap-3 mb-3">
                                        <label class="flex items-center gap-2 cursor-pointer group">
                                            <input type="checkbox" class="passport-toggle sr-only" data-index="${st.index}"
                                                   <c:if test="${not empty p.passportNo}">checked</c:if>>
                                            <span class="w-5 h-5 rounded-full border-2 border-gray-300 flex items-center justify-center transition-all
                                                         group-hover:border-primary
                                                         [input:checked+&]:border-primary [input:checked+&]:bg-primary">
                                                <svg class="w-3 h-3 text-white opacity-0 [input:checked~&]:opacity-100 transition-opacity" fill="currentColor" viewBox="0 0 20 20">
                                                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                                                </svg>
                                            </span>
                                            <span class="text-xs font-bold text-gray-800">여권 정보</span>
                                        </label>
                                        <span class="text-[11px] font-normal text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">선택사항 (나중에 등록 가능)</span>
                                    </div>
                                    <div class="passport-fields grid grid-cols-1 md:grid-cols-2 gap-4 transition-all duration-300"
                                         data-index="${st.index}"
                                         style="${empty p.passportNo ? 'display: none; opacity: 0;' : ''}">
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-xs font-bold text-slate-500 ml-0.5">여권번호</label>
                                            <input name="passengers[${st.index}].passportNo" value="${p.passportNo}" placeholder="M12345678" pattern="^[A-Z0-9]{7,9}$" oninput="this.value = this.value.toUpperCase()"
                                                   class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all uppercase"/>
                                        </div>
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-xs font-bold text-slate-500 ml-0.5">여권만료일</label>
                                            <input type="date" name="passengers[${st.index}].passportExpiryDate" value="${p.passportExpiryDate}" min="1900-01-01" max="2099-12-31"
                                                   class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                        </div>
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-xs font-bold text-slate-500 ml-0.5">국적</label>
                                            <input name="passengers[${st.index}].country" value="${p.country}" placeholder="대한민국"
                                                   class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                        </div>
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-xs font-bold text-slate-500 ml-0.5">발행국</label>
                                            <input name="passengers[${st.index}].passportIssueCountry" value="${p.passportIssueCountry}" placeholder="대한민국"
                                                   class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>

                    <div class="flex justify-end mt-6 pt-6 border-t border-gray-100">
                        <button type="submit" class="bg-primary hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-xl shadow-lg shadow-blue-500/20 transition-all flex items-center gap-2">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path><polyline points="17 21 17 13 7 13 7 21"></polyline><polyline points="7 3 7 8 15 8"></polyline></svg>
                            탑승자 정보 저장
                        </button>
                    </div>
                </form>
            </div>

            <!-- Service Options -->
            <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
                <div class="flex items-center gap-2 mb-4">
                    <div class="w-10 h-10 rounded-xl bg-slate-100 text-slate-600 flex items-center justify-center">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 9l-7 7-7-7"/></svg>
                    </div>
                    <div class="font-bold text-lg text-gray-900">좌석 및 부가서비스</div>
                </div>

                <div class="text-sm text-gray-500 mb-4">
                    탑승자 정보 저장 후 선택 가능합니다. (현재: <span class="${vm.passengerSaved ? 'text-green-600 font-bold' : 'text-red-500 font-bold'}">${vm.passengerSaved ? '저장 완료' : '미저장'}</span>)
                </div>

                <!-- 좌석 선택 통합 카드 -->
                <div class="flex items-center justify-between bg-slate-50 border border-slate-200 p-4 rounded-xl hover:bg-white hover:border-primary transition-all group">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 bg-blue-50 text-primary rounded-xl flex items-center justify-center shrink-0">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="4" y="4" width="6" height="6" rx="1"></rect>
                                <rect x="14" y="4" width="6" height="6" rx="1"></rect>
                                <rect x="4" y="14" width="6" height="6" rx="1"></rect>
                                <rect x="14" y="14" width="6" height="6" rx="1"></rect>
                            </svg>
                        </div>
                        <div>
                            <div class="text-sm font-bold text-gray-800">좌석 선택</div>
                            <div class="text-xs text-gray-500">
                                <c:forEach var="s" items="${vm.segments}" varStatus="status">
                                    <c:out value="${s.snapDepartureCity}"/> → <c:out value="${s.snapArrivalCity}"/><c:if test="${!status.last}"> / </c:if>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                    <c:set var="firstSegmentId" value="${vm.segments[0].reservationSegmentId}" />
                    <button type="button" ${vm.passengerSaved ? "" : "disabled"} onclick="openSeatPopup('${firstSegmentId}')"
                            class="px-5 py-2.5 bg-primary text-white rounded-lg text-sm font-bold hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="4" y="4" width="6" height="6" rx="1"></rect>
                            <rect x="14" y="4" width="6" height="6" rx="1"></rect>
                            <rect x="4" y="14" width="6" height="6" rx="1"></rect>
                            <rect x="14" y="14" width="6" height="6" rx="1"></rect>
                        </svg>
                        선택하기
                    </button>
                </div>

                <!-- Baggage / Ancillary Banner -->
                <div class="mt-4 bg-white border border-gray-200 rounded-2xl p-5 flex flex-col sm:flex-row items-center justify-between gap-4 shadow-sm">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 bg-orange-50 text-orange-500 rounded-xl flex items-center justify-center shrink-0">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="6" width="18" height="13" rx="2" ry="2"></rect><path d="M16 6V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"></path><path d="M12 6v13"></path></svg>
                        </div>
                        <div class="text-center sm:text-left">
                            <div class="font-bold text-sm text-slate-800">부가서비스 신청</div>
                            <div class="text-xs text-slate-500">사전 수하물 구매 및 기내식 신청</div>
                        </div>
                    </div>
                    <button type="button" ${vm.passengerSaved ? "" : "disabled"} onclick="openServicePopup()"
                            class="w-full sm:w-auto px-5 py-2.5 bg-primary text-white rounded-lg font-bold text-sm hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed">
                        신청하기
                    </button>
                </div>
            </div>

        </div>

        <!-- Right Column: Sidebar -->
        <div class="sticky top-6">

            <!-- Price Summary -->
            <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 mb-4">
                <div class="flex items-center gap-2 pb-3 mb-4 border-b border-gray-100 font-bold text-gray-800">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path><line x1="7" y1="7" x2="7.01" y2="7"></line></svg>
                    결제 상세
                </div>

                <div class="flex justify-between mb-3 text-sm text-slate-500">
                    <span>항공 운임</span>
                    <strong class="text-slate-800" id="flightPrice">₩0</strong>
                </div>

                <!-- Seat Details -->
                <div id="seatInfoContainer">
                    <c:forEach var="s" items="${vm.segments}">
                        <c:if test="${not empty s.passengerSeats}">
                            <div class="pl-3 mb-2 text-xs text-slate-400 border-l-2 border-slate-100">
                                <span class="block mb-1">${s.segmentOrder == 1 ? '가는편' : '오는편'} 좌석:</span>
                                <c:forEach var="seat" items="${s.passengerSeats}" varStatus="st">
                                    ${seat.passengerName} ${seat.seatNo}<c:if test="${!st.last}">, </c:if>
                                </c:forEach>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>

                <div class="flex justify-between mb-3 text-sm text-slate-500">
                    <span>부가서비스</span>
                    <strong class="text-slate-800" id="servicePrice">₩0</strong>
                </div>

                <!-- Service Details -->
                <div id="serviceInfoContainer">
                    <c:forEach var="s" items="${vm.segments}">
                        <c:set var="hasBaggage" value="false"/>
                        <c:forEach var="svc" items="${s.passengerServices}">
                            <c:if test="${svc.serviceType == '0'}"><c:set var="hasBaggage" value="true"/></c:if>
                        </c:forEach>
                        <c:if test="${hasBaggage}">
                            <div class="pl-3 mb-2 text-xs text-slate-400 border-l-2 border-slate-100">
                                <span class="block mb-1">${s.segmentOrder == 1 ? '가는편' : '오는편'} 수하물:</span>
                                <c:set var="isFirst" value="true" />
                                <c:forEach var="svc" items="${s.passengerServices}">
                                    <c:if test="${svc.serviceType == '0'}">
                                        <c:if test="${!isFirst}">, </c:if>
                                        ${svc.passengerName} ${svc.serviceName}
                                        <c:set var="isFirst" value="false" />
                                    </c:if>
                                </c:forEach>
                            </div>
                        </c:if>
                    </c:forEach>

                    <c:forEach var="s" items="${vm.segments}">
                        <c:set var="hasMeal" value="false"/>
                        <c:forEach var="svc" items="${s.passengerServices}">
                            <c:if test="${svc.serviceType == '1'}"><c:set var="hasMeal" value="true"/></c:if>
                        </c:forEach>
                        <c:if test="${hasMeal}">
                            <div class="pl-3 mb-2 text-xs text-slate-400 border-l-2 border-slate-100">
                                <span class="block mb-1">${s.segmentOrder == 1 ? '가는편' : '오는편'} 기내식:</span>
                                <c:set var="isFirst" value="true" />
                                <c:forEach var="svc" items="${s.passengerServices}">
                                    <c:if test="${svc.serviceType == '1'}">
                                        <c:if test="${!isFirst}">, </c:if>
                                        ${svc.passengerName} ${svc.serviceName}
                                        <c:set var="isFirst" value="false" />
                                    </c:if>
                                </c:forEach>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>

                <div class="mt-4 pt-4 border-t border-gray-100 flex justify-between items-end">
                    <span class="font-extrabold text-slate-800">총 결제금액</span>
                    <span class="text-2xl font-black text-primary" id="totalPrice">₩0</span>
                </div>

                <button type="button" ${vm.passengerSaved ? "" : "disabled"} onclick="goPayment()" id="payBtn"
                        class="w-full mt-5 py-4 bg-primary text-white rounded-xl font-extrabold text-base shadow-lg shadow-blue-500/25 hover:bg-blue-700 hover:-translate-y-0.5 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none">
                    결제하기
                </button>
                <div class="text-center text-[11px] text-slate-400 mt-3">
                    유류할증료와 제세공과금은 변동될 수 있습니다.
                </div>
            </div>

            <!-- Notices -->
            <div class="bg-white rounded-2xl p-5 border border-gray-100">
                <div class="flex items-center gap-1.5 text-xs font-extrabold text-slate-800 mb-3">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f97316" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                    예약 유의사항
                </div>
                <div class="flex flex-col gap-2">
                    <div class="pl-2 border-l-2 border-blue-50 text-[11px] text-slate-500 leading-relaxed">
                        영문 성명 철자가 여권과 다를 경우 탑승이 거절될 수 있으며, 이에 대한 책임은 본인에게 있습니다.
                    </div>
                    <div class="pl-2 border-l-2 border-blue-50 text-[11px] text-slate-500 leading-relaxed">
                        여권 유효기간은 출발일 기준 6개월 이상 남아있어야 합니다.
                    </div>
                    <div class="pl-2 border-l-2 border-blue-50 text-[11px] text-slate-500 leading-relaxed">
                        목적지 국가의 비자 필요 여부를 반드시 확인해주시기 바랍니다.
                    </div>
                </div>
            </div>

        </div>

    </div>
</div>

<!-- Wheel Date Picker Template (will be cloned for each birth input) -->
<template id="wheelPickerTemplate">
    <div class="wheel-picker-wrapper">
        <div class="wheel-picker">
            <div class="wheel-picker__header">
                <button type="button" class="wheel-picker__btn wheel-picker__btn--cancel js-wheel-cancel">취소</button>
                <span class="wheel-picker__title">생년월일</span>
                <button type="button" class="wheel-picker__btn wheel-picker__btn--confirm js-wheel-confirm">확인</button>
            </div>
            <div class="wheel-picker__wheels">
                <div class="wheel-picker__highlight"></div>
                <div class="wheel-picker__wheel js-year-wheel"></div>
                <div class="wheel-picker__wheel js-month-wheel"></div>
                <div class="wheel-picker__wheel js-day-wheel"></div>
            </div>
        </div>
    </div>
</template>

<script type="module">
    import { fetchWithRefresh } from '/resources/common/js/authFetch.js';
    var reservationId = '${vm.reservationId}';
    var passengerSaved = ${vm.passengerSaved};
    var passengerCount = ${vm.passengerCount};
    var segments = [
        <c:forEach var="s" items="${vm.segments}" varStatus="st">
        {
            snapPrice: ${s.snapPrice != null ? s.snapPrice : 0},
            segmentId: '${s.reservationSegmentId}',
            segmentOrder: ${s.segmentOrder},
            depCity: '${s.snapDepartureCity}',
            arrCity: '${s.snapArrivalCity}',
            seatCount: ${fn:length(s.passengerSeats)}
        }<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];

    // 좌석 팝업
    function openSeatPopup(segmentId) {
        if (!passengerSaved) {
            Swal.warning('탑승자 정보를 먼저 저장해주세요.', '정보 미입력');
            return;
        }

        if (!segmentId) {
            Swal.error('구간 정보를 찾을 수 없습니다.', '오류');
            return;
        }
        const popupUrl = '/reservations/' + encodeURIComponent(reservationId)
            + '/segments/' + encodeURIComponent(segmentId) + '/seats';
        window.open(popupUrl, 'seatPopup', 'width=1100,height=800,scrollbars=yes,resizable=yes');
    }

    // 부가서비스 팝업 열기
    function openServicePopup() {
        if (!passengerSaved) {
            Swal.warning('탑승자 정보를 먼저 저장해주세요.', '정보 미입력');
            return;
        }

        var popupUrl = '/reservations/' + reservationId + '/services';
        var popupOption = 'width=800,height=700,scrollbars=yes,resizable=yes';
        window.open(popupUrl, 'servicePopup', popupOption);
    }

    // 부가서비스 정보 갱신 (총액 + 상세)
    async function refreshServiceInfo() {
        try {
            const res = await fetchWithRefresh('/reservations/' + reservationId + '/services/details');
            const data = await res.json();
            if (data.success) {
                // 총액 갱신
                document.getElementById('servicePrice').textContent = '₩' + numberWithCommas(data.total);

                // 상세 정보 갱신
                let html = '';
                data.segments.forEach(function(seg) {
                    var label = seg.segmentOrder === 1 ? '가는편' : '오는편';

                    // 수하물
                    if (seg.baggageServices && seg.baggageServices.length > 0) {
                        html += '<div class="pl-3 mb-2 text-xs text-slate-400 border-l-2 border-slate-100">';
                        html += '<span class="block mb-1">' + label + ' 수하물:</span>';
                        seg.baggageServices.forEach(function(svc, idx) {
                            if (idx > 0) html += ', ';
                            html += svc.passengerName + ' ' + parseBaggageInfo(svc.serviceName);
                        });
                        html += '</div>';
                    }

                    // 기내식
                    if (seg.mealServices && seg.mealServices.length > 0) {
                        html += '<div class="pl-3 mb-2 text-xs text-slate-400 border-l-2 border-slate-100">';
                        html += '<span class="block mb-1">' + label + ' 기내식:</span>';
                        seg.mealServices.forEach(function(svc, idx) {
                            if (idx > 0) html += ', ';
                            html += svc.passengerName + ' ' + svc.serviceName;
                        });
                        html += '</div>';
                    }
                });
                document.getElementById('serviceInfoContainer').innerHTML = html;

                updateTotalPrice();
            }
        } catch (err) {
            console.error('Failed to refresh service info', err);
        }
    }

    // 수하물 JSON 파싱 (예: {"extraKg":5,"extraBags":1} → "+5kg, 추가 1개")
    function parseBaggageInfo(jsonStr) {
        try {
            var obj = JSON.parse(jsonStr);
            var parts = [];
            if (obj.extraKg > 0) parts.push('+' + obj.extraKg + 'kg');
            if (obj.extraBags > 0) parts.push('추가 ' + obj.extraBags + '개');
            return parts.length > 0 ? parts.join(', ') : '기본';
        } catch (e) {
            return jsonStr || '기본';
        }
    }

    // 기존 호환성 유지
    function refreshServiceTotal() {
        refreshServiceInfo();
    }
    async function refreshSeatInfo() {
        try {
            const res = await fetchWithRefresh('/reservations/' + reservationId + '/seats/info');
            const data = await res.json();
            if (data.success) {
                let html = '';
                data.segments.forEach(function(seg) {
                    if (seg.passengerSeats && seg.passengerSeats.length > 0) {
                        html += '<div class="pl-3 mb-2 text-xs text-slate-400 border-l-2 border-slate-100">';
                        html += '<span class="block mb-1">' + (seg.segmentOrder === 1 ? '가는편' : '오는편') + '좌석:</span>';
                        seg.passengerSeats.forEach(function(seat, idx) {
                            html += seat.passengerName + ' ' + seat.seatNo;
                            if (idx < seg.passengerSeats.length - 1) html += ', ';
                        });
                        html += '</div>';
                    }
                });
                document.getElementById('seatInfoContainer').innerHTML = html;
            }
        } catch (err) {
            console.error('Failed to refresh seat info', err);
        }
    }
    // 총 금액 업데이트
    function updateTotalPrice() {
        var flight = parsePrice(document.getElementById('flightPrice').textContent);
        var service = parsePrice(document.getElementById('servicePrice').textContent);
        var total = flight + service;
        document.getElementById('totalPrice').textContent = '₩' + numberWithCommas(total);
    }

    // 가격 문자열 파싱
    function parsePrice(str) {
        return parseInt(str.replace(/[₩,]/g, '')) || 0;
    }

    // 숫자 콤마 포맷
    function numberWithCommas(x) {
        return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }

    // 결제 페이지 이동
    function goPayment() {
        if (!passengerSaved) {
            Swal.warning('탑승자 정보를 먼저 저장해주세요.', '정보 미입력');
            return;
        }

        // 좌석 선택 여부 체크
        const incompleteSegment = segments.find(seg => seg.seatCount < passengerCount);

        if (incompleteSegment) {
            const segmentLabel = incompleteSegment.segmentOrder === 1 ? '가는편' : '오는편';
            const routeText = incompleteSegment.depCity + ' → ' + incompleteSegment.arrCity;

            Swal.fire({
                icon: 'warning',
                title: '좌석 미선택',
                html: '<b>' + segmentLabel + '</b> (' + routeText + ')<br>모든 승객의 좌석을 선택해주세요.',
                confirmButtonText: '좌석 선택하기',
                confirmButtonColor: '#1f6feb',
                showCancelButton: true,
                cancelButtonText: '취소',
                cancelButtonColor: '#64748b'
            }).then((result) => {
                if (result.isConfirmed) {
                    openSeatPopup(incompleteSegment.segmentId);
                }
            });
            return;
        }

        // 결제 페이지로 이동 (PaymentController)
        location.href = '/payments/' + reservationId;
    }

    function getLocalISODate() {
        const now = new Date();
        const tzOffsetMs = now.getTimezoneOffset() * 60000;
        return new Date(now.getTime() - tzOffsetMs).toISOString().split('T')[0];
    }

    // 페이지 로드 시 부가서비스 총액 조회
    document.addEventListener('DOMContentLoaded', function() {
        var flightTotal = 0;
        segments.forEach(function(seg) {
            flightTotal += seg.snapPrice;
        });
        flightTotal *= passengerCount;
        document.getElementById('flightPrice').textContent = '₩' + numberWithCommas(flightTotal);

        if (passengerSaved) {
            refreshServiceInfo();
        }
        updateTotalPrice();

        // 생년월일 최대 날짜 제한 (오늘로 설정)
        const today = getLocalISODate();
        document.querySelectorAll('input[type="date"]').forEach(el => {
            if(el.name.includes('birth')) {
                el.max = today;
            }
            if(el.name.includes('passportExpiryDate')) {
                el.min = today; // 여권만료는 오늘부터만 가능
            }
        });

        // 여권 정보 토글
        document.querySelectorAll('.passport-toggle').forEach(toggle => {
            toggle.addEventListener('change', function() {
                const idx = this.dataset.index;
                const fields = document.querySelector('.passport-fields[data-index="' + idx + '"]');
                if (this.checked) {
                    fields.style.display = 'grid';
                    setTimeout(() => { fields.style.opacity = '1'; }, 10);
                } else {
                    fields.style.opacity = '0';
                    setTimeout(() => { fields.style.display = 'none'; }, 300);
                    // 필드 초기화
                    fields.querySelectorAll('input').forEach(input => { input.value = ''; });
                }
            });
        });

        // 휠 날짜 피커 초기화
        initWheelDatePicker();

        // 외부 클릭 시 피커 닫기
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.birth-input-wrapper')) {
                document.querySelectorAll('.wheel-picker-wrapper.active').forEach(picker => {
                    picker.classList.remove('active');
                });
            }
        });

        // 폼 제출 이벤트 리스너
        const passengerForm = document.getElementById('passengerForm');
        if (passengerForm) {
            passengerForm.addEventListener('submit', savePassengers);
        }
    });

    // 휠 날짜 피커 초기화
    function initWheelDatePicker() {
        const template = document.getElementById('wheelPickerTemplate');
        const currentYear = new Date().getFullYear();

        document.querySelectorAll('.birth-input').forEach(input => {
            const wrapper = input.closest('.birth-input-wrapper');

            // 템플릿 복제하여 각 입력 필드에 추가
            const pickerClone = template.content.cloneNode(true);
            const pickerWrapper = pickerClone.querySelector('.wheel-picker-wrapper');
            const yearWheel = pickerClone.querySelector('.js-year-wheel');
            const monthWheel = pickerClone.querySelector('.js-month-wheel');
            const dayWheel = pickerClone.querySelector('.js-day-wheel');

            // 휠 아이템 생성
            for (let y = currentYear; y >= 1920; y--) {
                const item = document.createElement('div');
                item.className = 'wheel-picker__item';
                item.dataset.value = y;
                item.textContent = y + '년';
                yearWheel.appendChild(item);
            }

            for (let m = 1; m <= 12; m++) {
                const item = document.createElement('div');
                item.className = 'wheel-picker__item';
                item.dataset.value = String(m).padStart(2, '0');
                item.textContent = m + '월';
                monthWheel.appendChild(item);
            }

            for (let d = 1; d <= 31; d++) {
                const item = document.createElement('div');
                item.className = 'wheel-picker__item';
                item.dataset.value = String(d).padStart(2, '0');
                item.textContent = d + '일';
                dayWheel.appendChild(item);
            }

            wrapper.appendChild(pickerClone);

            // 입력 필드 클릭 시 피커 열기
            input.addEventListener('click', (e) => {
                e.stopPropagation();

                // 다른 피커 닫기
                document.querySelectorAll('.wheel-picker-wrapper.active').forEach(p => {
                    if (p !== pickerWrapper) p.classList.remove('active');
                });

                const isOpen = pickerWrapper.classList.contains('active');
                if (isOpen) {
                    pickerWrapper.classList.remove('active');
                } else {
                    pickerWrapper.classList.add('active');

                    // 기존 값으로 스크롤
                    const currentValue = input.value;
                    let year = 2000, month = '01', day = '01';
                    if (currentValue) {
                        const parts = currentValue.split('-');
                        year = parseInt(parts[0]);
                        month = parts[1];
                        day = parts[2];
                    }
                    scrollToValue(yearWheel, year);
                    scrollToValue(monthWheel, month);
                    scrollToValue(dayWheel, day);
                }
            });

            // 스크롤 이벤트
            [yearWheel, monthWheel, dayWheel].forEach(wheel => {
                wheel.addEventListener('scroll', () => updateSelectedItem(wheel));
                wheel.addEventListener('scrollend', () => snapToItem(wheel));
            });

            // 취소 버튼
            wrapper.querySelector('.js-wheel-cancel').addEventListener('click', (e) => {
                e.stopPropagation();
                pickerWrapper.classList.remove('active');
            });

            // 확인 버튼
            wrapper.querySelector('.js-wheel-confirm').addEventListener('click', (e) => {
                e.stopPropagation();
                const year = getSelectedValue(yearWheel);
                const month = getSelectedValue(monthWheel);
                const day = getSelectedValue(dayWheel);
                if (year && month && day) {
                    input.value = year + '-' + month + '-' + day;
                }
                pickerWrapper.classList.remove('active');
            });
        });
    }

    function scrollToValue(wheel, value) {
        const items = wheel.querySelectorAll('.wheel-picker__item');
        for (const item of items) {
            if (item.dataset.value == value) {
                const itemTop = item.offsetTop - 55;
                wheel.scrollTop = itemTop;
                break;
            }
        }
        setTimeout(() => updateSelectedItem(wheel), 50);
    }

    function updateSelectedItem(wheel) {
        const items = wheel.querySelectorAll('.wheel-picker__item');
        const wheelCenter = wheel.scrollTop + wheel.clientHeight / 2;

        items.forEach(item => {
            const itemCenter = item.offsetTop + item.clientHeight / 2 - 55;
            const distance = Math.abs(wheelCenter - itemCenter - 55);

            if (distance < 25) {
                item.classList.add('selected');
            } else {
                item.classList.remove('selected');
            }
        });
    }

    function snapToItem(wheel) {
        const selected = wheel.querySelector('.wheel-picker__item.selected');
        if (selected) {
            const targetTop = selected.offsetTop - 55;
            wheel.scrollTo({ top: targetTop, behavior: 'smooth' });
        }
    }

    function getSelectedValue(wheel) {
        const selected = wheel.querySelector('.wheel-picker__item.selected');
        return selected ? selected.dataset.value : null;
    }

    // =============================================
    // 한글 ↔ 영문 자동 변환
    // =============================================

    // 한글 → 영문 로마자 변환 (Revised Romanization)
    const CHO = ['g', 'kk', 'n', 'd', 'tt', 'r', 'm', 'b', 'pp', 's', 'ss', '', 'j', 'jj', 'ch', 'k', 't', 'p', 'h'];
    const JUNG = ['a', 'ae', 'ya', 'yae', 'eo', 'e', 'yeo', 'ye', 'o', 'wa', 'wae', 'oe', 'yo', 'u', 'wo', 'we', 'wi', 'yu', 'eu', 'ui', 'i'];
    const JONG = ['', 'k', 'k', 'k', 'n', 'n', 'n', 't', 'l', 'l', 'l', 'l', 'l', 'l', 'l', 'l', 'm', 'p', 'p', 't', 't', 'ng', 't', 't', 'k', 't', 'p', 't'];

    function koreanToRoman(str) {
        let result = '';
        for (let i = 0; i < str.length; i++) {
            const code = str.charCodeAt(i);
            // 한글 완성형 범위 (가-힣)
            if (code >= 0xAC00 && code <= 0xD7A3) {
                const syllable = code - 0xAC00;
                const cho = Math.floor(syllable / 588);
                const jung = Math.floor((syllable % 588) / 28);
                const jong = syllable % 28;
                result += CHO[cho] + JUNG[jung] + JONG[jong];
            } else if (/[a-zA-Z]/.test(str[i])) {
                result += str[i];
            }
        }
        return result.toUpperCase();
    }

    // 영문 키보드 → 한글 변환 (두벌식 기준)
    const EN_TO_KR = {
        'q': 'ㅂ', 'w': 'ㅈ', 'e': 'ㄷ', 'r': 'ㄱ', 't': 'ㅅ', 'y': 'ㅛ', 'u': 'ㅕ', 'i': 'ㅑ', 'o': 'ㅐ', 'p': 'ㅔ',
        'a': 'ㅁ', 's': 'ㄴ', 'd': 'ㅇ', 'f': 'ㄹ', 'g': 'ㅎ', 'h': 'ㅗ', 'j': 'ㅓ', 'k': 'ㅏ', 'l': 'ㅣ',
        'z': 'ㅋ', 'x': 'ㅌ', 'c': 'ㅊ', 'v': 'ㅍ', 'b': 'ㅠ', 'n': 'ㅜ', 'm': 'ㅡ',
        'Q': 'ㅃ', 'W': 'ㅉ', 'E': 'ㄸ', 'R': 'ㄲ', 'T': 'ㅆ', 'O': 'ㅒ', 'P': 'ㅖ'
    };

    // 자모 조합
    const CHO_MAP = { 'ㄱ': 0, 'ㄲ': 1, 'ㄴ': 2, 'ㄷ': 3, 'ㄸ': 4, 'ㄹ': 5, 'ㅁ': 6, 'ㅂ': 7, 'ㅃ': 8, 'ㅅ': 9, 'ㅆ': 10, 'ㅇ': 11, 'ㅈ': 12, 'ㅉ': 13, 'ㅊ': 14, 'ㅋ': 15, 'ㅌ': 16, 'ㅍ': 17, 'ㅎ': 18 };
    const JUNG_MAP = { 'ㅏ': 0, 'ㅐ': 1, 'ㅑ': 2, 'ㅒ': 3, 'ㅓ': 4, 'ㅔ': 5, 'ㅕ': 6, 'ㅖ': 7, 'ㅗ': 8, 'ㅘ': 9, 'ㅙ': 10, 'ㅚ': 11, 'ㅛ': 12, 'ㅜ': 13, 'ㅝ': 14, 'ㅞ': 15, 'ㅟ': 16, 'ㅠ': 17, 'ㅡ': 18, 'ㅢ': 19, 'ㅣ': 20 };
    const JONG_MAP = { '': 0, 'ㄱ': 1, 'ㄲ': 2, 'ㄳ': 3, 'ㄴ': 4, 'ㄵ': 5, 'ㄶ': 6, 'ㄷ': 7, 'ㄹ': 8, 'ㄺ': 9, 'ㄻ': 10, 'ㄼ': 11, 'ㄽ': 12, 'ㄾ': 13, 'ㄿ': 14, 'ㅀ': 15, 'ㅁ': 16, 'ㅂ': 17, 'ㅄ': 18, 'ㅅ': 19, 'ㅆ': 20, 'ㅇ': 21, 'ㅈ': 22, 'ㅊ': 23, 'ㅋ': 24, 'ㅌ': 25, 'ㅍ': 26, 'ㅎ': 27 };

    function englishToKorean(str) {
        // 영문 → 자모 변환
        let jamo = '';
        for (let i = 0; i < str.length; i++) {
            jamo += EN_TO_KR[str[i]] || str[i];
        }
        // 자모 → 한글 조합 (간단 버전)
        return assembleKorean(jamo);
    }

    function assembleKorean(jamo) {
        // 간단한 자모 조합 (완벽하지 않지만 기본적인 조합)
        const CHO_SET = new Set(Object.keys(CHO_MAP));
        const JUNG_SET = new Set(Object.keys(JUNG_MAP));
        const JONG_SET = new Set(Object.keys(JONG_MAP));

        let result = '';
        let i = 0;
        while (i < jamo.length) {
            const c = jamo[i];

            // 초성 + 중성 조합 시도
            if (CHO_SET.has(c) && i + 1 < jamo.length && JUNG_SET.has(jamo[i + 1])) {
                const cho = CHO_MAP[c];
                const jung = JUNG_MAP[jamo[i + 1]];
                let jong = 0;

                // 종성 확인
                if (i + 2 < jamo.length && JONG_SET.has(jamo[i + 2])) {
                    // 다음 글자가 초성+중성이면 종성으로 사용 안함
                    if (i + 3 < jamo.length && JUNG_SET.has(jamo[i + 3])) {
                        // 종성 없이 조합
                    } else {
                        jong = JONG_MAP[jamo[i + 2]];
                        i++;
                    }
                }

                result += String.fromCharCode(0xAC00 + (cho * 588) + (jung * 28) + jong);
                i += 2;
            } else {
                result += c;
                i++;
            }
        }
        return result;
    }

    // 영문 필드 입력 핸들러
    function handleEnglishInput(el) {
        const val = el.value;
        // 한글이 포함되어 있으면 로마자로 변환
        if (/[가-힣]/.test(val)) {
            el.value = koreanToRoman(val);
        } else {
            // 영문만 허용
            el.value = val.replace(/[^a-zA-Z\s]/g, '').toUpperCase();
        }
    }

    // 한글 필드 입력 핸들러
    function handleKoreanInput(el) {
        const val = el.value;
        // 영문이 포함되어 있으면 한글로 변환
        if (/[a-zA-Z]/.test(val)) {
            const converted = englishToKorean(val);
            // 한글만 남기기
            el.value = converted.replace(/[^가-힣ㄱ-ㅎㅏ-ㅣ]/g, '');
        } else {
            // 한글만 허용
            el.value = val.replace(/[^가-힣ㄱ-ㅎㅏ-ㅣ]/g, '');
        }
    }

    window.handleEnglishInput = handleEnglishInput;
    window.handleKoreanInput = handleKoreanInput;
    window.openSeatPopup = openSeatPopup;
    window.openServicePopup = openServicePopup;
    window.refreshServiceTotal = refreshServiceTotal;
    window.refreshServiceInfo = refreshServiceInfo;
    window.refreshSeatInfo = refreshSeatInfo;
    window.goPayment = goPayment;

    async function savePassengers(event) {
        event.preventDefault();
        console.log('savePassengers 함수 시작');

        const passengers = [];
        let index = 0;

        // --- 유효성 검사 정규식 ---
        const regKr = /^[가-힣]+$/;             // 한글만
        const regEn = /^[A-Z\s]+$/;             // 영문 대문자와 공백만
        const regPhone = /^01[0-9]{8,9}$/;      // 한국 휴대폰 번호 형식
        const regEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // 이메일 형식

        while (true) {
            const passengerIdEl = document.querySelector('input[name="passengers[' + index + '].passengerId"]');
            if (!passengerIdEl) break;

            const krLastName = document.querySelector('input[name="passengers[' + index + '].krLastName"]').value.trim();
            const krFirstName = document.querySelector('input[name="passengers[' + index + '].krFirstName"]').value.trim();
            const lastName = document.querySelector('input[name="passengers[' + index + '].lastName"]').value.trim().toUpperCase();
            const firstName = document.querySelector('input[name="passengers[' + index + '].firstName"]').value.trim().toUpperCase();
            const birth = document.querySelector('input[name="passengers[' + index + '].birth"]').value;
            const email = document.querySelector('input[name="passengers[' + index + '].email"]').value.trim();
            const phoneNumber = document.querySelector('input[name="passengers[' + index + '].phoneNumber"]').value.trim();
            const genderEl = document.querySelector(
                'input[name="passengers[' + index + '].gender"]:checked'
            );

            const pNum = index + 1; // 탑승자 번호

            // 0. 필수 필드 빈 값 체크
            if (!krLastName || !krFirstName) {
                Swal.warning('탑승자 ' + pNum + '의 한글 성명을 입력해주세요.', '입력 오류');
                return false;
            }
            if (!lastName || !firstName) {
                Swal.warning('탑승자 ' + pNum + '의 영문 성명을 입력해주세요.', '입력 오류');
                return false;
            }
            if (!birth) {
                Swal.warning('탑승자 ' + pNum + '의 생년월일을 입력해주세요.', '입력 오류');
                return false;
            }
            if (!phoneNumber) {
                Swal.warning('탑승자 ' + pNum + '의 연락처를 입력해주세요.', '입력 오류');
                return false;
            }
            if (!email) {
                Swal.warning('탑승자 ' + pNum + '의 이메일을 입력해주세요.', '입력 오류');
                return false;
            }

            // 1. 성별 체크
            if (!genderEl) {
                Swal.warning('탑승자 ' + pNum + '의 성별을 선택해주세요.', '입력 오류');
                return false;
            }

            // 2. 한글 이름 검사
            if (!regKr.test(krLastName) || !regKr.test(krFirstName)) {
                Swal.warning('탑승자 ' + pNum + '의 한글 성명은 한글만 입력 가능합니다.', '입력 오류');
                return false;
            }

            // 3. 영문 이름 검사
            if (!regEn.test(lastName) || !regEn.test(firstName)) {
                Swal.warning('탑승자 ' + pNum + '의 영문 성명은 영문 대문자만 입력 가능합니다.', '입력 오류');
                return false;
            }

            // 4. 생년월일 검사 (미래 날짜 선택 방지)
            const today = getLocalISODate();
            if (birth > today) {
                Swal.warning('탑승자 ' + pNum + '의 생년월일이 올바르지 않습니다.', '입력 오류');
                return false;
            }

            // 5. 연락처 및 이메일 형식 검사
            if (!regPhone.test(phoneNumber)) {
                Swal.warning('탑승자 ' + pNum + '의 연락처 형식이 올바르지 않습니다. (예: 01012345678)', '입력 오류');
                return false;
            }
            if (!regEmail.test(email)) {
                Swal.warning('탑승자 ' + pNum + '의 이메일 형식이 올바르지 않습니다.', '입력 오류');
                return false;
            }

            // 여권 정보가 입력된 경우 추가 검사 (필수가 아닐 수도 있으므로 입력 시에만 체크)
            const passportNo = document.querySelector('input[name="passengers[' + index + '].passportNo"]').value.trim();
            const passportExpiry = document.querySelector('input[name="passengers[' + index + '].passportExpiryDate"]').value;

            if (passportNo && passportExpiry) {
                // 여권 만료일 검사 (오늘 기준 6개월 이후인지 권장 사항 확인)
                const minExpiry = new Date();
                minExpiry.setMonth(minExpiry.getMonth() + 6);
                if (new Date(passportExpiry) < minExpiry) {
                    const confirmResult = await Swal.fire({
                        icon: 'warning',
                        title: '여권 만료일 확인',
                        text: '탑승자 ' + pNum + '의 여권 만료일이 6개월 미만입니다. 계속하시겠습니까?',
                        confirmButtonText: '계속',
                        confirmButtonColor: '#1f6feb',
                        showCancelButton: true,
                        cancelButtonText: '취소',
                        cancelButtonColor: '#64748b'
                    });
                    if (!confirmResult.isConfirmed) return false;
                }
            }

            passengers.push({
                passengerId: passengerIdEl.value,
                krLastName, krFirstName, lastName, firstName, birth,

                gender: genderEl ? genderEl.value : '',

                email, phoneNumber, passportNo,
                country: document.querySelector('input[name="passengers[' + index + '].country"]').value,
                passportExpiryDate: document.querySelector('input[name="passengers[' + index + '].passportExpiryDate"]').value,
                passportIssueCountry: document.querySelector('input[name="passengers[' + index + '].passportIssueCountry"]').value
            });

            index++;
        }

        // 로딩 표시
        Swal.fire({
            title: '탑승자 정보를 저장하는 중...',
            allowOutsideClick: false,
            allowEscapeKey: false,
            showConfirmButton: false,
            didOpen: () => { Swal.showLoading(); }
        });

        try {
            const res = await fetchWithRefresh('/reservations/' + reservationId + '/passengers/api', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ passengers: passengers })
            });

            // 응답 상태 확인
            if (!res.ok) {
                Swal.close();
                Swal.error('서버 오류가 발생했습니다. (HTTP ' + res.status + ')', '저장 실패');
                return false;
            }

            const data = await res.json();
            Swal.close();

            if (data.success) {
                // 저장 성공 시 상태 업데이트
                passengerSaved = true;

                // 좌석 선택, 부가서비스 버튼 활성화
                document.querySelectorAll('button[onclick*="openSeatPopup"], button[onclick*="openServicePopup"]').forEach(btn => {
                    btn.disabled = false;
                });

                // 결제 버튼 활성화
                const payBtn = document.getElementById('payBtn');
                if (payBtn) payBtn.disabled = false;

                // 안내 텍스트 업데이트
                const statusSpan = document.querySelector('.text-red-500.font-bold, .text-green-600.font-bold');
                if (statusSpan) {
                    statusSpan.className = 'text-green-600 font-bold';
                    statusSpan.textContent = '저장 완료';
                }

                Swal.success('탑승자 정보가 저장되었습니다.', '저장 완료');
            } else {
                Swal.error(data.message || '오류가 발생했습니다.', '저장 실패');
            }
        } catch (err) {
            Swal.close();
            console.error('탑승자 저장 오류:', err);
            Swal.error('저장 중 오류가 발생했습니다: ' + (err.message || '알 수 없는 오류'), '저장 실패');
        }

        return false;
    }
    window.savePassengers = savePassengers;

</script>
</body>
</html>
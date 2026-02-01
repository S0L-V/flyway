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
                <form id="passengerForm" onsubmit="return savePassengers(event)">
                    <c:forEach var="p" items="${vm.passengers}" varStatus="st">
                        <div class="mb-8 last:mb-0 border-b last:border-0 border-gray-100 pb-8 last:pb-0">
                            <h4 class="font-bold text-sm text-gray-800 mb-4">탑승자 ${st.index + 1}</h4>
                            <input type="hidden" name="passengers[${st.index}].passengerId" value="${p.passengerId}"/>

                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <!-- Row 1: Korean Name -->
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">한글 성</label>
                                    <input name="passengers[${st.index}].krLastName" value="${p.krLastName}" placeholder="홍" pattern="^[가-힣]+$" required
                                           class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                </div>
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">한글 이름</label>
                                    <input name="passengers[${st.index}].krFirstName" value="${p.krFirstName}" placeholder="길동" pattern="^[가-힣]+$" required
                                           class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                </div>

                                <!-- Row 2: English Name -->
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">영문 성 (Last Name)</label>
                                    <input name="passengers[${st.index}].lastName" value="${p.lastName}" placeholder="HONG" pattern="^[A-Z\s]+$" oninput="this.value = this.value.toUpperCase()" required
                                           class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all uppercase"/>
                                </div>
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">영문 이름 (First Name)</label>
                                    <input name="passengers[${st.index}].firstName" value="${p.firstName}" placeholder="GILDONG" pattern="^[A-Z\s]+$" oninput="this.value = this.value.toUpperCase()" required
                                           class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all uppercase"/>
                                </div>

                                <!-- Row 3: Birth & Gender -->
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">생년월일</label>
                                    <input type="date" name="passengers[${st.index}].birth" value="${p.birth}" max="9999-12-31" required
                                           class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
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
                                           class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                </div>
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-slate-500 ml-0.5">이메일</label>
                                    <input type="email" name="passengers[${st.index}].email" value="${p.email}" placeholder="example@email.com" required
                                           class="w-full p-3 rounded-xl border border-gray-200 bg-gray-50 text-sm font-semibold focus:bg-white focus:border-primary focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"/>
                                </div>

                                <!-- Row 5: Passport Info -->
                                <div class="md:col-span-2 mt-2">
                                    <div class="text-xs font-bold text-gray-800 mb-3 flex items-center gap-2">
                                        여권 정보
                                        <span class="text-[11px] font-normal text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">선택사항 (나중에 등록 가능)</span>
                                    </div>
                                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
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

                <div class="flex flex-col gap-3">
                    <c:forEach var="s" items="${vm.segments}" varStatus="status">
                        <div class="flex items-center justify-between bg-slate-50 border border-slate-200 p-4 rounded-xl hover:bg-white hover:border-primary transition-all group">
                            <div>
                                <span class="inline-block px-2 py-0.5 rounded text-[11px] font-extrabold mb-1 ${status.first ? 'bg-blue-50 text-primary' : 'bg-slate-100 text-slate-600'}">
                                    ${status.first ? '가는편' : '오는편'}
                                </span>
                                <div class="text-sm font-bold text-gray-800">
                                    <c:out value="${s.snapDepartureCity}"/> → <c:out value="${s.snapArrivalCity}"/>
                                </div>
                                <div class="text-xs text-gray-500">
                                    <c:out value="${s.snapAirlineName}"/> <c:out value="${s.snapFlightNumber}"/>
                                </div>
                            </div>
                            <button type="button" ${vm.passengerSaved ? "" : "disabled"} onclick="openSeatPopup('${s.reservationSegmentId}')"
                                    class="px-4 py-2 bg-white border border-slate-300 rounded-lg text-xs font-bold text-slate-600 group-hover:bg-primary group-hover:border-primary group-hover:text-white transition-all disabled:opacity-50 disabled:cursor-not-allowed">
                                좌석 선택
                            </button>
                        </div>
                    </c:forEach>
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

                <div class="flex justify-between mb-3 text-sm text-slate-500">
                    <span>부가서비스</span>
                    <strong class="text-slate-800" id="servicePrice">₩0</strong>
                </div>

                <!-- Service Details -->
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

<script type="module">
    import { fetchWithRefresh } from '/resources/common/js/authFetch.js';
    var reservationId = '${vm.reservationId}';
    var passengerSaved = ${vm.passengerSaved};
    var passengerCount = ${vm.passengerCount};
    var segments = [
        <c:forEach var="s" items="${vm.segments}" varStatus="st">
        { snapPrice: ${s.snapPrice != null ? s.snapPrice : 0} }<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];

    // 좌석 팝업
    function openSeatPopup(segmentId) {
        if (!passengerSaved) { alert('탑승자 정보를 먼저 저장해주세요.'); return; }

        if (!segmentId) { alert('구간 정보를 찾을 수 없습니다.'); return; }
        const popupUrl = '/reservations/' + encodeURIComponent(reservationId)
            + '/segments/' + encodeURIComponent(segmentId) + '/seats';
        window.open(popupUrl, 'seatPopup', 'width=1100,height=800,scrollbars=yes,resizable=yes');
    }

    // 부가서비스 팝업 열기
    function openServicePopup() {
        if (!passengerSaved) {
            alert('탑승자 정보를 먼저 저장해주세요.');
            return;
        }

        var popupUrl = '/reservations/' + reservationId + '/services';
        var popupOption = 'width=800,height=700,scrollbars=yes,resizable=yes';
        window.open(popupUrl, 'servicePopup', popupOption);
    }

    // 부가서비스 총액 갱신 (팝업에서 호출)
    async function refreshServiceTotal() {
        try {
            const res = await fetchWithRefresh('/reservations/' + reservationId + '/services/total');
            const data = await res.json();
            if (data.success) {
                document.getElementById('servicePrice').textContent = '₩' + numberWithCommas(data.total);
                updateTotalPrice();
            }
        } catch (err) {
            console.error('Failed to refresh service total', err);
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
            alert('탑승자 정보를 먼저 저장해주세요.');
            return;
        }
        // 결제 페이지로 이동 (PaymentController)
        location.href = '/payments/' + reservationId;
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
            refreshServiceTotal();
        }
        updateTotalPrice();

        // 생년월일 최대 날짜 제한 (오늘로 설정)
        const today = new Date().toISOString().split("T")[0];
        document.querySelectorAll('input[type="date"]').forEach(el => {
            if(el.name.includes('birth')) {
                el.max = today;
            }
            if(el.name.includes('passportExpiryDate')) {
                el.min = today; // 여권만료는 오늘부터만 가능
            }
        });
    });
    window.openSeatPopup = openSeatPopup;
    window.openServicePopup = openServicePopup;
    window.refreshServiceTotal = refreshServiceTotal;
    window.goPayment = goPayment;

    async function savePassengers(event) {
        event.preventDefault();

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

            // 1. 성별 체크
            if (!genderEl) {
                alert(`탑승자 \${pNum}의 성별을 선택해주세요.`);
                return false;
            }

            // 2. 한글 이름 검사
            if (!regKr.test(krLastName) || !regKr.test(krFirstName)) {
                alert(`탑승자 \${pNum}의 한글 성명은 한글만 입력 가능합니다.`);
                return false;
            }

            // 3. 영문 이름 검사
            if (!regEn.test(lastName) || !regEn.test(firstName)) {
                alert(`탑승자 \${pNum}의 영문 성명은 영문 대문자만 입력 가능합니다.`);
                return false;
            }

            // 4. 생년월일 검사 (미래 날짜 선택 방지)
            const today = new Date().toISOString().split('T')[0];
            if (birth > today) {
                alert(`탑승자 \${pNum}의 생년월일이 올바르지 않습니다.`);
                return false;
            }

            // 5. 연락처 및 이메일 형식 검사
            if (!regPhone.test(phoneNumber)) {
                alert(`탑승자 \${pNum}의 연락처 형식이 올바르지 않습니다. (예: 01012345678)`);
                return false;
            }
            if (!regEmail.test(email)) {
                alert(`탑승자 \${pNum}의 이메일 형식이 올바르지 않습니다.`);
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
                    if(!confirm(`탑승자 \${pNum}의 여권 만료일이 6개월 미만입니다. 계속하시겠습니까?`)) return false;
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

        try {
            const res = await fetchWithRefresh('/reservations/' + reservationId + '/passengers/api', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ passengers: passengers })
            });

            const data = await res.json();

            if (data.success) {
                location.href = '/reservations/' + reservationId + '/booking?saved=1';
            } else {
                alert('저장 실패: ' + (data.message || '오류가 발생했습니다.'));
            }
        } catch (err) {
            alert('저장 실패: ' + err.message);
        }

        return false;
    }
    window.savePassengers = savePassengers;

</script>
</body>
</html>
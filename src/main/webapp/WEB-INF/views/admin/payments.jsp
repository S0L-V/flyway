<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<main class="pl-64 pt-16 min-h-screen">
    <div class="p-8 max-w-[1600px] mx-auto space-y-8">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-slate-900">결제 내역 관리</h1>
                <p class="text-slate-500">전체 결제 내역 및 통계 정보를 확인합니다.</p>
            </div>
            <button id="refresh-button" class="px-4 py-2 bg-blue-600 text-white text-sm font-bold rounded-lg shadow-lg shadow-blue-500/20 hover:bg-blue-700 transition-colors">
                <i data-lucide="refresh-cw" class="w-4 h-4 mr-2 inline-block"></i> 새로고침
            </button>
        </div>

        <!-- 결제 통계 카드 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">결제 완료 건수</span>
                    <div class="p-2 bg-green-50 text-green-600 rounded-lg"><i data-lucide="check-circle" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-paid-count" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">승인 대기 건수</span>
                    <div class="p-2 bg-yellow-50 text-yellow-600 rounded-lg"><i data-lucide="clock" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-pending-count" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">환불/취소 건수</span>
                    <div class="p-2 bg-orange-50 text-orange-600 rounded-lg"><i data-lucide="rotate-ccw" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-refunded-count" class="text-2xl font-bold text-slate-900">-</div>
            </div>
            <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-slate-500">이번 달 매출</span>
                    <div class="p-2 bg-purple-50 text-purple-600 rounded-lg"><i data-lucide="dollar-sign" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-monthly-revenue" class="text-2xl font-bold text-slate-900">-</div>
            </div>
        </div>

        <!-- 결제 내역 테이블 -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm">
            <div class="p-6 border-b border-slate-100 flex items-center justify-between">
                <h2 class="text-lg font-bold text-slate-800">결제 내역</h2>
                <div class="flex items-center space-x-3">
                    <select id="filter-status" class="p-2 border border-slate-300 rounded-lg text-sm">
                        <option value="">전체 상태</option>
                        <option value="PAID">결제 완료</option>
                        <option value="PENDING">대기 중</option>
                        <option value="FAILED">실패</option>
                        <option value="CANCELLED">취소됨</option>
                        <option value="REFUNDED">환불됨</option>
                    </select>
                    <button id="search-button" class="px-3 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600">
                        <i data-lucide="search" class="w-4 h-4 inline-block mr-1"></i> 검색
                    </button>
                </div>
            </div>
            <div class="p-6">
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-slate-200">
                        <thead class="bg-slate-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase w-64">회원 정보</th>
                            <th scope="col" class="px-4 py-3 text-right text-xs font-semibold text-slate-500 uppercase">금액</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">결제 수단</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">상태</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">대상 항공편</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">결제 시각</th>
                        </tr>
                        </thead>
                        <tbody id="payment-list-body" class="bg-white divide-y divide-slate-100">
                        <tr><td colspan="6" class="text-center py-12 text-slate-500">결제 내역을 불러오는 중...</td></tr>
                        </tbody>
                    </table>
                </div>
                <nav id="pagination-controls" class="flex items-center justify-end pt-4">
                    <!-- 페이지네이션 컨트롤이 여기에 렌더링됩니다. -->
                </nav>
            </div>
        </div>
    </div>
</main>

<!-- CountUp 라이브러리 (애니메이션용) - 인라인 스크립트보다 먼저 로드 -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/countup.js/2.8.0/countUp.umd.min.js"></script>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        lucide.createIcons();

        let currentPage = 0;
        const pageSize = 10;
        let currentFilterStatus = '';

        const elements = {
            stats: {
                paidCount: document.getElementById('stat-paid-count'),
                pendingCount: document.getElementById('stat-pending-count'),
                refundedCount: document.getElementById('stat-refunded-count'),
                monthlyRevenue: document.getElementById('stat-monthly-revenue')
            },
            filterStatus: document.getElementById('filter-status'),
            searchButton: document.getElementById('search-button'),
            refreshButton: document.getElementById('refresh-button'),
            paymentListBody: document.getElementById('payment-list-body'),
            paginationControls: document.getElementById('pagination-controls')
        };

        // 초기 데이터 로딩
        fetchPaymentStats();
        fetchPaymentList();

        // 이벤트 리스너
        elements.refreshButton.addEventListener('click', function() {
            currentPage = 0;
            fetchPaymentStats();
            fetchPaymentList();
        });

        elements.searchButton.addEventListener('click', function() {
            currentPage = 0;
            currentFilterStatus = elements.filterStatus.value;
            fetchPaymentList();
        });

        elements.filterStatus.addEventListener('change', function() {
            currentPage = 0;
            currentFilterStatus = elements.filterStatus.value;
            fetchPaymentList();
        });


        // [수정됨] 통계 데이터 가져오기 + 애니메이션 적용
        function fetchPaymentStats() {
            fetch(window.CONTEXT_PATH + '/admin/payments/api/stats')
                .then(response => response.json())
                .then(data => {
                    if (data.success && data.data) {
                        // 기존 textContent 대입 방식을 animateValue 함수 호출로 변경

                        // 1. 건수 (단순 숫자)
                        animateValue(elements.stats.paidCount, data.data.paidCount);
                        animateValue(elements.stats.pendingCount, data.data.pendingCount);
                        animateValue(elements.stats.refundedCount, data.data.refundedCount);

                        // 2. 매출 (원화 표시 옵션 추가)
                        animateValue(elements.stats.monthlyRevenue, data.data.monthlyRevenue, {
                            prefix: '₩ ',   // 앞에 원화 기호 붙이기
                        });

                    } else {
                        console.error('Failed to fetch payment stats:', data.message);
                    }
                })
                .catch(error => {
                    console.error('Error fetching payment stats:', error);
                });
        }

        function fetchPaymentList() {
            elements.paymentListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-slate-500"><i data-lucide="loader-2" class="w-8 h-8 animate-spin mx-auto mb-2"></i><p>결제 내역을 불러오는 중...</p></td></tr>';
            lucide.createIcons(); // 로딩 아이콘 렌더링

            const url = new URL(window.CONTEXT_PATH + '/admin/payments/api/list', window.location.origin);
            url.searchParams.append('page', currentPage);
            url.searchParams.append('size', pageSize);
            if (currentFilterStatus) {
                url.searchParams.append('status', currentFilterStatus);
            }

            fetch(url)
                .then(response => response.json())
                .then(data => {
                    if (data.success && data.data) {
                        renderPaymentList(data.data.list);
                        renderPagination(data.data.totalCount, data.data.currentPage, data.data.pageSize, data.data.totalPages);
                    } else {
                        console.error('Failed to fetch payment list:', data.message);
                        elements.paymentListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-slate-500">결제 내역 조회에 실패했습니다: ' + (data.message || '알 수 없는 오류') + '</td></tr>';
                    }
                    lucide.createIcons(); // 상태 아이콘 렌더링
                })
                .catch(error => {
                    console.error('Error fetching payment list:', error);
                    elements.paymentListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-red-500">결제 내역 조회 중 네트워크 오류가 발생했습니다.</td></tr>';
                    lucide.createIcons(); // 에러 아이콘 렌더링
                });
        }

        function renderPaymentList(payments) {
            elements.paymentListBody.innerHTML = ''; // 기존 내용 지우기
            if (payments.length === 0) {
                elements.paymentListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-slate-500">결제 내역이 없습니다.</td></tr>';
                return;
            }

            payments.forEach(function(payment) {
                var row = document.createElement('tr');
                row.className = 'hover:bg-slate-50';
                row.innerHTML =
                    '<td class="px-4 py-3 text-sm text-slate-600 w-64">' +
                    '<div class="font-medium">' + escapeHtml(payment.userName || '이름 미입력') + '</div>' +
                    '<div class="text-xs text-slate-500">' + escapeHtml(payment.userEmail || '-') + '</div>' +
                    '</td>' +
                    '<td class="px-4 py-3 text-sm text-slate-800 font-semibold text-right">' + formatCurrency(payment.amount) + '</td>' +
                    '<td class="px-4 py-3 text-sm text-slate-600">' + escapeHtml(payment.paymentMethodDisplay) + '</td>' +
                    '<td class="px-4 py-3 text-sm">' +
                    '<span class="px-2 py-1 text-xs font-medium rounded-full ' + payment.statusBadgeClass + '">' + escapeHtml(payment.statusDisplay) + '</span>' +
                    '</td>' +
                    '<td class="px-4 py-3 text-sm text-slate-600">' +
                    '<div class="font-medium">' + escapeHtml(payment.flightNumber || '-') + '</div>' +
                    '<div class="text-xs text-slate-500">' + escapeHtml(payment.route || '-') + '</div>' +
                    '</td>' +
                    '<td class="px-4 py-3 text-sm text-slate-600">' + formatDateTime(payment.paidAt) + '</td>';
                elements.paymentListBody.appendChild(row);
            });
        }

        function renderPagination(totalCount, curPage, pageSize, totalPages) {
            elements.paginationControls.innerHTML = ''; // 기존 내용 지우기

            // [수정 1] 총 건수 표시는 페이지 수와 상관없이 항상 실행되어야 하므로 맨 위로 올림
            var totalInfo = document.createElement('span');
            totalInfo.className = 'mr-4 text-sm text-slate-500';
            // formatNumber 함수를 사용하여 1,000 단위 콤마 적용 (선택 사항)
            totalInfo.textContent = '총 ' + formatNumber(totalCount) + '건';
            elements.paginationControls.appendChild(totalInfo);

            // [수정 2] 페이지가 1개 이하이면 버튼을 그리지 않고 여기서 종료 (총 건수는 이미 그려짐)
            if (totalPages <= 1) return;

            var ul = document.createElement('ul');
            ul.className = 'flex items-center space-x-1';

            // 이전 페이지 버튼
            var prevLi = document.createElement('li');
            var prevButton = document.createElement('button');
            prevButton.className = 'p-2 rounded-lg hover:bg-slate-100 disabled:opacity-50';
            prevButton.innerHTML = '<i data-lucide="chevron-left" class="w-4 h-4"></i>';
            prevButton.disabled = curPage === 0;
            prevButton.addEventListener('click', function() {
                if (currentPage > 0) {
                    currentPage--;
                    fetchPaymentList();
                }
            });
            prevLi.appendChild(prevButton);
            ul.appendChild(prevLi);

            // 페이지 번호 (너무 많으면 생략하는 로직 없이 단순 반복문인 상태)
            for (var i = 0; i < totalPages; i++) {
                var li = document.createElement('li');
                var button = document.createElement('button');
                button.className = 'px-3 py-1 rounded-lg text-sm font-medium transition-colors ' + (i == curPage ? 'bg-blue-600 text-white' : 'hover:bg-slate-100');
                button.textContent = i + 1;
                (function(pageIndex) {
                    button.addEventListener('click', function() {
                        currentPage = pageIndex;
                        fetchPaymentList();
                    });
                })(i);
                li.appendChild(button);
                ul.appendChild(li);
            }

            // 다음 페이지 버튼
            var nextLi = document.createElement('li');
            var nextButton = document.createElement('button');
            nextButton.className = 'p-2 rounded-lg hover:bg-slate-100 disabled:opacity-50';
            nextButton.innerHTML = '<i data-lucide="chevron-right" class="w-4 h-4"></i>';
            nextButton.disabled = curPage === totalPages - 1;
            nextButton.addEventListener('click', function() {
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    fetchPaymentList();
                }
            });
            nextLi.appendChild(nextButton);
            ul.appendChild(nextLi);

            elements.paginationControls.appendChild(ul);
            lucide.createIcons(); // 페이지네이션 아이콘 렌더링
        }

        function animateValue(element, endValue, customOptions) {
            // 값이 없으면 0으로 처리
            if (endValue === null || endValue === undefined) endValue = 0;

            // 기본 옵션 (2초 동안 실행, 천단위 콤마)
            const defaultOptions = {
                duration: 2,
                separator: ',',
            };

            // 옵션 합치기
            const options = { ...defaultOptions, ...customOptions };

            // CountUp 라이브러리 존재 여부 확인 (로딩 순서 문제 방어)
            if (typeof countUp === 'undefined' || typeof countUp.CountUp !== 'function') {
                // CountUp이 없으면 애니메이션 없이 값만 표시
                element.textContent = (options.prefix || '') + new Intl.NumberFormat('ko-KR').format(endValue);
                return;
            }

            // CountUp 인스턴스 생성 (element는 DOM 요소 자체여야 함)
            const anim = new countUp.CountUp(element, endValue, options);

            if (!anim.error) {
                anim.start();
            } else {
                console.error(anim.error);
                // 에러 발생 시 애니메이션 없이 그냥 값만 보여줌 (백업)
                element.textContent = (options.prefix || '') + new Intl.NumberFormat('ko-KR').format(endValue);
            }
        }

        // 유틸리티 함수 (dashboard.js에서 복사)
        function formatNumber(num) {
            if (num === null || num === undefined) return '0';
            return new Intl.NumberFormat('ko-KR').format(num);
        }

        function formatCurrency(amount) {
            if (amount === null || amount === undefined) return '₩ 0';
            return '₩ ' + new Intl.NumberFormat('ko-KR').format(amount);
        }

        function formatDateTime(dateString) {
            if (!dateString) return '';
            const date = new Date(dateString.replace('T', ' ')); // "2023-01-01T12:30:00" -> "2023-01-01 12:30:00"
            if (isNaN(date.getTime())) {
                // Jackson의 숫자 배열 형식 처리 [year, month, day, hour, minute, second]
                const dateParts = dateString.split(/[-\s:T]/).map(Number);
                if (dateParts.length >= 6) {
                    return new Date(dateParts[0], dateParts[1] - 1, dateParts[2], dateParts[3], dateParts[4], dateParts[5])
                        .toLocaleString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
                }
                return '잘못된 날짜';
            }
            return date.toLocaleString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
        }


        function escapeHtml(text) {
            if (text === null || text === undefined) return ''; // null 또는 undefined 처리
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

    });
</script>
</body>
</html>
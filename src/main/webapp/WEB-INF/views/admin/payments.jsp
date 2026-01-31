<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<style>
    .payment-card {
        position: relative;
        overflow: hidden;
    }
    .payment-card::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 3px;
        background: linear-gradient(90deg, #3b82f6, #8b5cf6);
        opacity: 0;
        transition: opacity 0.3s;
    }
    .payment-card:hover::before {
        opacity: 1;
    }
    .payment-card:hover {
        transform: translateY(-2px);
    }

    /* 카드 그리드 애니메이션 */
    @keyframes cardFadeIn {
        from {
            opacity: 0;
            transform: translateY(10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
    .payment-card {
        animation: cardFadeIn 0.3s ease-out forwards;
    }
    .payment-card:nth-child(1) { animation-delay: 0.02s; }
    .payment-card:nth-child(2) { animation-delay: 0.04s; }
    .payment-card:nth-child(3) { animation-delay: 0.06s; }
    .payment-card:nth-child(4) { animation-delay: 0.08s; }
    .payment-card:nth-child(5) { animation-delay: 0.10s; }
    .payment-card:nth-child(6) { animation-delay: 0.12s; }
    .payment-card:nth-child(7) { animation-delay: 0.14s; }
    .payment-card:nth-child(8) { animation-delay: 0.16s; }
    .payment-card:nth-child(9) { animation-delay: 0.18s; }
</style>

<main class="admin-bg pl-0 lg:pl-[72px] pt-16 min-h-screen transition-all duration-300 relative">
    <div class="p-8 max-w-[1600px] mx-auto space-y-8 relative z-10">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-glass-primary">결제 내역 관리</h1>
                <p class="text-glass-muted">전체 결제 내역 및 통계 정보를 확인합니다.</p>
            </div>
            <button id="refresh-button" class="glass-btn px-4 py-2 text-white text-sm font-bold">
                <i data-lucide="refresh-cw" class="w-4 h-4 mr-2 inline-block"></i> 새로고침
            </button>
        </div>

        <!-- 결제 통계 카드 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-glass-muted">결제 완료 건수</span>
                    <div class="p-2 glass-icon-emerald text-emerald-400 rounded-lg"><i data-lucide="check-circle" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-paid-count" class="stat-number">-</div>
            </div>
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-glass-muted">승인 대기 건수</span>
                    <div class="p-2 glass-icon-yellow text-yellow-400 rounded-lg"><i data-lucide="clock" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-pending-count" class="stat-number">-</div>
            </div>
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-glass-muted">환불/취소 건수</span>
                    <div class="p-2 glass-icon-rose text-rose-400 rounded-lg"><i data-lucide="rotate-ccw" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-refunded-count" class="stat-number">-</div>
            </div>
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-glass-muted">이번 달 매출</span>
                    <div class="p-2 glass-icon-purple text-purple-400 rounded-lg"><i data-lucide="dollar-sign" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-monthly-revenue" class="stat-number">-</div>
            </div>
        </div>

        <!-- 결제 내역 카드 -->
        <div class="glass-section">
            <div class="p-6 border-b border-white/5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <h2 class="text-lg font-bold text-glass-primary">결제 내역</h2>
                <div class="flex flex-wrap items-center gap-3">
                    <div class="glass-search flex items-center px-3 py-2">
                        <i data-lucide="search" class="w-4 h-4 text-glass-muted mr-2"></i>
                        <input type="text" id="search-keyword" placeholder="이름, 이메일 검색"
                               class="bg-transparent border-none text-sm text-glass-primary placeholder:text-glass-muted w-40 focus:outline-none">
                    </div>
                    <select id="filter-status" class="px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-sm text-glass-primary focus:outline-none focus:ring-2 focus:ring-blue-500/20">
                        <option value="" class="bg-slate-800">전체 상태</option>
                        <option value="PAID" class="bg-slate-800">결제 완료</option>
                        <option value="PENDING" class="bg-slate-800">대기 중</option>
                        <option value="FAILED" class="bg-slate-800">실패</option>
                        <option value="CANCELLED" class="bg-slate-800">취소됨</option>
                        <option value="REFUNDED" class="bg-slate-800">환불됨</option>
                    </select>
                </div>
            </div>
            <div class="p-6">
                <!-- 카드 그리드 -->
                <div id="payment-card-grid" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                    <!-- 로딩 상태 -->
                    <div class="col-span-full flex flex-col items-center justify-center py-16 text-glass-muted">
                        <i data-lucide="loader-2" class="w-8 h-8 animate-spin mb-3"></i>
                        <p>결제 내역을 불러오는 중...</p>
                    </div>
                </div>

                <!-- 페이지네이션 -->
                <nav id="pagination-controls" class="flex items-center justify-between pt-6 mt-6 border-t border-white/5">
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
            searchKeyword: document.getElementById('search-keyword'),
            refreshButton: document.getElementById('refresh-button'),
            paymentCardGrid: document.getElementById('payment-card-grid'),
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

        elements.filterStatus.addEventListener('change', function() {
            currentPage = 0;
            currentFilterStatus = elements.filterStatus.value;
            fetchPaymentList();
        });

        // 검색어 입력 (Enter 키 또는 디바운스)
        let searchTimeout;
        elements.searchKeyword.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(function() {
                currentPage = 0;
                fetchPaymentList();
            }, 300);
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
            elements.paymentCardGrid.innerHTML = `
                <div class="col-span-full flex flex-col items-center justify-center py-16 text-glass-muted">
                    <i data-lucide="loader-2" class="w-8 h-8 animate-spin mb-3"></i>
                    <p>결제 내역을 불러오는 중...</p>
                </div>`;
            lucide.createIcons();

            const url = new URL(window.CONTEXT_PATH + '/admin/payments/api/list', window.location.origin);
            url.searchParams.append('page', currentPage);
            url.searchParams.append('size', pageSize);
            if (currentFilterStatus) {
                url.searchParams.append('status', currentFilterStatus);
            }
            const keyword = elements.searchKeyword.value.trim();
            if (keyword) {
                url.searchParams.append('keyword', keyword);
            }

            fetch(url)
                .then(response => response.json())
                .then(data => {
                    if (data.success && data.data) {
                        renderPaymentCards(data.data.list);
                        renderPagination(data.data.totalCount, data.data.currentPage, data.data.pageSize, data.data.totalPages);
                    } else {
                        console.error('Failed to fetch payment list:', data.message);
                        elements.paymentCardGrid.innerHTML = `
                            <div class="col-span-full flex flex-col items-center justify-center py-16 text-glass-muted">
                                <i data-lucide="alert-circle" class="w-8 h-8 mb-3"></i>
                                <p>결제 내역 조회에 실패했습니다</p>
                            </div>`;
                    }
                    lucide.createIcons();
                })
                .catch(error => {
                    console.error('Error fetching payment list:', error);
                    elements.paymentCardGrid.innerHTML = `
                        <div class="col-span-full flex flex-col items-center justify-center py-16 text-rose-400">
                            <i data-lucide="wifi-off" class="w-8 h-8 mb-3"></i>
                            <p>네트워크 오류가 발생했습니다</p>
                        </div>`;
                    lucide.createIcons();
                });
        }

        function renderPaymentCards(payments) {
            elements.paymentCardGrid.innerHTML = '';

            if (payments.length === 0) {
                elements.paymentCardGrid.innerHTML = `
                    <div class="col-span-full flex flex-col items-center justify-center py-16 text-glass-muted">
                        <i data-lucide="inbox" class="w-12 h-12 mb-3"></i>
                        <p class="text-lg font-medium">결제 내역이 없습니다</p>
                    </div>`;
                lucide.createIcons();
                return;
            }

            payments.forEach(function(payment) {
                const card = document.createElement('div');
                card.className = 'payment-card glass-card p-5 hover:border-white/20 transition-all duration-300 cursor-pointer';

                const statusConfig = getStatusConfigGlass(payment.status);

                card.innerHTML = `
                    <div class="flex items-start justify-between mb-4">
                        <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white font-bold text-sm shadow-lg shadow-blue-500/30">
                                ` + escapeHtml((payment.userName || '?').charAt(0).toUpperCase()) + `
                            </div>
                            <div>
                                <p class="font-semibold text-glass-primary">` + escapeHtml(payment.userName || '이름 없음') + `</p>
                                <p class="text-xs text-glass-muted">` + escapeHtml(payment.userEmail || '-') + `</p>
                            </div>
                        </div>
                        <span class="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-semibold rounded-full ` + statusConfig.class + `">
                            <i data-lucide="` + statusConfig.icon + `" class="w-3 h-3"></i>
                            ` + escapeHtml(payment.statusDisplay) + `
                        </span>
                    </div>

                    <div class="mb-4">
                        <p class="text-2xl font-bold text-glass-primary">` + formatCurrency(payment.amount) + `</p>
                    </div>

                    <div class="space-y-2 text-sm">
                        <div class="flex items-center gap-2 text-glass-secondary">
                            <i data-lucide="plane" class="w-4 h-4 text-glass-muted"></i>
                            <span class="font-medium">` + escapeHtml(payment.flightNumber || '-') + `</span>
                            <span class="text-glass-muted">` + escapeHtml(payment.route || '') + `</span>
                        </div>
                        <div class="flex items-center gap-2 text-glass-secondary">
                            <i data-lucide="credit-card" class="w-4 h-4 text-glass-muted"></i>
                            <span>` + escapeHtml(payment.paymentMethodDisplay || '-') + `</span>
                        </div>
                        <div class="flex items-center gap-2 text-glass-muted">
                            <i data-lucide="clock" class="w-4 h-4"></i>
                            <span class="text-xs">` + formatDateTime(payment.paidAt) + `</span>
                        </div>
                    </div>
                `;

                elements.paymentCardGrid.appendChild(card);
            });

            lucide.createIcons();
        }

        // 글래스 테마용 상태 설정
        function getStatusConfigGlass(status) {
            const configs = {
                'PAID': { class: 'bg-emerald-500/20 text-emerald-400', icon: 'check-circle' },
                'PENDING': { class: 'bg-yellow-500/20 text-yellow-400', icon: 'clock' },
                'FAILED': { class: 'bg-red-500/20 text-red-400', icon: 'x-circle' },
                'CANCELLED': { class: 'bg-white/10 text-glass-secondary', icon: 'x' },
                'REFUNDED': { class: 'bg-orange-500/20 text-orange-400', icon: 'rotate-ccw' }
            };
            return configs[status] || { class: 'bg-white/10 text-glass-secondary', icon: 'help-circle' };
        }

        function getStatusConfig(status) {
            const configs = {
                'PAID': { class: 'bg-green-100 text-green-700', icon: 'check-circle' },
                'PENDING': { class: 'bg-yellow-100 text-yellow-700', icon: 'clock' },
                'FAILED': { class: 'bg-red-100 text-red-700', icon: 'x-circle' },
                'CANCELLED': { class: 'bg-slate-100 text-slate-600', icon: 'x' },
                'REFUNDED': { class: 'bg-orange-100 text-orange-700', icon: 'rotate-ccw' }
            };
            return configs[status] || { class: 'bg-slate-100 text-slate-600', icon: 'help-circle' };
        }

        function renderPagination(totalCount, curPage, pageSize, totalPages) {
            elements.paginationControls.innerHTML = '';

            // 왼쪽: 총 건수 및 현재 페이지 정보
            const infoDiv = document.createElement('div');
            infoDiv.className = 'text-sm text-glass-muted';
            const startNum = curPage * pageSize + 1;
            const endNum = Math.min((curPage + 1) * pageSize, totalCount);
            infoDiv.innerHTML = `
                <span class="font-medium text-glass-secondary">` + formatNumber(totalCount) + `</span>건 중
                <span class="font-medium text-glass-secondary">` + startNum + `-` + endNum + `</span> 표시
            `;
            elements.paginationControls.appendChild(infoDiv);

            // 페이지가 1개 이하면 버튼 생략
            if (totalPages <= 1) return;

            // 오른쪽: 페이지네이션 버튼
            const navDiv = document.createElement('div');
            navDiv.className = 'flex items-center gap-1';

            // 이전 버튼
            const prevBtn = document.createElement('button');
            prevBtn.className = 'p-2 rounded-lg text-glass-secondary hover:bg-white/10 disabled:opacity-40 disabled:cursor-not-allowed transition-colors';
            prevBtn.innerHTML = '<i data-lucide="chevron-left" class="w-4 h-4"></i>';
            prevBtn.disabled = curPage === 0;
            prevBtn.addEventListener('click', function() {
                if (currentPage > 0) { currentPage--; fetchPaymentList(); }
            });
            navDiv.appendChild(prevBtn);

            // 페이지 번호 (최대 5개 표시, 생략 기호 포함)
            const maxVisible = 5;
            let startPage = Math.max(0, curPage - Math.floor(maxVisible / 2));
            let endPage = Math.min(totalPages, startPage + maxVisible);
            if (endPage - startPage < maxVisible) {
                startPage = Math.max(0, endPage - maxVisible);
            }

            if (startPage > 0) {
                navDiv.appendChild(createPageBtn(0, curPage));
                if (startPage > 1) {
                    const dots = document.createElement('span');
                    dots.className = 'px-2 text-glass-muted';
                    dots.textContent = '...';
                    navDiv.appendChild(dots);
                }
            }

            for (let i = startPage; i < endPage; i++) {
                navDiv.appendChild(createPageBtn(i, curPage));
            }

            if (endPage < totalPages) {
                if (endPage < totalPages - 1) {
                    const dots = document.createElement('span');
                    dots.className = 'px-2 text-glass-muted';
                    dots.textContent = '...';
                    navDiv.appendChild(dots);
                }
                navDiv.appendChild(createPageBtn(totalPages - 1, curPage));
            }

            // 다음 버튼
            const nextBtn = document.createElement('button');
            nextBtn.className = 'p-2 rounded-lg text-glass-secondary hover:bg-white/10 disabled:opacity-40 disabled:cursor-not-allowed transition-colors';
            nextBtn.innerHTML = '<i data-lucide="chevron-right" class="w-4 h-4"></i>';
            nextBtn.disabled = curPage === totalPages - 1;
            nextBtn.addEventListener('click', function() {
                if (currentPage < totalPages - 1) { currentPage++; fetchPaymentList(); }
            });
            navDiv.appendChild(nextBtn);

            elements.paginationControls.appendChild(navDiv);
            lucide.createIcons();
        }

        function createPageBtn(pageIndex, currentPageIndex) {
            const btn = document.createElement('button');
            const isActive = pageIndex === currentPageIndex;
            btn.className = 'min-w-[36px] h-9 px-3 rounded-lg text-sm font-medium transition-all ' +
                (isActive
                    ? 'bg-blue-500/80 text-white shadow-lg shadow-blue-500/30'
                    : 'text-glass-secondary hover:bg-white/10');
            btn.textContent = pageIndex + 1;
            btn.addEventListener('click', function() {
                currentPage = pageIndex;
                fetchPaymentList();
            });
            return btn;
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
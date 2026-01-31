/**
 * 관리자 대시보드 UI 업데이트
 * WebSocket 데이터를 받아 화면에 렌더링
 */
const AdminDashboard = (function() {
    'use strict';

    // DOM 요소 캐시
    let elements = {};

    // 데이터 캐시
    let currentStats = null;
    let currentActivities = [];
    let currentNotifications = [];

    // 컨텍스트 경로 저장
    let basePath = '';

    // 현재 선택된 기간
    let currentPeriod = 'daily';

    /**
     * 초기화
     */
    function init(contextPath) {
        // 대시보드 페이지인지 확인 (stat-visitors 요소가 있는 경우에만)
        var isDashboardPage = document.getElementById('stat-visitors') !== null;

        if (!isDashboardPage) {
            console.log('[Dashboard] Not on dashboard page, skipping initialization');
            return;
        }

        console.log('[Dashboard] Initializing...');

        // 컨텍스트 경로 저장
        basePath = contextPath || '';

        // DOM 요소 캐시
        cacheElements();

        // WebSocket 이벤트 핸들러 등록
        registerWebSocketHandlers();

        // WebSocket 연결
        AdminWebSocket.connect(basePath);

        // 새로고침 버튼 이벤트
        bindRefreshButton();

        // 기간 선택 탭 이벤트
        bindPeriodTabs();

        // 방문자 모달 이벤트
        bindVisitorModal();

        console.log('[Dashboard] Initialized');
    }

    /**
     * DOM 요소 캐시
     */
    function cacheElements() {
        elements = {
            // 통계 카드 - 1행: 방문자, 결제 완료, 취소/환불, 매출
            visitors: document.getElementById('stat-visitors'),
            payments: document.getElementById('stat-payments'),
            cancellations: document.getElementById('stat-cancellations'),
            revenue: document.getElementById('stat-revenue'),

            // 통계 카드 - 2행: 대기 중 예약, 총 회원 수, 신규 가입, 운항 예정 항공편
            pendingReservations: document.getElementById('stat-pending-reservations'),
            totalUsers: document.getElementById('stat-total-users'),
            newUsers: document.getElementById('stat-new-users'),
            activeFlights: document.getElementById('stat-active-flights'),

            // 라벨
            labelVisitors: document.getElementById('label-visitors'),
            labelPayments: document.getElementById('label-payments'),
            labelCancellations: document.getElementById('label-cancellations'),
            labelRevenue: document.getElementById('label-revenue'),

            // 기간 선택 탭 (iOS 세그먼트)
            periodTabs: document.querySelectorAll('.ios-segment-btn'),
            periodSegment: document.getElementById('period-segment'),

            // 알림
            notificationBadge: document.getElementById('notification-badge'),
            notificationList: document.getElementById('notification-list'),
            notificationDropdown: document.getElementById('notification-dropdown'),

            // 최근 활동
            activityList: document.getElementById('activity-list'),

            // 연결 상태
            connectionStatus: document.getElementById('connection-status'),

            // 새로고침 버튼
            refreshButton: document.getElementById('refresh-button'),

            // 방문자 모달
            visitorCard: document.getElementById('visitor-card'),
            visitorModal: document.getElementById('visitor-modal'),
            visitorModalBackdrop: document.getElementById('visitor-modal-backdrop'),
            visitorModalClose: document.getElementById('visitor-modal-close'),
            visitorList: document.getElementById('visitor-list')
        };
    }

    /**
     * WebSocket 이벤트 핸들러 등록
     */
    function registerWebSocketHandlers() {
        AdminWebSocket.on('stats', updateStats);
        AdminWebSocket.on('activities', updateActivities);
        AdminWebSocket.on('notifications', updateNotifications);
        AdminWebSocket.on('connect', onConnect);
        AdminWebSocket.on('disconnect', onDisconnect);
        AdminWebSocket.on('error', onError);
    }

    /**
     * 통계 업데이트 (일일 - WebSocket)
     */
    function updateStats(stats) {
        console.log('[Dashboard] Updating stats:', stats);
        currentStats = stats;

        // 현재 기간이 daily일 때만 WebSocket 데이터로 업데이트
        // 1행: 방문자, 결제 완료, 취소/환불, 매출
        if (currentPeriod === 'daily') {
            animateValue(elements.visitors, stats.dailyVisitors);
            animateValue(elements.payments, stats.dailyPayments);
            animateValue(elements.cancellations, stats.dailyCancellations);
            animateValue(elements.revenue, stats.dailyRevenue, { prefix: '₩ ' });
        }

        // 2행: 대기 중 예약, 총 회원 수, 신규 가입, 운항 예정 항공편 (기간에 관계없이 업데이트)
        animateValue(elements.pendingReservations, stats.pendingReservations);
        animateValue(elements.totalUsers, stats.totalUsers);
        animateValue(elements.newUsers, stats.dailyNewUsers);
        animateValue(elements.activeFlights, stats.activeFlights);

        // 알림 배지
        updateNotificationBadge(stats.unreadNotifications);
    }

    /**
     * 기간별 통계 업데이트 (주간/월간 - REST API)
     * 1행: 방문자(activeUsers), 결제 완료(confirmedReservations), 취소/환불(cancelledReservations), 매출(totalRevenue)
     */
    function updatePeriodStats(stats) {
        console.log('[Dashboard] Updating period stats:', stats);

        animateValue(elements.visitors, stats.activeUsers);
        animateValue(elements.payments, stats.confirmedReservations);
        animateValue(elements.cancellations, stats.cancelledReservations);
        animateValue(elements.revenue, stats.totalRevenue, { prefix: '₩ ' });
    }

    /**
     * 기간별 라벨 업데이트
     */
    function updatePeriodLabels(period) {
        const labels = {
            daily: { visitors: '일일 방문자', payments: '결제 완료', cancellations: '취소/환불', revenue: '오늘의 매출' },
            weekly: { visitors: '주간 활성 사용자', payments: '주간 확정 예약', cancellations: '주간 취소', revenue: '주간 매출' },
            monthly: { visitors: '월간 활성 사용자', payments: '월간 확정 예약', cancellations: '월간 취소', revenue: '월간 매출' }
        };

        const label = labels[period] || labels.daily;
        updateElement(elements.labelVisitors, label.visitors);
        updateElement(elements.labelPayments, label.payments);
        updateElement(elements.labelCancellations, label.cancellations);
        updateElement(elements.labelRevenue, label.revenue);
    }

    /**
     * 기간 선택 탭 바인딩 (iOS 세그먼트)
     */
    function bindPeriodTabs() {
        console.log('[Dashboard] Binding period tabs, found:', elements.periodTabs ? elements.periodTabs.length : 0);

        // 초기 슬라이더 위치 설정
        if (elements.periodSegment) {
            elements.periodSegment.setAttribute('data-active', '0');
        }

        // querySelectorAll로 찾은 탭들에 이벤트 바인딩
        if (elements.periodTabs && elements.periodTabs.length > 0) {
            elements.periodTabs.forEach(function(tab) {
                tab.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    var period = this.getAttribute('data-period');
                    console.log('[Dashboard] Period tab clicked:', period);
                    switchPeriod(period);
                });
            });
        } else {
            // Fallback: ID로 직접 바인딩
            console.log('[Dashboard] Using fallback ID binding for period tabs');
            var dailyBtn = document.getElementById('period-daily');
            var weeklyBtn = document.getElementById('period-weekly');
            var monthlyBtn = document.getElementById('period-monthly');

            if (dailyBtn) {
                dailyBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    switchPeriod('daily');
                });
            }
            if (weeklyBtn) {
                weeklyBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    switchPeriod('weekly');
                });
            }
            if (monthlyBtn) {
                monthlyBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    switchPeriod('monthly');
                });
            }
        }
    }

    /**
     * 기간 전환 (iOS 슬라이딩 애니메이션)
     */
    function switchPeriod(period) {
        console.log('[Dashboard] switchPeriod called with:', period, 'current:', currentPeriod);

        if (currentPeriod === period) {
            console.log('[Dashboard] Same period, skipping');
            return;
        }

        currentPeriod = period;
        console.log('[Dashboard] Switching to period:', period);

        // iOS 세그먼트 슬라이더 업데이트
        var segmentGroup = document.getElementById('period-segment');
        var buttons = document.querySelectorAll('.ios-segment-btn');

        buttons.forEach(function(btn) {
            if (btn.getAttribute('data-period') === period) {
                btn.classList.add('active');
                // 슬라이더 위치 업데이트 (data-active 속성 사용)
                var index = btn.getAttribute('data-index');
                if (segmentGroup) {
                    segmentGroup.setAttribute('data-active', index);
                }
            } else {
                btn.classList.remove('active');
            }
        });

        // 라벨 업데이트
        updatePeriodLabels(period);

        // 데이터 로드
        if (period === 'daily') {
            // 일일은 WebSocket 데이터 사용
            if (currentStats) {
                updateStats(currentStats);
            }
        } else {
            // 주간/월간은 REST API 호출
            fetchPeriodStats(period.toUpperCase());
        }
    }

    /**
     * 기간별 통계 API 호출
     */
    function fetchPeriodStats(period) {
        // 로딩 표시 (1행만 업데이트)
        updateElement(elements.visitors, '-');
        updateElement(elements.payments, '-');
        updateElement(elements.cancellations, '-');
        updateElement(elements.revenue, '-');

        fetch(basePath + '/admin/api/dashboard/stats/' + period, {
            method: 'GET',
            credentials: 'same-origin'
        })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success && data.data) {
                    updatePeriodStats(data.data);
                } else {
                    console.warn('[Dashboard] No period stats available');
                    updateElement(elements.visitors, '0');
                    updateElement(elements.payments, '0');
                    updateElement(elements.cancellations, '0');
                    updateElement(elements.revenue, '₩ 0');
                }
            })
            .catch(function(error) {
                console.error('[Dashboard] Failed to fetch period stats:', error);
            });
    }

    /**
     * 최근 활동 업데이트
     */
    function updateActivities(activities) {
        console.log('[Dashboard] Updating activities:', activities.length);
        currentActivities = activities;

        if (!elements.activityList) return;

        if (activities.length === 0) {
            elements.activityList.innerHTML = `
                <div class="text-center text-glass-muted py-8">
                    최근 활동이 없습니다.
                </div>
            `;
            return;
        }

        const html = activities.map(activity => {
            const icon = getActivityIcon(activity.activityType);
            const statusBadge = getStatusBadgeGlass(activity.status);
            const timeBadge = getTimeBadge(activity.createdAt);

            return `
                <div class="glass-activity-item flex items-start gap-3">
                    <div class="p-2.5 rounded-xl ${icon.glassBg} ${icon.textColor} flex-shrink-0">
                        <i data-lucide="${icon.name}" class="w-5 h-5"></i>
                    </div>
                    <div class="flex-1 min-w-0 overflow-hidden">
                        <div class="flex items-center gap-2 flex-wrap">
                            <span class="font-medium text-glass-primary text-sm">${escapeHtml(activity.description)}</span>
                            ${statusBadge}
                        </div>
                        <div class="text-xs text-glass-muted mt-0.5 truncate">
                            ${escapeHtml(activity.userName)} · ${escapeHtml(activity.userEmail)}
                        </div>
                        <div class="flex items-center justify-between mt-1.5">
                            ${timeBadge}
                            ${activity.amount ? `<span class="text-sm font-bold text-emerald-400">${formatCurrency(activity.amount)}</span>` : ''}
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        elements.activityList.innerHTML = html;

        // Lucide 아이콘 재초기화
        if (typeof lucide !== 'undefined') {
            lucide.createIcons();
        }
    }

    /**
     * 알림 업데이트
     */
    function updateNotifications(notifications) {
        console.log('[Dashboard] Updating notifications:', notifications.length);
        currentNotifications = notifications;

        if (!elements.notificationList) return;

        if (notifications.length === 0) {
            elements.notificationList.innerHTML = `
                <div class="text-center text-slate-400 py-4 px-4">
                    알림이 없습니다.
                </div>
            `;
            return;
        }

        const html = notifications.map(notification => {
            const icon = getNotificationIcon(notification.notificationType);
            const isUnread = notification.isRead === 'N';
            const timeAgo = formatTimeAgo(notification.createdAt);

            return `
                <div class="notification-item p-4 ${isUnread ? 'bg-blue-50' : ''} hover:bg-slate-50 cursor-pointer border-b border-slate-100 last:border-b-0"
                     data-notification-id="${escapeHtml(notification.notificationId)}">
                    <div class="flex items-start gap-3">
                        <div class="p-1.5 ${icon.bgColor} ${icon.textColor} rounded-full">
                            <i data-lucide="${icon.name}" class="w-4 h-4"></i>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="font-medium text-sm text-slate-800 ${isUnread ? 'font-semibold' : ''}">${escapeHtml(notification.title)}</div>
                            <div class="text-xs text-slate-500 mt-0.5 truncate">${escapeHtml(notification.message)}</div>
                            <div class="text-xs text-slate-400 mt-1">${timeAgo}</div>
                        </div>
                        ${isUnread ? '<span class="w-2 h-2 bg-blue-500 rounded-full"></span>' : ''}
                    </div>
                </div>
            `;
        }).join('');

        elements.notificationList.innerHTML = html;

        // 이벤트 위임으로 클릭 처리 (XSS 방지)
        elements.notificationList.querySelectorAll('.notification-item').forEach(function(item) {
            item.addEventListener('click', function() {
                var id = this.getAttribute('data-notification-id');
                if (id) {
                    markAsRead(id);
                }
            });
        });

        // Lucide 아이콘 재초기화
        if (typeof lucide !== 'undefined') {
            lucide.createIcons();
        }
    }

    /**
     * 알림 배지 업데이트
     */
    function updateNotificationBadge(count) {
        if (!elements.notificationBadge) return;

        if (count > 0) {
            elements.notificationBadge.textContent = count > 99 ? '99+' : count;
            elements.notificationBadge.classList.remove('hidden');
        } else {
            elements.notificationBadge.classList.add('hidden');
        }
    }

    /**
     * 알림 읽음 처리
     */
    function markAsRead(notificationId) {
        fetch(basePath + '/admin/api/dashboard/notifications/' + encodeURIComponent(notificationId) + '/read', {
            method: 'POST',
            credentials: 'same-origin'
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // WebSocket으로 새 데이터 요청
                    AdminWebSocket.requestNotifications();
                    AdminWebSocket.requestStats();
                }
            })
            .catch(error => console.error('[Dashboard] Failed to mark as read:', error));
    }

    /**
     * 모든 알림 읽음 처리
     */
    function markAllAsRead() {
        fetch(basePath + '/admin/api/dashboard/notifications/read-all', {
            method: 'POST',
            credentials: 'same-origin'
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    AdminWebSocket.requestNotifications();
                    AdminWebSocket.requestStats();
                }
            })
            .catch(error => console.error('[Dashboard] Failed to mark all as read:', error));
    }

    /**
     * 연결 성공 시
     */
    function onConnect() {
        console.log('[Dashboard] WebSocket connected');
        updateConnectionStatus(true);
    }

    /**
     * 연결 해제 시
     */
    function onDisconnect() {
        console.log('[Dashboard] WebSocket disconnected');
        updateConnectionStatus(false);
    }

    /**
     * 에러 발생 시
     */
    function onError(error) {
        console.error('[Dashboard] WebSocket error:', error);
    }

    /**
     * 연결 상태 표시 업데이트
     */
    function updateConnectionStatus(connected) {
        if (!elements.connectionStatus) return;

        // 대시보드에서만 표시 (hidden 제거, flex 추가)
        elements.connectionStatus.classList.remove('hidden');
        elements.connectionStatus.classList.add('flex');

        if (connected) {
            elements.connectionStatus.innerHTML = '<span class="w-2 h-2 bg-emerald-400 rounded-full"></span><span class="text-emerald-400 text-xs">실시간 연결됨</span>';
        } else {
            elements.connectionStatus.innerHTML = '<span class="w-2 h-2 bg-rose-400 rounded-full animate-pulse"></span><span class="text-rose-400 text-xs">연결 끊김</span>';
        }
    }

    /**
     * 새로고침 버튼 바인딩
     */
    function bindRefreshButton() {
        if (!elements.refreshButton) return;

        elements.refreshButton.addEventListener('click', function() {
            // WebSocket 데이터 요청
            AdminWebSocket.requestStats();
            AdminWebSocket.requestActivities();
            AdminWebSocket.requestNotifications();

            // 현재 기간이 daily가 아니면 REST API도 호출
            if (currentPeriod !== 'daily') {
                fetchPeriodStats(currentPeriod.toUpperCase());
            }

            // 버튼 피드백
            this.disabled = true;
            this.innerHTML = '<i data-lucide="loader-2" class="w-4 h-4 animate-spin"></i> 새로고침 중...';

            setTimeout(function() {
                elements.refreshButton.disabled = false;
                elements.refreshButton.innerHTML = '새로고침';
                if (typeof lucide !== 'undefined') {
                    lucide.createIcons();
                }
            }, 1000);
        });
    }

    /**
     * 방문자 모달 바인딩
     */
    function bindVisitorModal() {
        // 방문자 카드 클릭 -> 모달 열기
        if (elements.visitorCard) {
            elements.visitorCard.addEventListener('click', function() {
                openVisitorModal();
            });
        }

        // 모달 닫기 버튼
        if (elements.visitorModalClose) {
            elements.visitorModalClose.addEventListener('click', closeVisitorModal);
        }

        // 모달 배경 클릭 -> 닫기
        if (elements.visitorModalBackdrop) {
            elements.visitorModalBackdrop.addEventListener('click', closeVisitorModal);
        }

        // ESC 키 -> 모달 닫기
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && elements.visitorModal && !elements.visitorModal.classList.contains('hidden')) {
                closeVisitorModal();
            }
        });
    }

    /**
     * 방문자 모달 열기
     */
    function openVisitorModal() {
        if (!elements.visitorModal) return;

        elements.visitorModal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';

        // 방문자 목록 조회
        fetchVisitors();
    }

    /**
     * 방문자 모달 닫기
     */
    function closeVisitorModal() {
        if (!elements.visitorModal) return;

        elements.visitorModal.classList.add('hidden');
        document.body.style.overflow = '';
    }

    /**
     * 방문자 목록 조회
     */
    function fetchVisitors() {
        if (!elements.visitorList) return;

        // 로딩 표시 (glass theme)
        elements.visitorList.innerHTML = `
            <div class="text-center text-glass-muted py-12">
                <i data-lucide="loader-2" class="w-8 h-8 animate-spin mx-auto mb-2"></i>
                <p>방문자 목록을 불러오는 중...</p>
            </div>
        `;
        if (typeof lucide !== 'undefined') {
            lucide.createIcons();
        }

        fetch(basePath + '/admin/api/dashboard/visitors?limit=50', {
            method: 'GET',
            credentials: 'same-origin'
        })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success && data.data) {
                    renderVisitors(data.data);
                } else {
                    elements.visitorList.innerHTML = `
                        <div class="text-center text-glass-muted py-12">
                            <i data-lucide="users" class="w-8 h-8 mx-auto mb-2"></i>
                            <p>오늘 방문자가 없습니다.</p>
                        </div>
                    `;
                }
                if (typeof lucide !== 'undefined') {
                    lucide.createIcons();
                }
            })
            .catch(function(error) {
                console.error('[Dashboard] Failed to fetch visitors:', error);
                elements.visitorList.innerHTML = `
                    <div class="text-center text-rose-400 py-12">
                        <i data-lucide="alert-circle" class="w-8 h-8 mx-auto mb-2"></i>
                        <p>방문자 목록을 불러오지 못했습니다.</p>
                    </div>
                `;
                if (typeof lucide !== 'undefined') {
                    lucide.createIcons();
                }
            });
    }

    /**
     * 방문자 목록 렌더링 (glass theme)
     */
    function renderVisitors(visitors) {
        if (!elements.visitorList || visitors.length === 0) {
            elements.visitorList.innerHTML = `
                <div class="text-center text-glass-muted py-12">
                    <i data-lucide="users" class="w-8 h-8 mx-auto mb-2"></i>
                    <p>오늘 방문자가 없습니다.</p>
                </div>
            `;
            return;
        }

        var html = '<div class="overflow-x-auto">';
        html += '<table class="min-w-full divide-y divide-white/5">';
        html += '<thead class="bg-white/5">';
        html += '<tr>';
        html += '<th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">시간</th>';
        html += '<th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">사용자</th>';
        html += '<th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">IP 주소</th>';
        html += '<th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">페이지</th>';
        html += '</tr>';
        html += '</thead>';
        html += '<tbody class="divide-y divide-white/5">';

        visitors.forEach(function(visitor) {
            var timeStr = formatTimeAgo(visitor.visitedAt);
            var userName = visitor.userName && visitor.userName.trim() ? escapeHtml(visitor.userName) : '<span class="text-glass-muted">비회원</span>';
            var userEmail = visitor.userEmail ? '<div class="text-xs text-glass-muted">' + escapeHtml(visitor.userEmail) + '</div>' : '';
            var pageUrl = visitor.pageUrl || ''; // null 체크
            var displayPageUrl = pageUrl;
            var titleAttribute = pageUrl ? escapeHtml(pageUrl) : ''; // title에는 전체 URL

            if (displayPageUrl.length > 30) {
                displayPageUrl = displayPageUrl.substring(0, 30) + '...'; // 원본 자르기
            }
            displayPageUrl = escapeHtml(displayPageUrl); // 자른 후 이스케이프

            html += '<tr class="hover:bg-white/5">';
            html += '<td class="px-4 py-3 text-sm text-glass-secondary whitespace-nowrap">' + timeStr + '</td>';
            html += '<td class="px-4 py-3 text-glass-primary">' + userName + userEmail + '</td>';
            html += '<td class="px-4 py-3 text-sm text-glass-secondary font-mono">' + escapeHtml(visitor.ipAddress) + '</td>';
            html += '<td class="px-4 py-3 text-sm text-glass-secondary" title="' + titleAttribute + '">' + displayPageUrl + '</td>';
            html += '</tr>';
        });

        html += '</tbody>';
        html += '</table>';
        html += '</div>';

        elements.visitorList.innerHTML = html;
    }

    // === 유틸리티 함수 ===

    function updateElement(el, value) {
        if (el) {
            el.textContent = value;
        }
    }

    function formatNumber(num) {
        if (num === null || num === undefined) return '0';
        return new Intl.NumberFormat('ko-KR').format(num);
    }

    function formatCurrency(amount) {
        if (amount === null || amount === undefined) return '₩ 0';
        return '₩ ' + new Intl.NumberFormat('ko-KR').format(amount);
    }

    /**
     * 숫자 애니메이션 (CountUp.js 사용)
     */
    function animateValue(element, endValue, customOptions) {
        if (!element) return;

        // 값이 없으면 0으로 처리
        if (endValue === null || endValue === undefined) endValue = 0;

        // 기본 옵션
        var defaultOptions = {
            duration: 1.5,
            separator: ','
        };

        // 옵션 합치기
        var options = Object.assign({}, defaultOptions, customOptions);

        // CountUp 라이브러리 존재 여부 확인
        if (typeof countUp === 'undefined' || typeof countUp.CountUp !== 'function') {
            // CountUp이 없으면 애니메이션 없이 값만 표시
            element.textContent = (options.prefix || '') + new Intl.NumberFormat('ko-KR').format(endValue);
            return;
        }

        // CountUp 인스턴스 생성
        var anim = new countUp.CountUp(element, endValue, options);

        if (!anim.error) {
            anim.start();
        } else {
            console.error(anim.error);
            element.textContent = (options.prefix || '') + new Intl.NumberFormat('ko-KR').format(endValue);
        }
    }

    function formatTimeAgo(dateInput) {
        if (!dateInput) return '';

        let date;
        if (typeof dateInput === 'string') {
            date = new Date(dateInput.replace('T', ' '));
        } else if (Array.isArray(dateInput)) {
            date = new Date(
                dateInput[0],
                (dateInput[1] || 1) - 1,
                dateInput[2] || 1,
                dateInput[3] || 0,
                dateInput[4] || 0,
                dateInput[5] || 0
            );
        } else {
            date = new Date(dateInput);
        }

        if (isNaN(date.getTime())) {
            return '';
        }

        const now = new Date();
        const diff = Math.floor((now - date) / 1000);

        // 트렌디한 짧은 형식
        if (diff < 30) return 'now';
        if (diff < 60) return diff + 's';
        if (diff < 3600) return Math.floor(diff / 60) + 'm';
        if (diff < 86400) return Math.floor(diff / 3600) + 'h';
        if (diff < 604800) return Math.floor(diff / 86400) + 'd';

        return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
    }

    /**
     * 트렌디한 시간 배지 HTML 생성
     */
    function getTimeBadge(dateInput) {
        const timeAgo = formatTimeAgo(dateInput);
        if (!timeAgo) return '';

        // "now"이면 초록색 펄스 도트
        if (timeAgo === 'now') {
            return `<span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 text-xs font-medium">
                <span class="w-1.5 h-1.5 bg-emerald-400 rounded-full animate-pulse"></span>now
            </span>`;
        }

        // 1시간 이내면 파란색 배지
        if (timeAgo.endsWith('s') || timeAgo.endsWith('m')) {
            return `<span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-blue-500/20 text-blue-400 text-xs font-medium">
                <i data-lucide="clock" class="w-3 h-3"></i>${timeAgo}
            </span>`;
        }

        // 그 외는 기본 스타일
        return `<span class="inline-flex items-center gap-1 text-xs text-glass-muted">
            <i data-lucide="clock" class="w-3 h-3"></i>${timeAgo}
        </span>`;
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function getActivityIcon(type) {
        switch (type) {
            case 'RESERVATION':
                return { name: 'ticket', bgColor: 'bg-blue-50', textColor: 'text-blue-600', glassBg: 'glass-icon-blue' };
            case 'PAYMENT':
                return { name: 'credit-card', bgColor: 'bg-emerald-50', textColor: 'text-emerald-600', glassBg: 'glass-icon-emerald' };
            case 'REFUND':
                return { name: 'rotate-ccw', bgColor: 'bg-orange-50', textColor: 'text-orange-600', glassBg: 'glass-icon-yellow' };
            case 'CANCELLATION':
                return { name: 'x-circle', bgColor: 'bg-rose-50', textColor: 'text-rose-600', glassBg: 'glass-icon-rose' };
            default:
                return { name: 'activity', bgColor: 'bg-slate-50', textColor: 'text-slate-600', glassBg: 'glass-icon-indigo' };
        }
    }

    function getStatusBadge(status) {
        switch (status) {
            case 'PAID':
            case 'CONFIRMED':
            case 'APPROVED':
                return '<span class="px-2 py-0.5 text-xs font-medium bg-emerald-100 text-emerald-700 rounded-full">완료</span>';
            case 'PENDING':
                return '<span class="px-2 py-0.5 text-xs font-medium bg-yellow-100 text-yellow-700 rounded-full">대기</span>';
            case 'HELD':
                return '<span class="px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-700 rounded-full">좌석 선점</span>';
            case 'CANCELLED':
            case 'REFUNDED':
            case 'EXPIRED':
                return '<span class="px-2 py-0.5 text-xs font-medium bg-rose-100 text-rose-700 rounded-full">취소</span>';
            default:
                return '';
        }
    }

    function getStatusBadgeGlass(status) {
        switch (status) {
            case 'PAID':
            case 'CONFIRMED':
            case 'APPROVED':
                return '<span class="px-2 py-0.5 text-xs font-medium bg-emerald-500/20 text-emerald-400 rounded-full border border-emerald-500/30">완료</span>';
            case 'PENDING':
                return '<span class="px-2 py-0.5 text-xs font-medium bg-yellow-500/20 text-yellow-400 rounded-full border border-yellow-500/30">대기</span>';
            case 'HELD':
                return '<span class="px-2 py-0.5 text-xs font-medium bg-blue-500/20 text-blue-400 rounded-full border border-blue-500/30">좌석 선점</span>';
            case 'CANCELLED':
            case 'REFUNDED':
            case 'EXPIRED':
                return '<span class="px-2 py-0.5 text-xs font-medium bg-rose-500/20 text-rose-400 rounded-full border border-rose-500/30">취소</span>';
            default:
                return '';
        }
    }

    function getNotificationIcon(type) {
        switch (type) {
            case 'NEW_RESERVATION':
                return { name: 'ticket', bgColor: 'bg-blue-100', textColor: 'text-blue-600' };
            case 'REFUND_REQUEST':
                return { name: 'rotate-ccw', bgColor: 'bg-orange-100', textColor: 'text-orange-600' };
            case 'PAYMENT_FAILED':
                return { name: 'alert-circle', bgColor: 'bg-rose-100', textColor: 'text-rose-600' };
            case 'SYSTEM_ALERT':
                return { name: 'alert-triangle', bgColor: 'bg-yellow-100', textColor: 'text-yellow-600' };
            default:
                return { name: 'bell', bgColor: 'bg-slate-100', textColor: 'text-slate-600' };
        }
    }

    // Public API
    return {
        init: init,
        markAsRead: markAsRead,
        markAllAsRead: markAllAsRead,
        switchPeriod: switchPeriod,
        fetchPeriodStats: fetchPeriodStats,
        getStats: function() { return currentStats; },
        getActivities: function() { return currentActivities; },
        getNotifications: function() { return currentNotifications; }
    };

})();

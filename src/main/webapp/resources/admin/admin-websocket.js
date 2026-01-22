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

    /**
     * 초기화
     */
    function init(contextPath) {
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

        console.log('[Dashboard] Initialized');
    }

    /**
     * DOM 요소 캐시
     */
    function cacheElements() {
        elements = {
            // 통계 카드
            dailyVisitors: document.getElementById('stat-daily-visitors'),
            dailyReservations: document.getElementById('stat-daily-reservations'),
            dailyCancellations: document.getElementById('stat-daily-cancellations'),
            dailyRevenue: document.getElementById('stat-daily-revenue'),
            dailyPayments: document.getElementById('stat-daily-payments'),
            totalUsers: document.getElementById('stat-total-users'),
            activeFlights: document.getElementById('stat-active-flights'),
            pendingReservations: document.getElementById('stat-pending-reservations'),
            pendingPayments: document.getElementById('stat-pending-payments'),

            // 알림
            notificationBadge: document.getElementById('notification-badge'),
            notificationList: document.getElementById('notification-list'),
            notificationDropdown: document.getElementById('notification-dropdown'),

            // 최근 활동
            activityList: document.getElementById('activity-list'),

            // 연결 상태
            connectionStatus: document.getElementById('connection-status'),

            // 새로고침 버튼
            refreshButton: document.getElementById('refresh-button')
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
     * 통계 업데이트
     */
    function updateStats(stats) {
        console.log('[Dashboard] Updating stats:', stats);
        currentStats = stats;

        // 오늘 통계
        updateElement(elements.dailyVisitors, formatNumber(stats.dailyVisitors));
        updateElement(elements.dailyReservations, formatNumber(stats.dailyReservations));
        updateElement(elements.dailyCancellations, formatNumber(stats.dailyCancellations));
        updateElement(elements.dailyRevenue, formatCurrency(stats.dailyRevenue));
        updateElement(elements.dailyPayments, formatNumber(stats.dailyPayments));

        // 전체 통계
        updateElement(elements.totalUsers, formatNumber(stats.totalUsers));
        updateElement(elements.activeFlights, formatNumber(stats.activeFlights));

        // 실시간 상태
        updateElement(elements.pendingReservations, formatNumber(stats.pendingReservations));
        updateElement(elements.pendingPayments, formatNumber(stats.pendingPayments));

        // 알림 배지
        updateNotificationBadge(stats.unreadNotifications);
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
                <div class="text-center text-slate-400 py-8">
                    최근 활동이 없습니다.
                </div>
            `;
            return;
        }

        const html = activities.map(activity => {
            const icon = getActivityIcon(activity.activityType);
            const statusBadge = getStatusBadge(activity.status);
            const timeAgo = formatTimeAgo(activity.createdAt);

            return `
                <div class="flex items-start gap-4 p-4 hover:bg-slate-50 rounded-lg transition-colors">
                    <div class="p-2 ${icon.bgColor} ${icon.textColor} rounded-lg">
                        <i data-lucide="${icon.name}" class="w-5 h-5"></i>
                    </div>
                    <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2">
                            <span class="font-medium text-slate-800 truncate">${escapeHtml(activity.description)}</span>
                            ${statusBadge}
                        </div>
                        <div class="text-sm text-slate-500 truncate">
                            ${escapeHtml(activity.userName)} · ${escapeHtml(activity.userEmail)}
                        </div>
                        <div class="text-xs text-slate-400 mt-1">${timeAgo}</div>
                    </div>
                    ${activity.amount ? `<div class="text-sm font-semibold text-slate-700">${formatCurrency(activity.amount)}</div>` : ''}
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

        if (connected) {
            elements.connectionStatus.innerHTML = `
                <span class="w-2 h-2 bg-green-500 rounded-full"></span>
                <span class="text-green-600 text-xs">실시간 연결됨</span>
            `;
        } else {
            elements.connectionStatus.innerHTML = `
                <span class="w-2 h-2 bg-red-500 rounded-full animate-pulse"></span>
                <span class="text-red-600 text-xs">연결 끊김</span>
            `;
        }
    }

    /**
     * 새로고침 버튼 바인딩
     */
    function bindRefreshButton() {
        if (!elements.refreshButton) return;

        elements.refreshButton.addEventListener('click', function() {
            AdminWebSocket.requestStats();
            AdminWebSocket.requestActivities();
            AdminWebSocket.requestNotifications();

            // 버튼 피드백
            this.disabled = true;
            this.innerHTML = '<i data-lucide="loader-2" class="w-4 h-4 animate-spin"></i> 새로고침 중...';

            setTimeout(() => {
                this.disabled = false;
                this.innerHTML = '데이터 새로고침';
                if (typeof lucide !== 'undefined') {
                    lucide.createIcons();
                }
            }, 1000);
        });
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
        if (amount >= 100000000) {
            return '₩ ' + (amount / 100000000).toFixed(1) + '억';
        }
        if (amount >= 10000) {
            return '₩ ' + (amount / 10000).toFixed(0) + '만';
        }
        return '₩ ' + new Intl.NumberFormat('ko-KR').format(amount);
    }

    function formatTimeAgo(dateString) {
        if (!dateString) return '';

        const date = new Date(dateString);
        const now = new Date();
        const diff = Math.floor((now - date) / 1000);

        if (diff < 60) return '방금 전';
        if (diff < 3600) return Math.floor(diff / 60) + '분 전';
        if (diff < 86400) return Math.floor(diff / 3600) + '시간 전';
        if (diff < 604800) return Math.floor(diff / 86400) + '일 전';

        return date.toLocaleDateString('ko-KR');
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
                return { name: 'ticket', bgColor: 'bg-blue-50', textColor: 'text-blue-600' };
            case 'PAYMENT':
                return { name: 'credit-card', bgColor: 'bg-emerald-50', textColor: 'text-emerald-600' };
            case 'REFUND':
                return { name: 'rotate-ccw', bgColor: 'bg-orange-50', textColor: 'text-orange-600' };
            case 'CANCELLATION':
                return { name: 'x-circle', bgColor: 'bg-rose-50', textColor: 'text-rose-600' };
            default:
                return { name: 'activity', bgColor: 'bg-slate-50', textColor: 'text-slate-600' };
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
                return '<span class="px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-700 rounded-full">보류</span>';
            case 'CANCELLED':
            case 'REFUNDED':
            case 'EXPIRED':
                return '<span class="px-2 py-0.5 text-xs font-medium bg-rose-100 text-rose-700 rounded-full">취소</span>';
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
        getStats: function() { return currentStats; },
        getActivities: function() { return currentActivities; },
        getNotifications: function() { return currentNotifications; }
    };

})();

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 fixed top-0 left-64 right-0 z-40">
    <div class="flex items-center gap-6">
        <!-- 검색 -->
        <div class="flex items-center w-80 relative">
            <i data-lucide="search" class="absolute left-3 text-slate-400 w-4 h-4"></i>
            <input type="text" placeholder="검색어를 입력하세요..."
                   class="w-full bg-slate-50 border-none rounded-md py-2 pl-10 pr-4 text-sm focus:ring-2 focus:ring-blue-500/20 outline-none">
        </div>

        <!-- 연결 상태 (대시보드에서만 표시) -->
        <div id="connection-status" class="hidden items-center gap-1.5">
            <span class="w-2 h-2 bg-slate-300 rounded-full"></span>
            <span class="text-slate-400 text-xs">연결 중...</span>
        </div>
    </div>

    <div class="flex items-center gap-4">
        <!-- 알림 버튼 및 드롭다운 -->
        <div class="relative" id="notification-container">
            <button id="notification-button" class="relative p-2 text-slate-500 hover:bg-slate-100 rounded-full">
                <i data-lucide="bell" class="w-5 h-5"></i>
                <span id="notification-badge" class="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] flex items-center justify-center text-[10px] font-bold text-white bg-red-500 rounded-full border-2 border-white hidden">0</span>
            </button>

            <!-- 알림 드롭다운 -->
            <div id="notification-dropdown" class="hidden absolute right-0 top-12 w-80 bg-white rounded-xl shadow-xl border border-slate-200 z-50">
                <div class="flex items-center justify-between p-4 border-b border-slate-100">
                    <span class="font-semibold text-slate-800">알림</span>
                    <button onclick="TopbarNotifications.markAllAsRead()" class="text-xs text-blue-600 hover:text-blue-700">모두 읽음</button>
                </div>
                <div id="notification-list" class="max-h-80 overflow-y-auto">
                    <div class="text-center text-slate-400 py-8">
                        알림을 불러오는 중...
                    </div>
                </div>
            </div>
        </div>

        <!-- 관리자 정보 -->
        <div class="flex items-center gap-3 ml-2 pl-4 border-l border-slate-200">
            <div class="text-right hidden sm:block">
                <p class="text-sm font-semibold text-slate-700">${sessionScope.adminName != null ? sessionScope.adminName : '관리자'}</p>
                <p class="text-[10px] text-slate-400 uppercase font-extrabold tracking-tight">${sessionScope.role != null ? sessionScope.role : 'ADMIN'}</p>
            </div>
            <div class="w-9 h-9 bg-slate-200 rounded-xl overflow-hidden border-2 border-slate-100">
                <img src="https://picsum.photos/id/64/100" alt="profile" class="w-full h-full object-cover">
            </div>
        </div>
    </div>
</header>

<!-- 알림 드롭다운 토글 및 로더 스크립트 -->
<script>
    (function() {
        'use strict';

        var basePath = '${pageContext.request.contextPath}';
        var notificationList = null;
        var notificationBadge = null;

        document.addEventListener('DOMContentLoaded', function() {
            var btn = document.getElementById('notification-button');
            var dropdown = document.getElementById('notification-dropdown');
            var container = document.getElementById('notification-container');
            notificationList = document.getElementById('notification-list');
            notificationBadge = document.getElementById('notification-badge');

            if (btn && dropdown) {
                btn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    dropdown.classList.toggle('hidden');
                });

                document.addEventListener('click', function(e) {
                    if (!container.contains(e.target)) {
                        dropdown.classList.add('hidden');
                    }
                });
            }

            // 대시보드가 아닌 페이지에서 알림 로드 (대시보드는 WebSocket으로 처리)
            var isDashboardPage = document.getElementById('stat-visitors') !== null;
            if (!isDashboardPage) {
                loadNotifications();
            }
        });

        // 알림 로드 함수
        function loadNotifications() {
            fetch(basePath + '/admin/api/dashboard/notifications', {
                method: 'GET',
                credentials: 'same-origin'
            })
                .then(function(response) { return response.json(); })
                .then(function(data) {
                    if (data.success && data.data) {
                        renderNotifications(data.data);
                    } else {
                        renderEmptyNotifications();
                    }
                })
                .catch(function(error) {
                    console.error('[Topbar] Failed to load notifications:', error);
                    renderEmptyNotifications();
                });
        }

        // 알림 렌더링
        function renderNotifications(notifications) {
            if (!notificationList) return;

            // 읽지 않은 알림 수 계산
            var unreadCount = notifications.filter(function(n) { return n.isRead === 'N'; }).length;
            updateBadge(unreadCount);

            if (notifications.length === 0) {
                renderEmptyNotifications();
                return;
            }

            var html = notifications.map(function(notification) {
                var icon = getNotificationIcon(notification.notificationType);
                var isUnread = notification.isRead === 'N';
                var timeAgo = formatTimeAgo(notification.createdAt);

                return '<div class="notification-item p-4 ' + (isUnread ? 'bg-blue-50' : '') + ' hover:bg-slate-50 cursor-pointer border-b border-slate-100 last:border-b-0" data-notification-id="' + escapeHtml(notification.notificationId) + '">' +
                    '<div class="flex items-start gap-3">' +
                    '<div class="p-1.5 ' + icon.bgColor + ' ' + icon.textColor + ' rounded-full">' +
                    '<i data-lucide="' + icon.name + '" class="w-4 h-4"></i>' +
                    '</div>' +
                    '<div class="flex-1 min-w-0">' +
                    '<div class="font-medium text-sm text-slate-800 ' + (isUnread ? 'font-semibold' : '') + '">' + escapeHtml(notification.title) + '</div>' +
                    '<div class="text-xs text-slate-500 mt-0.5 truncate">' + escapeHtml(notification.message) + '</div>' +
                    '<div class="text-xs text-slate-400 mt-1">' + timeAgo + '</div>' +
                    '</div>' +
                    (isUnread ? '<span class="w-2 h-2 bg-blue-500 rounded-full"></span>' : '') +
                    '</div>' +
                    '</div>';
            }).join('');

            notificationList.innerHTML = html;

            // 클릭 이벤트 바인딩
            notificationList.querySelectorAll('.notification-item').forEach(function(item) {
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

        // 빈 알림 렌더링
        function renderEmptyNotifications() {
            if (!notificationList) return;
            notificationList.innerHTML = '<div class="text-center text-slate-400 py-4 px-4">알림이 없습니다.</div>';
            updateBadge(0);
        }

        // 배지 업데이트
        function updateBadge(count) {
            if (!notificationBadge) return;
            if (count > 0) {
                notificationBadge.textContent = count > 99 ? '99+' : count;
                notificationBadge.classList.remove('hidden');
            } else {
                notificationBadge.classList.add('hidden');
            }
        }

        // 알림 읽음 처리
        function markAsRead(notificationId) {
            fetch(basePath + '/admin/api/dashboard/notifications/' + encodeURIComponent(notificationId) + '/read', {
                method: 'POST',
                credentials: 'same-origin'
            })
                .then(function(response) { return response.json(); })
                .then(function(data) {
                    if (data.success) {
                        loadNotifications();
                    }
                })
                .catch(function(error) {
                    console.error('[Topbar] Failed to mark as read:', error);
                });
        }

        // 모든 알림 읽음 처리 (전역 함수로 노출)
        window.TopbarNotifications = {
            markAllAsRead: function() {
                fetch(basePath + '/admin/api/dashboard/notifications/read-all', {
                    method: 'POST',
                    credentials: 'same-origin'
                })
                    .then(function(response) { return response.json(); })
                    .then(function(data) {
                        if (data.success) {
                            loadNotifications();
                            // 대시보드의 WebSocket도 갱신 요청
                            if (typeof AdminWebSocket !== 'undefined' && AdminWebSocket.requestNotifications) {
                                AdminWebSocket.requestNotifications();
                                AdminWebSocket.requestStats();
                            }
                        }
                    })
                    .catch(function(error) {
                        console.error('[Topbar] Failed to mark all as read:', error);
                    });
            },
            reload: loadNotifications
        };

        // 유틸리티 함수들
        function escapeHtml(text) {
            if (!text) return '';
            var div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        function formatTimeAgo(dateInput) {
            if (!dateInput) return '';
            var date;
            if (typeof dateInput === 'string') {
                date = new Date(dateInput.replace('T', ' '));
            } else if (Array.isArray(dateInput)) {
                date = new Date(dateInput[0], (dateInput[1] || 1) - 1, dateInput[2] || 1, dateInput[3] || 0, dateInput[4] || 0, dateInput[5] || 0);
            } else {
                date = new Date(dateInput);
            }
            if (isNaN(date.getTime())) return '';

            var now = new Date();
            var diff = Math.floor((now - date) / 1000);
            if (diff < 60) return '방금 전';
            if (diff < 3600) return Math.floor(diff / 60) + '분 전';
            if (diff < 86400) return Math.floor(diff / 3600) + '시간 전';
            if (diff < 604800) return Math.floor(diff / 86400) + '일 전';
            return date.toLocaleDateString('ko-KR');
        }

        function getNotificationIcon(type) {
            switch (type) {
                case 'NEW_RESERVATION': return { name: 'ticket', bgColor: 'bg-blue-100', textColor: 'text-blue-600' };
                case 'REFUND_REQUEST': return { name: 'rotate-ccw', bgColor: 'bg-orange-100', textColor: 'text-orange-600' };
                case 'PAYMENT_FAILED': return { name: 'alert-circle', bgColor: 'bg-rose-100', textColor: 'text-rose-600' };
                case 'SYSTEM_ALERT': return { name: 'alert-triangle', bgColor: 'bg-yellow-100', textColor: 'text-yellow-600' };
                default: return { name: 'bell', bgColor: 'bg-slate-100', textColor: 'text-slate-600' };
            }
        }
    })();
</script>
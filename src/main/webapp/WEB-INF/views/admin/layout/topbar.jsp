<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<header class="glass-topbar h-16 flex items-center justify-between px-4 lg:px-8 fixed top-0 left-0 lg:left-[72px] right-0 z-40 transition-all duration-300">
    <div class="flex items-center gap-4 lg:gap-6">
        <!-- 모바일 햄버거 메뉴 -->
        <button id="sidebar-toggle" class="lg:hidden p-2 hover:bg-white/10 rounded-lg transition-colors" onclick="toggleSidebar()">
            <i data-lucide="menu" class="w-5 h-5 text-glass-secondary"></i>
        </button>

        <!-- 검색 (모바일에서 숨김) -->
        <div class="glass-search hidden sm:flex items-center w-80 relative px-3 py-2">
            <i data-lucide="search" class="text-glass-muted w-4 h-4 mr-2"></i>
            <input type="text" id="global-search-input" placeholder="검색어를 입력하세요..."
                   class="w-full bg-transparent border-none text-sm text-glass-primary placeholder:text-glass-muted focus:outline-none">
        </div>

        <!-- 연결 상태 (대시보드에서만 표시) -->
        <div id="connection-status" class="hidden items-center gap-1.5">
            <span class="w-2 h-2 bg-slate-400 rounded-full"></span>
            <span class="text-glass-muted text-xs">연결 중...</span>
        </div>
    </div>

    <div class="flex items-center gap-4">
        <!-- 알림 버튼 및 드롭다운 -->
        <div class="relative" id="notification-container">
            <button id="notification-button" class="relative p-2 text-glass-secondary hover:bg-white/10 rounded-full transition-colors">
                <i data-lucide="bell" class="w-5 h-5"></i>
                <span id="notification-badge" class="notification-badge hidden">0</span>
            </button>

            <!-- 알림 드롭다운 (글래스) -->
            <div id="notification-dropdown" class="hidden absolute right-0 top-12 w-[420px] glass-modal rounded-xl z-50">
                <div class="flex items-center justify-between p-4 border-b border-white/10">
                    <span class="font-semibold text-glass-primary text-base">알림</span>
                    <button onclick="TopbarNotifications.markAllAsRead()" class="text-xs text-blue-400 hover:text-blue-300 transition-colors">모두 읽음</button>
                </div>
                <div id="notification-list" class="max-h-[480px] overflow-y-auto">
                    <div class="text-center text-glass-muted py-8">
                        알림을 불러오는 중...
                    </div>
                </div>
            </div>
        </div>

        <!-- 관리자 정보 -->
        <div class="flex items-center gap-3 ml-2 pl-4 border-l border-white/10">
            <div class="text-right hidden sm:block">
                <p class="text-sm font-semibold text-glass-primary">${sessionScope.adminName != null ? sessionScope.adminName : '관리자'}</p>
                <p class="text-[10px] text-glass-muted uppercase font-extrabold tracking-tight">${sessionScope.role != null ? sessionScope.role : 'ADMIN'}</p>
            </div>
            <div class="w-9 h-9 rounded-xl overflow-hidden border-2 border-white/20 shadow-lg">
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

            // 전역 검색 기능
            var globalSearchInput = document.getElementById('global-search-input');
            if (globalSearchInput) {
                globalSearchInput.addEventListener('keydown', function(e) {
                    if (e.key === 'Enter') {
                        var keyword = this.value.trim();
                        if (keyword) {
                            // 결제 내역 페이지로 이동하면서 검색어 전달
                            window.location.href = basePath + '/admin/payments?keyword=' + encodeURIComponent(keyword);
                        }
                    }
                });
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

        // 알림 렌더링 (글래스 스타일)
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
                var icon = getNotificationIconGlass(notification.notificationType);
                var isUnread = notification.isRead === 'N';
                var timeAgo = formatTimeAgo(notification.createdAt);
                var bgClass = isUnread ? 'bg-blue-500/20 border-l-2 border-l-blue-400' : 'bg-white/5';

                return '<div class="notification-item p-4 ' + bgClass + ' hover:bg-white/15 cursor-pointer border-b border-white/10 last:border-b-0 transition-all duration-200" ' +
                    'data-notification-id="' + escapeHtml(notification.notificationId) + '" ' +
                    'data-resource-type="' + escapeHtml(notification.relatedResourceType || '') + '" ' +
                    'data-resource-id="' + escapeHtml(notification.relatedResourceId || '') + '">' +
                    '<div class="flex items-start gap-4">' +
                    '<div class="p-2.5 ' + icon.bgColor + ' ' + icon.textColor + ' rounded-lg shrink-0">' +
                    '<i data-lucide="' + icon.name + '" class="w-5 h-5"></i>' +
                    '</div>' +
                    '<div class="flex-1 min-w-0">' +
                    '<div class="text-sm ' + (isUnread ? 'font-bold text-white' : 'font-medium text-white/90') + '">' + escapeHtml(notification.title) + '</div>' +
                    '<div class="text-[13px] text-white/80 mt-1.5 leading-relaxed line-clamp-2">' + escapeHtml(notification.message) + '</div>' +
                    '<div class="text-xs text-white/60 mt-2 font-medium">' + timeAgo + '</div>' +
                    '</div>' +
                    (isUnread ? '<span class="w-2.5 h-2.5 bg-blue-400 rounded-full shrink-0 mt-1.5 animate-pulse shadow-lg shadow-blue-400/50"></span>' : '') +
                    '</div>' +
                    '</div>';
            }).join('');

            notificationList.innerHTML = html;

            // 클릭 이벤트 바인딩 - 읽음 처리 후 결제 내역으로 이동
            notificationList.querySelectorAll('.notification-item').forEach(function(item) {
                item.addEventListener('click', function() {
                    var id = this.getAttribute('data-notification-id');
                    var resourceType = this.getAttribute('data-resource-type');
                    var resourceId = this.getAttribute('data-resource-id');

                    if (id) {
                        // 읽음 처리 후 페이지 이동
                        markAsReadAndNavigate(id, resourceType, resourceId);
                    }
                });
            });

            // Lucide 아이콘 재초기화
            if (typeof lucide !== 'undefined') {
                lucide.createIcons();
            }
        }

        // 빈 알림 렌더링 (글래스 스타일)
        function renderEmptyNotifications() {
            if (!notificationList) return;
            notificationList.innerHTML = '<div class="flex flex-col items-center justify-center py-12 px-4">' +
                '<i data-lucide="bell-off" class="w-10 h-10 text-white/30 mb-3"></i>' +
                '<p class="text-white/50 text-sm">새로운 알림이 없습니다</p>' +
                '</div>';
            updateBadge(0);
            if (typeof lucide !== 'undefined') {
                lucide.createIcons();
            }
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

        // 알림 읽음 처리 후 결제 내역으로 이동
        function markAsReadAndNavigate(notificationId, resourceType, resourceId) {
            fetch(basePath + '/admin/api/dashboard/notifications/' + encodeURIComponent(notificationId) + '/read', {
                method: 'POST',
                credentials: 'same-origin'
            })
                .then(function(response) { return response.json(); })
                .then(function(data) {
                    // 결제/예약 관련이면 결제 내역 페이지로 이동
                    if (resourceType === 'RESERVATION' || resourceType === 'PAYMENT') {
                        var url = basePath + '/admin/payments';
                        if (resourceId) {
                            url += '?keyword=' + encodeURIComponent(resourceId);
                        }
                        window.location.href = url;
                    } else {
                        // 그 외의 경우 알림만 새로고침
                        loadNotifications();
                    }
                })
                .catch(function(error) {
                    console.error('[Topbar] Failed to mark as read:', error);
                    // 에러가 나도 페이지 이동 시도
                    window.location.href = basePath + '/admin/payments';
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

        // 글래스 테마용 알림 아이콘
        function getNotificationIconGlass(type) {
            switch (type) {
                case 'NEW_RESERVATION': return { name: 'ticket', bgColor: 'bg-blue-500/20', textColor: 'text-blue-400' };
                case 'REFUND_REQUEST': return { name: 'rotate-ccw', bgColor: 'bg-orange-500/20', textColor: 'text-orange-400' };
                case 'PAYMENT_FAILED': return { name: 'alert-circle', bgColor: 'bg-rose-500/20', textColor: 'text-rose-400' };
                case 'SYSTEM_ALERT': return { name: 'alert-triangle', bgColor: 'bg-yellow-500/20', textColor: 'text-yellow-400' };
                default: return { name: 'bell', bgColor: 'bg-white/10', textColor: 'text-glass-secondary' };
            }
        }
    })();
</script>
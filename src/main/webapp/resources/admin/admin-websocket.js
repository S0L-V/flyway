/**
 * 관리자 대시보드 WebSocket 연결 관리
 * SockJS + 폴백 지원
 */
const AdminWebSocket = (function() {
    'use strict';

    // 설정
    const CONFIG = {
        // WebSocket 엔드포인트 (contextPath 포함)
        endpoint: '/admin/ws/dashboard',
        // 재연결 설정
        reconnectInterval: 3000,      // 재연결 간격 (ms)
        maxReconnectAttempts: 10,     // 최대 재연결 시도
        // Heartbeat
        pingInterval: 20000,          // PING 간격 (ms)
        pongTimeout: 10000             // PONG 대기 시간 (ms)
    };

    // 상태
    let socket = null;
    let connected = false;
    let reconnectAttempts = 0;
    let pingTimer = null;
    let pongTimer = null;
    let intentionalDisconnect = false;  // 의도적 연결 해제 플래그
    let reconnectTimer = null;          // 재연결 타이머

    // 콜백 핸들러
    const handlers = {
        onStats: null,
        onActivities: null,
        onNotifications: null,
        onConnect: null,
        onDisconnect: null,
        onError: null
    };

    /**
     * WebSocket 연결
     * @param {string} contextPath - 애플리케이션 컨텍스트 경로
     */
    function connect(contextPath) {
        const wsUrl = (contextPath || '') + CONFIG.endpoint;
        console.log('[WebSocket] Connecting to:', wsUrl);

        // 의도적 연결 해제 플래그 초기화
        intentionalDisconnect = false;

        try {
            // SockJS 사용
            socket = new SockJS(wsUrl);

            socket.onopen = function() {
                console.log('[WebSocket] Connected');
                connected = true;
                reconnectAttempts = 0;
                startHeartbeat();

                if (handlers.onConnect) {
                    handlers.onConnect();
                }
            };

            socket.onmessage = function(event) {
                handleMessage(event.data);
            };

            socket.onclose = function(event) {
                console.log('[WebSocket] Disconnected:', event);
                connected = false;
                stopHeartbeat();

                if (handlers.onDisconnect) {
                    handlers.onDisconnect(event);
                }

                // 의도적 연결 해제가 아닌 경우에만 자동 재연결
                if (!intentionalDisconnect) {
                    scheduleReconnect(contextPath);
                }
            };

            socket.onerror = function(error) {
                console.error('[WebSocket] Error:', error);
                if (handlers.onError) {
                    handlers.onError(error);
                }
            };

        } catch (e) {
            console.error('[WebSocket] Connection failed:', e);
            scheduleReconnect(contextPath);
        }
    }

    /**
     * 메시지 처리
     */
    function handleMessage(data) {
        try {
            const message = JSON.parse(data);
            console.log('[WebSocket] Received:', message.type);

            switch (message.type) {
                case 'STATS':
                    if (handlers.onStats) {
                        handlers.onStats(message.data);
                    }
                    break;

                case 'ACTIVITIES':
                    if (handlers.onActivities) {
                        handlers.onActivities(message.data);
                    }
                    break;

                case 'NOTIFICATIONS':
                    if (handlers.onNotifications) {
                        handlers.onNotifications(message.data);
                    }
                    break;

                case 'PONG':
                    clearTimeout(pongTimer);
                    break;

                case 'ERROR':
                    console.error('[WebSocket] Server error:', message.errorMessage);
                    if (handlers.onError) {
                        handlers.onError(new Error(message.errorMessage));
                    }
                    break;

                default:
                    console.warn('[WebSocket] Unknown message type:', message.type);
            }

        } catch (e) {
            console.error('[WebSocket] Failed to parse message:', e);
        }
    }

    /**
     * 메시지 전송
     */
    function send(type, data) {
        if (!connected || !socket) {
            console.warn('[WebSocket] Not connected');
            return false;
        }

        try {
            const message = JSON.stringify({
                type: type,
                data: data,
                timestamp: new Date().toISOString()
            });
            socket.send(message);
            return true;
        } catch (e) {
            console.error('[WebSocket] Send failed:', e);
            return false;
        }
    }

    /**
     * 통계 요청
     */
    function requestStats() {
        return send('REQUEST_STATS');
    }

    /**
     * 활동 요청
     */
    function requestActivities() {
        return send('REQUEST_ACTIVITIES');
    }

    /**
     * 알림 요청
     */
    function requestNotifications() {
        return send('REQUEST_NOTIFICATIONS');
    }

    /**
     * Heartbeat 시작
     */
    function startHeartbeat() {
        stopHeartbeat();

        pingTimer = setInterval(function() {
            if (connected) {
                send('PING');

                // PONG 대기 타이머
                pongTimer = setTimeout(function() {
                    console.warn('[WebSocket] PONG timeout, reconnecting...');
                    disconnect();
                }, CONFIG.pongTimeout);
            }
        }, CONFIG.pingInterval);
    }

    /**
     * Heartbeat 중지
     */
    function stopHeartbeat() {
        if (pingTimer) {
            clearInterval(pingTimer);
            pingTimer = null;
        }
        if (pongTimer) {
            clearTimeout(pongTimer);
            pongTimer = null;
        }
    }

    /**
     * 재연결 스케줄링
     */
    function scheduleReconnect(contextPath) {
        // 의도적 연결 해제 시 재연결 안함
        if (intentionalDisconnect) {
            return;
        }

        if (reconnectAttempts >= CONFIG.maxReconnectAttempts) {
            console.error('[WebSocket] Max reconnect attempts reached');
            return;
        }

        // 기존 재연결 타이머 정리 (중복 방지)
        if (reconnectTimer) {
            clearTimeout(reconnectTimer);
        }

        reconnectAttempts++;
        console.log('[WebSocket] Reconnecting in', CONFIG.reconnectInterval, 'ms (attempt', reconnectAttempts + ')');

        reconnectTimer = setTimeout(function() {
            reconnectTimer = null;
            connect(contextPath);
        }, CONFIG.reconnectInterval);
    }

    /**
     * 연결 해제
     */
    function disconnect() {
        // 의도적 연결 해제 플래그 설정 (자동 재연결 방지)
        intentionalDisconnect = true;

        // 재연결 타이머 정리
        if (reconnectTimer) {
            clearTimeout(reconnectTimer);
            reconnectTimer = null;
        }

        stopHeartbeat();

        if (socket) {
            socket.close();
            socket = null;
        }

        connected = false;
    }

    /**
     * 이벤트 핸들러 등록
     */
    function on(event, callback) {
        const handlerKey = 'on' + event.charAt(0).toUpperCase() + event.slice(1);
        if (handlers.hasOwnProperty(handlerKey)) {
            handlers[handlerKey] = callback;
        } else {
            console.warn('[WebSocket] Unknown event:', event);
        }
    }

    /**
     * 연결 상태 확인
     */
    function isConnected() {
        return connected;
    }

    // Public API
    return {
        connect: connect,
        disconnect: disconnect,
        send: send,
        requestStats: requestStats,
        requestActivities: requestActivities,
        requestNotifications: requestNotifications,
        on: on,
        isConnected: isConnected
    };

})();


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<main class="admin-bg pl-0 lg:pl-[72px] pt-16 min-h-screen transition-all duration-300 relative">
    <div class="p-8 max-w-[1600px] mx-auto space-y-8 relative z-10">
        <div class="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4">
            <div>
                <h1 class="text-2xl font-bold text-glass-primary">Flyway 관리 현황</h1>
                <p class="text-glass-muted">실시간 항공권 예약 및 시스템 지표입니다.</p>
            </div>
            <div class="flex items-center gap-3">
                <!-- 기간 선택 탭 (iOS Segmented Control) -->
                <div class="ios-segment-group" id="period-segment">
                    <div class="ios-segment-slider" id="segment-slider"></div>
                    <button type="button" id="period-daily" class="ios-segment-btn active" data-period="daily" data-index="0">
                        오늘
                    </button>
                    <button type="button" id="period-weekly" class="ios-segment-btn" data-period="weekly" data-index="1">
                        이번 주
                    </button>
                    <button type="button" id="period-monthly" class="ios-segment-btn" data-period="monthly" data-index="2">
                        이번 달
                    </button>
                </div>
                <button id="refresh-button" class="glass-btn px-4 py-2 text-white text-sm font-bold">
                    <i data-lucide="refresh-cw" class="w-4 h-4 inline-block mr-1"></i> 새로고침
                </button>
            </div>
        </div>

        <!-- 통계 카드 (기간별) - 1행: 방문자, 결제 완료, 취소/환불, 매출 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div id="visitor-card" class="glass-card p-6 cursor-pointer" title="클릭하여 방문자 상세 보기">
                <div class="flex items-center justify-between mb-4">
                    <span id="label-visitors" class="text-sm font-semibold text-glass-muted">일일 방문자</span>
                    <div class="p-2.5 rounded-xl glass-icon-blue text-blue-400"><i data-lucide="users" class="w-5 h-5"></i></div>
                </div>
                <div class="flex items-center gap-2">
                    <div id="stat-visitors" class="stat-number">-</div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-glass-muted"></i>
                </div>
            </div>
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span id="label-payments" class="text-sm font-semibold text-glass-muted">결제 완료</span>
                    <div class="p-2.5 rounded-xl glass-icon-teal text-teal-400"><i data-lucide="check-circle" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-payments" class="stat-number">-</div>
            </div>
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span id="label-cancellations" class="text-sm font-semibold text-glass-muted">취소/환불</span>
                    <div class="p-2.5 rounded-xl glass-icon-rose text-rose-400"><i data-lucide="alert-circle" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-cancellations" class="stat-number">-</div>
            </div>
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span id="label-revenue" class="text-sm font-semibold text-glass-muted">오늘의 매출</span>
                    <div class="p-2.5 rounded-xl glass-icon-emerald text-emerald-400"><i data-lucide="credit-card" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-revenue" class="stat-number">-</div>
            </div>
        </div>

        <!-- 실시간/전체 통계 - 2행: 대기 중 예약, 총 회원 수, 신규 가입, 운항 중 항공편 -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-glass-muted">대기 중 예약</span>
                    <div class="p-2.5 rounded-xl glass-icon-yellow text-yellow-400"><i data-lucide="clock" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-pending-reservations" class="stat-number">-</div>
            </div>
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-glass-muted">총 회원 수</span>
                    <div class="p-2.5 rounded-xl glass-icon-indigo text-indigo-400"><i data-lucide="user-check" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-total-users" class="stat-number">-</div>
            </div>
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-glass-muted">신규 가입</span>
                    <div class="p-2.5 rounded-xl glass-icon-purple text-purple-400"><i data-lucide="user-plus" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-new-users" class="stat-number">-</div>
            </div>
            <div class="glass-card p-6">
                <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-semibold text-glass-muted">운항 예정 항공편</span>
                    <div class="p-2.5 rounded-xl glass-icon-sky text-sky-400"><i data-lucide="plane" class="w-5 h-5"></i></div>
                </div>
                <div id="stat-active-flights" class="stat-number">-</div>
            </div>
        </div>

        <!-- 최근 활동 -->
        <div class="glass-section">
            <div class="flex items-center justify-between p-6 border-b border-white/5">
                <div>
                    <h2 class="text-lg font-bold text-glass-primary">최근 활동</h2>
                    <p class="text-sm text-glass-muted">최근 24시간 예약, 결제, 환불 내역</p>
                </div>
                <span class="flex items-center gap-2 text-xs text-glass-muted">
                    <span class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
                    실시간 업데이트
                </span>
            </div>
            <div id="activity-list">
                <div class="text-center text-glass-muted py-12">
                    <i data-lucide="loader-2" class="w-8 h-8 animate-spin mx-auto mb-2"></i>
                    <p>데이터를 불러오는 중...</p>
                </div>
            </div>
        </div>
    </div>
</main>

<!-- 방문자 상세 모달 (글래스) -->
<div id="visitor-modal" class="fixed inset-0 z-50 hidden">
    <div class="fixed inset-0 bg-black/60 backdrop-blur-md" id="visitor-modal-backdrop"></div>
    <div class="fixed inset-0 flex items-center justify-center p-4">
        <div class="glass-modal w-full max-w-3xl max-h-[80vh] flex flex-col">
            <div class="flex items-center justify-between p-6 border-b border-white/10">
                <div>
                    <h2 class="text-lg font-bold text-glass-primary">오늘 방문자 목록</h2>
                    <p class="text-sm text-glass-muted">유니크 세션 기준 방문자 목록입니다.</p>
                </div>
                <button id="visitor-modal-close" class="p-2 hover:bg-white/10 rounded-lg transition-colors">
                    <i data-lucide="x" class="w-5 h-5 text-glass-muted"></i>
                </button>
            </div>
            <div id="visitor-list" class="flex-1 overflow-y-auto p-6">
                <div class="text-center text-glass-muted py-12">
                    <i data-lucide="loader-2" class="w-8 h-8 animate-spin mx-auto mb-2"></i>
                    <p>방문자 목록을 불러오는 중...</p>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Dashboard 초기화 -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        console.log('[JSP] 페이지 로드 완료 - 대시보드 초기화 시작');

        // 1. WebSocket 및 대시보드 코어 초기화
        if (typeof AdminDashboard !== 'undefined') {
            AdminDashboard.init(window.CONTEXT_PATH || '');
        } else {
            console.error('[JSP] AdminDashboard JS가 로드되지 않았습니다.');
        }

        // 2. 기간 선택 버튼 강제 연결 (JS 내부 바인딩 실패 대비)
        // 'daily', 'weekly', 'monthly' 버튼을 찾아서 클릭 이벤트를 직접 붙여줍니다.
        var periods = ['daily', 'weekly', 'monthly'];

        periods.forEach(function(period) {
            var btn = document.getElementById('period-' + period);

            if (btn) {
                // 기존 이벤트 제거 후 새로 할당 (중복 방지)
                btn.onclick = function(e) {
                    e.preventDefault(); // 링크 이동 막기
                    console.log('[JSP] 기간 버튼 클릭됨:', period);

                    if (typeof AdminDashboard !== 'undefined') {
                        // JS 내부의 switchPeriod 함수를 직접 호출하여 안전하게 기간 변경
                        AdminDashboard.switchPeriod(period);
                    }
                };
            } else {
                console.warn('[JSP] 버튼을 찾을 수 없음: period-' + period);
            }
        });
    });
</script>
</body>
</html>

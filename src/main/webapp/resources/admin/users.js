/**
 * 관리자 회원 관리 페이지 JavaScript
 * (수정됨: XSS 방지를 위해 Inline onclick 제거 및 이벤트 위임 적용)
 */
document.addEventListener('DOMContentLoaded', function() {
    // 회원 관리 페이지인지 확인 (user-list-body 요소가 있는 경우에만 실행)
    var isUsersPage = document.getElementById('user-list-body') !== null;
    if (!isUsersPage) {
        console.log('[Users] Not on users page, skipping initialization');
        return;
    }

    console.log('[Users] Initializing...');

    // Lucide 아이콘 초기화 안전 체크
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    let currentPage = 1;
    const pageSize = 10;
    let currentFilterStatus = 'ACTIVE'; // 기본값: 활성 회원만 표시
    let currentSearchKeyword = '';
    let currentViewMode = 'list'; // 기본값: 리스트 형태

    const elements = {
        stats: {
            total: document.getElementById('stat-total'),
            active: document.getElementById('stat-active'),
            blocked: document.getElementById('stat-blocked'),
            onboarding: document.getElementById('stat-onboarding'),
            withdrawn: document.getElementById('stat-withdrawn')
        },
        filterStatus: document.getElementById('filter-status'),
        searchKeyword: document.getElementById('search-keyword'),
        refreshButton: document.getElementById('refresh-button'),
        userListBody: document.getElementById('user-list-body'),
        userCardGrid: document.getElementById('user-card-grid'),
        userListTable: document.getElementById('user-list-table'),
        paginationControls: document.getElementById('pagination-controls'),
        viewToggle: document.getElementById('view-toggle'),
        modal: document.getElementById('user-detail-modal'),
        modalContent: document.getElementById('modal-content'),
        closeModalBtn: document.getElementById('close-modal-btn')
    };

    // 초기 필터 상태 설정 (활성 회원)
    elements.filterStatus.value = 'ACTIVE';

    // 초기 보기 형태 설정 (리스트)
    elements.userCardGrid.classList.add('hidden');
    elements.userListTable.classList.remove('hidden');
    if (elements.viewToggle) {
        elements.viewToggle.setAttribute('data-active', '1');
        var viewBtns = elements.viewToggle.querySelectorAll('.ios-segment-btn');
        viewBtns.forEach(function(btn) {
            if (btn.getAttribute('data-view') === 'list') {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
    }

    // 초기 데이터 로딩
    fetchUserStats();
    fetchUserList();

    // --- 기본 UI 이벤트 리스너 ---
    elements.refreshButton.addEventListener('click', function() {
        currentPage = 1;
        fetchUserStats();
        fetchUserList();
    });

    elements.filterStatus.addEventListener('change', function() {
        currentPage = 1;
        currentFilterStatus = elements.filterStatus.value;
        fetchUserList();
    });

    // 검색어 입력 (디바운스)
    let searchTimeout;
    elements.searchKeyword.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() {
            currentPage = 1;
            currentFilterStatus = elements.filterStatus.value;
            currentSearchKeyword = elements.searchKeyword.value.trim();
            fetchUserList();
        }, 300);
    });

    // 보기형 토글 이벤트
    if (elements.viewToggle) {
        elements.viewToggle.setAttribute('data-active', '0');
        var viewBtns = elements.viewToggle.querySelectorAll('.ios-segment-btn');
        viewBtns.forEach(function(btn) {
            btn.addEventListener('click', function() {
                var viewMode = this.getAttribute('data-view');
                var index = this.getAttribute('data-index');
                if (currentViewMode === viewMode) return;

                currentViewMode = viewMode;
                elements.viewToggle.setAttribute('data-active', index);

                // 버튼 활성 상태 업데이트
                viewBtns.forEach(function(b) { b.classList.remove('active'); });
                this.classList.add('active');

                // 뷰 전환
                if (viewMode === 'card') {
                    elements.userCardGrid.classList.remove('hidden');
                    elements.userListTable.classList.add('hidden');
                } else {
                    elements.userCardGrid.classList.add('hidden');
                    elements.userListTable.classList.remove('hidden');
                }

                // 데이터 다시 렌더링
                fetchUserList();
            });
        });
    }

    elements.closeModalBtn.addEventListener('click', closeModal);
    elements.modal.addEventListener('click', function(e) {
        if (e.target === elements.modal) {
            closeModal();
        }
    });

    // --- [보안 수정] 이벤트 위임 (Event Delegation) ---
    // 버튼 클릭 핸들러 (공통)
    function handleUserAction(e) {
        const btn = e.target.closest('button');
        if (!btn) return;

        const action = btn.dataset.action;
        const userId = btn.dataset.userId;

        if (!action || !userId) return;

        if (action === 'detail') {
            showUserDetail(userId);
        } else if (action === 'block') {
            changeUserStatus(userId, 'BLOCKED');
        } else if (action === 'unblock') {
            changeUserStatus(userId, 'ACTIVE');
        }
    }

    // 목록 테이블 내 버튼 클릭 처리
    elements.userListBody.addEventListener('click', handleUserAction);

    // 카드 그리드 내 버튼 클릭 처리
    if (elements.userCardGrid) {
        elements.userCardGrid.addEventListener('click', handleUserAction);
    }

    // 모달 내부 버튼 클릭 처리
    elements.modalContent.addEventListener('click', function(e) {
        const btn = e.target.closest('button');
        if (!btn) return;

        const action = btn.dataset.action;
        const userId = btn.dataset.userId;

        if (!action || !userId) return;

        if (action === 'block') {
            changeUserStatus(userId, 'BLOCKED');
            closeModal();
        } else if (action === 'unblock') {
            changeUserStatus(userId, 'ACTIVE');
            closeModal();
        }
    });


    // --- API 호출 함수 ---

    function fetchUserStats() {
        fetch(window.CONTEXT_PATH + '/admin/users/api/stats')
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success && data.data) {
                    elements.stats.total.textContent = formatNumber(data.data.total);
                    elements.stats.active.textContent = formatNumber(data.data.active);
                    elements.stats.blocked.textContent = formatNumber(data.data.blocked);
                    elements.stats.onboarding.textContent = formatNumber(data.data.onboarding);
                    elements.stats.withdrawn.textContent = formatNumber(data.data.withdrawn);
                } else {
                    console.error('Failed to fetch user stats:', data.message);
                }
            })
            .catch(function(error) {
                console.error('Error fetching user stats:', error);
            });
    }

    function fetchUserList() {
        // 로딩 상태 표시
        if (currentViewMode === 'card') {
            elements.userCardGrid.innerHTML = `
                <div class="col-span-full flex flex-col items-center justify-center py-16 text-glass-muted">
                    <i data-lucide="loader-2" class="w-8 h-8 animate-spin mb-3"></i>
                    <p>회원 목록을 불러오는 중...</p>
                </div>`;
        } else {
            elements.userListBody.innerHTML = `
                <tr><td colspan="6" class="text-center py-16 text-glass-muted">
                    <i data-lucide="loader-2" class="w-8 h-8 animate-spin mx-auto mb-3"></i>
                    <p>회원 목록을 불러오는 중...</p>
                </td></tr>`;
        }
        if (typeof lucide !== 'undefined') lucide.createIcons();

        var url = new URL(window.CONTEXT_PATH + '/admin/users/api/list', window.location.origin);
        url.searchParams.append('page', currentPage);
        url.searchParams.append('size', pageSize);
        if (currentFilterStatus) {
            url.searchParams.append('status', currentFilterStatus);
        }
        if (currentSearchKeyword) {
            url.searchParams.append('searchKeyword', currentSearchKeyword);
        }

        fetch(url)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success && data.data) {
                    renderUserList(data.data.list);
                    renderPagination(data.data.totalCount, data.data.currentPage, data.data.pageSize, data.data.totalPages);
                } else {
                    console.error('Failed to fetch user list:', data.message);
                    elements.userListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-glass-muted">회원 목록 조회에 실패했습니다.</td></tr>';
                }
                if (typeof lucide !== 'undefined') lucide.createIcons();
            })
            .catch(function(error) {
                console.error('Error fetching user list:', error);
                elements.userListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-rose-400">회원 목록 조회 중 오류가 발생했습니다.</td></tr>';
            });
    }

    // --- 렌더링 함수 ---

    function renderUserList(users) {
        if (currentViewMode === 'card') {
            renderUserCards(users);
        } else {
            renderUserListRows(users);
        }
    }

    function renderUserCards(users) {
        elements.userCardGrid.innerHTML = '';

        if (!users || users.length === 0) {
            elements.userCardGrid.innerHTML = `
                <div class="col-span-full flex flex-col items-center justify-center py-16 text-glass-muted">
                    <i data-lucide="inbox" class="w-12 h-12 mb-3"></i>
                    <p class="text-lg font-medium">회원이 없습니다</p>
                </div>`;
            if (typeof lucide !== 'undefined') lucide.createIcons();
            return;
        }

        users.forEach(function(user) {
            const card = document.createElement('div');
            const isBlocked = user.status === 'BLOCKED';
            const isWithdrawn = user.status === 'WITHDRAWN';

            let cardExtraClass = '';
            if (isBlocked) cardExtraClass = ' border-red-500/30 bg-red-500/5';
            else if (isWithdrawn) cardExtraClass = ' border-white/5 bg-white/3 opacity-60';

            card.className = 'user-card glass-card p-5 hover:border-white/20 transition-all duration-300' + cardExtraClass;

            const initial = escapeHtml(getInitial(user.displayName || user.email));
            const displayName = escapeHtml(user.displayName || '-');
            const email = escapeHtml(user.email);
            const providerBadge = `<span class="px-2 py-1 text-xs font-medium rounded-full ${getProviderBadgeClassGlass(user.provider)}">${escapeHtml(getProviderDisplayName(user.provider))}</span>`;
            const reservationCount = formatNumber(user.reservationCount) + '건';
            const statusBadge = `<span class="px-2 py-1 text-xs font-medium rounded-full ${getStatusBadgeClassGlass(user.status)}">${escapeHtml(user.statusDisplayName)}</span>`;
            const createdAt = formatDate(user.createdAt);
            const userId = escapeHtml(user.userId);

            // 아바타 스타일
            let avatarClass = 'bg-gradient-to-br from-blue-500 to-blue-600 shadow-blue-500/30';
            if (isBlocked) avatarClass = 'bg-gradient-to-br from-red-500 to-red-600 shadow-red-500/30';
            else if (isWithdrawn) avatarClass = 'bg-gradient-to-br from-gray-500 to-gray-600 shadow-gray-500/30';

            // 이름 색상
            let nameClass = 'text-glass-primary';
            if (isBlocked) nameClass = 'text-red-300';
            else if (isWithdrawn) nameClass = 'text-gray-400 line-through';

            // 상단 바 표시
            let topBar = '';
            if (isBlocked) topBar = '<div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-red-500 to-red-400"></div>';
            else if (isWithdrawn) topBar = '<div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-gray-500 to-gray-400"></div>';

            // 상태 아이콘
            let statusIcon = '';
            if (isBlocked) statusIcon = '<i data-lucide="ban" class="w-3 h-3"></i>';
            else if (isWithdrawn) statusIcon = '<i data-lucide="user-x" class="w-3 h-3"></i>';

            card.innerHTML = `
                ${topBar}
                <div class="flex items-start justify-between mb-4">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 flex-shrink-0 rounded-full ${avatarClass} flex items-center justify-center text-white font-bold text-sm shadow-lg">${initial}</div>
                        <div class="min-w-0">
                            <p class="font-semibold ${nameClass} truncate">${displayName}</p>
                            <p class="text-xs text-glass-muted truncate">${email}</p>
                        </div>
                    </div>
                    <span class="flex-shrink-0 inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold rounded-full ${getStatusBadgeClassGlass(user.status)}">
                        ${statusIcon}
                        ${escapeHtml(user.statusDisplayName)}
                    </span>
                </div>
                <div class="space-y-2 text-sm">
                    <div class="flex items-center gap-2 text-glass-secondary">
                        <i data-lucide="user-check" class="w-4 h-4 text-glass-muted flex-shrink-0"></i>
                        <span>가입 경로:</span>
                        ${providerBadge}
                    </div>
                    <div class="flex items-center gap-2 text-glass-secondary">
                        <i data-lucide="calendar" class="w-4 h-4 text-glass-muted flex-shrink-0"></i>
                        <span>예약 건수:</span>
                        <span class="font-semibold text-glass-primary">${reservationCount}</span>
                    </div>
                    <div class="flex items-center gap-2 text-glass-muted">
                        <i data-lucide="clock" class="w-4 h-4 flex-shrink-0"></i>
                        <span class="text-xs">${createdAt}</span>
                    </div>
                </div>
                ${user.status === 'ACTIVE' || user.status === 'BLOCKED' ? `
                <div class="mt-4 pt-4 border-t border-white/10 flex justify-end">
                    ${user.status === 'ACTIVE' ? `
                    <button data-action="block" data-user-id="${userId}" class="px-4 py-2 text-xs font-medium rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 hover:bg-red-500/20 transition-all flex items-center gap-2">
                        <i data-lucide="ban" class="w-4 h-4 pointer-events-none"></i>
                        <span class="pointer-events-none">차단하기</span>
                    </button>` : `
                    <button data-action="unblock" data-user-id="${userId}" class="px-4 py-2 text-xs font-medium rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 hover:bg-emerald-500/20 transition-all flex items-center gap-2">
                        <i data-lucide="shield-check" class="w-4 h-4 pointer-events-none"></i>
                        <span class="pointer-events-none">차단 해제</span>
                    </button>`}
                </div>` : ''}
            `;

            elements.userCardGrid.appendChild(card);
        });

        if (typeof lucide !== 'undefined') lucide.createIcons();
    }

    function renderUserListRows(users) {
        elements.userListBody.innerHTML = '';

        if (!users || users.length === 0) {
            elements.userListBody.innerHTML = `
                <tr><td colspan="6" class="text-center py-16 text-glass-muted">
                    <i data-lucide="inbox" class="w-12 h-12 mx-auto mb-3"></i>
                    <p class="text-lg font-medium">회원이 없습니다</p>
                </td></tr>`;
            if (typeof lucide !== 'undefined') lucide.createIcons();
            return;
        }

        users.forEach(function(user) {
            const row = document.createElement('tr');
            const isBlocked = user.status === 'BLOCKED';
            const isWithdrawn = user.status === 'WITHDRAWN';

            let rowClass = 'hover:bg-white/5 transition-colors';
            if (isBlocked) rowClass = 'bg-red-500/5 hover:bg-red-500/10 transition-colors border-l-2 border-red-500';
            else if (isWithdrawn) rowClass = 'bg-white/2 hover:bg-white/5 transition-colors opacity-50 border-l-2 border-gray-500';

            row.className = rowClass;

            const initial = escapeHtml(getInitial(user.displayName || user.email));
            const displayName = escapeHtml(user.displayName || '-');
            const email = escapeHtml(user.email);
            const providerBadge = `<span class="px-2 py-1 text-xs font-medium rounded-full ${getProviderBadgeClassGlass(user.provider)}">${escapeHtml(getProviderDisplayName(user.provider))}</span>`;
            const reservationCount = formatNumber(user.reservationCount) + '건';
            const createdAt = formatDate(user.createdAt);
            const userId = escapeHtml(user.userId);

            // 아바타 스타일
            let avatarClass = 'bg-gradient-to-br from-blue-500 to-purple-500';
            if (isBlocked) avatarClass = 'bg-gradient-to-br from-red-500 to-red-600';
            else if (isWithdrawn) avatarClass = 'bg-gradient-to-br from-gray-500 to-gray-600';

            // 이름 색상
            let nameClass = 'font-medium text-glass-primary text-sm';
            if (isBlocked) nameClass = 'font-medium text-red-300 text-sm';
            else if (isWithdrawn) nameClass = 'font-medium text-gray-400 text-sm line-through';

            // 상태 뱃지 (아이콘 포함)
            let statusIcon = '';
            if (isBlocked) statusIcon = '<i data-lucide="ban" class="w-3 h-3 inline-block mr-1"></i>';
            else if (isWithdrawn) statusIcon = '<i data-lucide="user-x" class="w-3 h-3 inline-block mr-1"></i>';

            const statusBadge = `<span class="px-2 py-1 text-xs font-medium rounded-full inline-flex items-center ${getStatusBadgeClassGlass(user.status)}">${statusIcon}${escapeHtml(user.statusDisplayName)}</span>`;

            row.innerHTML = `
                <td class="px-4 py-4">
                    <div class="flex items-center gap-3">
                        <div class="w-8 h-8 ${avatarClass} rounded-full flex items-center justify-center text-white font-bold text-xs">${initial}</div>
                        <div>
                            <p class="${nameClass}">${displayName}</p>
                            <p class="text-xs text-glass-muted">${email}</p>
                        </div>
                    </div>
                </td>
                <td class="px-4 py-4 text-sm">${providerBadge}</td>
                <td class="px-4 py-4 text-sm text-glass-primary font-medium">${reservationCount}</td>
                <td class="px-4 py-4 text-sm">${statusBadge}</td>
                <td class="px-4 py-4 text-sm text-glass-secondary">${createdAt}</td>
                <td class="px-4 py-4">
                    <div class="flex items-center justify-center">
                        ${user.status === 'ACTIVE' ? `
                        <button data-action="block" data-user-id="${userId}" class="px-4 py-2 text-xs font-medium rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 hover:bg-red-500/20 transition-all flex items-center gap-2">
                            <i data-lucide="ban" class="w-4 h-4 pointer-events-none"></i>
                            <span class="pointer-events-none">차단하기</span>
                        </button>` : user.status === 'BLOCKED' ? `
                        <button data-action="unblock" data-user-id="${userId}" class="px-4 py-2 text-xs font-medium rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 hover:bg-emerald-500/20 transition-all flex items-center gap-2">
                            <i data-lucide="shield-check" class="w-4 h-4 pointer-events-none"></i>
                            <span class="pointer-events-none">차단 해제</span>
                        </button>` : `
                        <span class="text-xs text-glass-muted">-</span>`}
                    </div>
                </td>
            `;

            elements.userListBody.appendChild(row);
        });

        if (typeof lucide !== 'undefined') lucide.createIcons();
    }

    function renderPagination(totalCount, curPage, pageSize, totalPages) {
        elements.paginationControls.innerHTML = '';
        if (totalPages <= 1) return;

        var infoDiv = document.createElement('div');
        infoDiv.className = 'text-sm text-glass-muted';
        infoDiv.textContent = '총 ' + formatNumber(totalCount) + '명 중 ' + ((curPage - 1) * pageSize + 1) + '-' + Math.min(curPage * pageSize, totalCount) + '명';
        elements.paginationControls.appendChild(infoDiv);

        var ul = document.createElement('ul');
        ul.className = 'flex items-center space-x-1';

        // 이전 페이지
        var prevLi = document.createElement('li');
        var prevButton = document.createElement('button');
        prevButton.className = 'p-2 rounded-lg text-glass-secondary hover:bg-white/10 disabled:opacity-50 transition-colors';
        prevButton.innerHTML = '<i data-lucide="chevron-left" class="w-4 h-4"></i>';
        prevButton.disabled = curPage === 1;
        prevButton.addEventListener('click', function() {
            if (currentPage > 1) {
                currentPage--;
                fetchUserList();
            }
        });
        prevLi.appendChild(prevButton);
        ul.appendChild(prevLi);

        // 페이지 번호
        var startPage = Math.max(1, curPage - 2);
        var endPage = Math.min(totalPages, startPage + 4);
        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }

        for (var i = startPage; i <= endPage; i++) {
            var li = document.createElement('li');
            var button = document.createElement('button');
            button.className = 'px-3 py-1 rounded-lg text-sm font-medium transition-colors ' + (i === curPage ? 'bg-blue-500/80 text-white shadow-lg shadow-blue-500/30' : 'text-glass-secondary hover:bg-white/10');
            button.textContent = i;
            (function(pageIndex) {
                button.addEventListener('click', function() {
                    currentPage = pageIndex;
                    fetchUserList();
                });
            })(i);
            li.appendChild(button);
            ul.appendChild(li);
        }

        // 다음 페이지
        var nextLi = document.createElement('li');
        var nextButton = document.createElement('button');
        nextButton.className = 'p-2 rounded-lg text-glass-secondary hover:bg-white/10 disabled:opacity-50 transition-colors';
        nextButton.innerHTML = '<i data-lucide="chevron-right" class="w-4 h-4"></i>';
        nextButton.disabled = curPage === totalPages;
        nextButton.addEventListener('click', function() {
            if (currentPage < totalPages) {
                currentPage++;
                fetchUserList();
            }
        });
        nextLi.appendChild(nextButton);
        ul.appendChild(nextLi);

        elements.paginationControls.appendChild(ul);
        if (typeof lucide !== 'undefined') lucide.createIcons();
    }

    // --- 비즈니스 로직 함수 (window 스코프 제거 후 로컬 함수로 변경) ---

    function showUserDetail(userId) {
        fetch(window.CONTEXT_PATH + '/admin/users/api/' + userId)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success && data.data) {
                    renderUserDetailModal(data.data);
                    elements.modal.classList.remove('hidden');
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: '조회 실패',
                        text: '회원 정보를 불러오는데 실패했습니다.',
                        confirmButtonText: '확인'
                    });
                }
            })
            .catch(function(error) {
                console.error('Error fetching user detail:', error);
                Swal.fire({
                    icon: 'error',
                    title: '오류 발생',
                    text: '회원 정보를 불러오는 중 오류가 발생했습니다.',
                    confirmButtonText: '확인'
                });
            });
    }

    function changeUserStatus(userId, newStatus) {
        var statusText = newStatus === 'BLOCKED' ? '차단' : '차단해제';
        var iconType = newStatus === 'BLOCKED' ? 'warning' : 'question';
        var confirmColor = newStatus === 'BLOCKED' ? '#ef4444' : '#22c55e';

        Swal.fire({
            icon: iconType,
            title: '회원 ' + statusText,
            text: '해당 회원을 ' + statusText + '하시겠습니까?',
            showCancelButton: true,
            confirmButtonText: statusText,
            cancelButtonText: '취소',
            confirmButtonColor: confirmColor
        }).then(function(result) {
            if (result.isConfirmed) {
                // [보안 수정] encodeURIComponent 적용
                fetch(window.CONTEXT_PATH + '/admin/users/api/' + encodeURIComponent(userId) + '/status', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ status: newStatus })
                })
                    .then(function(response) { return response.json(); })
                    .then(function(data) {
                        if (data.success) {
                            Swal.fire({
                                icon: 'success',
                                title: statusText + ' 완료',
                                text: '회원 상태가 변경되었습니다.',
                                confirmButtonText: '확인'
                            });
                            fetchUserStats();
                            fetchUserList();
                        } else {
                            Swal.fire({
                                icon: 'error',
                                title: statusText + ' 실패',
                                text: data.message || '알 수 없는 오류가 발생했습니다.',
                                confirmButtonText: '확인'
                            });
                        }
                    })
                    .catch(function(error) {
                        console.error('Error changing user status:', error);
                        Swal.fire({
                            icon: 'error',
                            title: '오류 발생',
                            text: '상태 변경 중 오류가 발생했습니다.',
                            confirmButtonText: '확인'
                        });
                    });
            }
        });
    }

    function renderUserDetailModal(user) {
        // [보안 수정] onclick 제거하고 data- 속성 사용 + 글래스 테마 적용
        elements.modalContent.innerHTML =
            '<div class="space-y-4">' +
            '<div class="flex items-center gap-4 pb-4 border-b border-white/10">' +
            '<div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold text-xl shadow-lg shadow-blue-500/30">' +
            escapeHtml(getInitial(user.displayName || user.email)) +
            '</div>' +
            '<div>' +
            '<div class="text-lg font-bold text-glass-primary">' + escapeHtml(user.displayName || '-') + '</div>' +
            '<div class="text-sm text-glass-muted">' + escapeHtml(user.email) + '</div>' +
            '</div>' +
            '</div>' +
            '<div class="grid grid-cols-2 gap-4">' +
            '<div>' +
            '<p class="text-xs text-glass-muted mb-1">회원 ID</p>' +
            '<p class="text-sm text-glass-primary font-mono">' + escapeHtml(user.userId.substring(0, 8)) + '...</p>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-glass-muted mb-1">가입 경로</p>' +
            '<span class="px-2 py-1 text-xs font-medium rounded-full ' + getProviderBadgeClassGlass(user.provider) + '">' +
            escapeHtml(getProviderDisplayName(user.provider)) +
            '</span>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-glass-muted mb-1">상태</p>' +
            '<span class="px-2 py-1 text-xs font-medium rounded-full ' + getStatusBadgeClassGlass(user.status) + '">' +
            escapeHtml(user.statusDisplayName) +
            '</span>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-glass-muted mb-1">예약 건수</p>' +
            '<p class="text-sm text-glass-primary font-medium">' + formatNumber(user.reservationCount) + '건</p>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-glass-muted mb-1">성별</p>' +
            '<p class="text-sm text-glass-primary">' + escapeHtml(getGenderDisplayName(user.gender)) + '</p>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-glass-muted mb-1">국적</p>' +
            '<p class="text-sm text-glass-primary">' + escapeHtml(user.country || '-') + '</p>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-glass-muted mb-1">가입일</p>' +
            '<p class="text-sm text-glass-primary">' + formatDate(user.createdAt) + '</p>' +
            '</div>' +
            (user.withdrawnAt ?
                '<div>' +
                '<p class="text-xs text-glass-muted mb-1">탈퇴일</p>' +
                '<p class="text-sm text-glass-primary">' + formatDate(user.withdrawnAt) + '</p>' +
                '</div>' : '') +
            '</div>' +
            (user.status === 'ACTIVE' || user.status === 'BLOCKED' ?
                '<div class="pt-4 border-t border-white/10">' +
                (user.status === 'ACTIVE' ?
                    // 차단하기: data-action="block"
                    '<button data-action="block" data-user-id="' + escapeHtml(user.userId) + '" class="w-full px-4 py-2.5 bg-red-500/20 border border-red-500/40 text-red-400 rounded-lg hover:bg-red-500/30 transition-all font-medium flex items-center justify-center gap-2">' +
                    '<i data-lucide="ban" class="w-4 h-4"></i>회원 차단하기' +
                    '</button>' :
                    // 차단해제: data-action="unblock"
                    '<button data-action="unblock" data-user-id="' + escapeHtml(user.userId) + '" class="w-full px-4 py-2.5 bg-emerald-500/20 border border-emerald-500/40 text-emerald-400 rounded-lg hover:bg-emerald-500/30 transition-all font-medium flex items-center justify-center gap-2">' +
                    '<i data-lucide="shield-check" class="w-4 h-4"></i>차단 해제하기' +
                    '</button>') +
                '</div>' : '') +
            '</div>';
        if (typeof lucide !== 'undefined') lucide.createIcons();
    }

    function closeModal() {
        elements.modal.classList.add('hidden');
    }

    // 유틸리티 함수들
    function formatNumber(num) {
        if (num === null || num === undefined) return '0';
        return new Intl.NumberFormat('ko-KR').format(num);
    }

    function formatDate(dateString) {
        if (!dateString) return '-';
        if (Array.isArray(dateString)) {
            var d = new Date(dateString[0], dateString[1] - 1, dateString[2]);
            return d.toLocaleDateString('ko-KR');
        }
        var date = new Date(dateString);
        if (isNaN(date.getTime())) return '-';
        return date.toLocaleDateString('ko-KR');
    }

    function escapeHtml(text) {
        if (text === null || text === undefined) return '';
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function getInitial(name) {
        if (!name) return '?';
        return name.charAt(0).toUpperCase();
    }

    function getStatusBadgeClass(status) {
        switch (status) {
            case 'ACTIVE': return 'bg-green-100 text-green-700';
            case 'BLOCKED': return 'bg-red-100 text-red-700';
            case 'ONBOARDING': return 'bg-yellow-100 text-yellow-700';
            case 'WITHDRAWN': return 'bg-slate-100 text-slate-600';
            default: return 'bg-slate-100 text-slate-600';
        }
    }

    // 글래스 테마용 상태 뱃지
    function getStatusBadgeClassGlass(status) {
        switch (status) {
            case 'ACTIVE': return 'bg-emerald-500/20 text-emerald-400';
            case 'BLOCKED': return 'bg-red-500/20 text-red-400';
            case 'ONBOARDING': return 'bg-yellow-500/20 text-yellow-400';
            case 'WITHDRAWN': return 'bg-white/10 text-glass-secondary';
            default: return 'bg-white/10 text-glass-secondary';
        }
    }

    function getProviderBadgeClass(provider) {
        switch (provider) {
            case 'GOOGLE': return 'bg-red-100 text-red-700';
            case 'KAKAO': return 'bg-yellow-100 text-yellow-700';
            case 'NAVER': return 'bg-green-100 text-green-700';
            case 'EMAIL': return 'bg-blue-100 text-blue-700';
            default: return 'bg-slate-100 text-slate-600';
        }
    }

    // 글래스 테마용 제공자 뱃지
    function getProviderBadgeClassGlass(provider) {
        switch (provider) {
            case 'GOOGLE': return 'bg-red-500/20 text-red-400';
            case 'KAKAO': return 'bg-yellow-500/20 text-yellow-400';
            case 'NAVER': return 'bg-green-500/20 text-green-400';
            case 'EMAIL': return 'bg-blue-500/20 text-blue-400';
            default: return 'bg-white/10 text-glass-secondary';
        }
    }

    function getProviderDisplayName(provider) {
        switch (provider) {
            case 'GOOGLE': return 'Google';
            case 'KAKAO': return 'Kakao';
            case 'NAVER': return 'Naver';
            case 'EMAIL': return 'Email';
            default: return provider || '-';
        }
    }

    function getGenderDisplayName(gender) {
        switch (gender) {
            case 'M': return '남성';
            case 'F': return '여성';
            default: return '-';
        }
    }
});
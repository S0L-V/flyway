/**
 * 관리자 회원 관리 페이지 JavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    lucide.createIcons();

    let currentPage = 1;
    const pageSize = 10;
    let currentFilterStatus = '';
    let currentSearchKeyword = '';

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
        searchButton: document.getElementById('search-button'),
        refreshButton: document.getElementById('refresh-button'),
        userListBody: document.getElementById('user-list-body'),
        paginationControls: document.getElementById('pagination-controls'),
        modal: document.getElementById('user-detail-modal'),
        modalContent: document.getElementById('modal-content'),
        closeModalBtn: document.getElementById('close-modal-btn')
    };

    // 초기 데이터 로딩
    fetchUserStats();
    fetchUserList();

    // 이벤트 리스너
    elements.refreshButton.addEventListener('click', function() {
        currentPage = 1;
        fetchUserStats();
        fetchUserList();
    });

    elements.searchButton.addEventListener('click', function() {
        currentPage = 1;
        currentFilterStatus = elements.filterStatus.value;
        currentSearchKeyword = elements.searchKeyword.value.trim();
        fetchUserList();
    });

    elements.filterStatus.addEventListener('change', function() {
        currentPage = 1;
        currentFilterStatus = elements.filterStatus.value;
        fetchUserList();
    });

    elements.searchKeyword.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            currentPage = 1;
            currentFilterStatus = elements.filterStatus.value;
            currentSearchKeyword = elements.searchKeyword.value.trim();
            fetchUserList();
        }
    });

    elements.closeModalBtn.addEventListener('click', closeModal);
    elements.modal.addEventListener('click', function(e) {
        if (e.target === elements.modal) {
            closeModal();
        }
    });

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
        elements.userListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-slate-500"><i data-lucide="loader-2" class="w-8 h-8 animate-spin mx-auto mb-2"></i><p>회원 목록을 불러오는 중...</p></td></tr>';
        lucide.createIcons();

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
                    elements.userListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-slate-500">회원 목록 조회에 실패했습니다.</td></tr>';
                }
                lucide.createIcons();
            })
            .catch(function(error) {
                console.error('Error fetching user list:', error);
                elements.userListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-red-500">회원 목록 조회 중 오류가 발생했습니다.</td></tr>';
            });
    }

    function renderUserList(users) {
        elements.userListBody.innerHTML = '';
        if (!users || users.length === 0) {
            elements.userListBody.innerHTML = '<tr><td colspan="6" class="text-center py-12 text-slate-500">회원이 없습니다.</td></tr>';
            return;
        }

        users.forEach(function(user) {
            var row = document.createElement('tr');
            row.className = 'hover:bg-slate-50';
            row.innerHTML =
                '<td class="px-4 py-3">' +
                '<div class="flex items-center gap-3">' +
                '<div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold text-sm">' +
                escapeHtml(getInitial(user.displayName || user.email)) +
                '</div>' +
                '<div>' +
                '<div class="text-sm font-medium text-slate-800">' + escapeHtml(user.displayName || '-') + '</div>' +
                '<div class="text-xs text-slate-500">' + escapeHtml(user.email) + '</div>' +
                '</div>' +
                '</div>' +
                '</td>' +
                '<td class="px-4 py-3 text-sm text-slate-600">' +
                '<span class="px-2 py-1 text-xs font-medium rounded-full ' + getProviderBadgeClass(user.provider) + '">' +
                escapeHtml(getProviderDisplayName(user.provider)) +
                '</span>' +
                '</td>' +
                '<td class="px-4 py-3 text-sm text-slate-800 font-medium">' + formatNumber(user.reservationCount) + '건</td>' +
                '<td class="px-4 py-3 text-sm">' +
                '<span class="px-2 py-1 text-xs font-medium rounded-full ' + getStatusBadgeClass(user.status) + '">' +
                escapeHtml(user.statusDisplayName) +
                '</span>' +
                '</td>' +
                '<td class="px-4 py-3 text-sm text-slate-600">' + formatDate(user.createdAt) + '</td>' +
                '<td class="px-4 py-3 text-center">' +
                '<div class="flex items-center justify-center gap-2">' +
                '<button onclick="showUserDetail(\'' + escapeHtml(user.userId) + '\')" class="p-2 hover:bg-slate-100 rounded-lg transition-colors" title="상세보기">' +
                '<i data-lucide="eye" class="w-4 h-4 text-slate-500"></i>' +
                '</button>' +
                (user.status === 'ACTIVE' ?
                    '<button onclick="changeUserStatus(\'' + escapeHtml(user.userId) + '\', \'BLOCKED\')" class="p-2 hover:bg-red-50 rounded-lg transition-colors" title="차단하기">' +
                    '<i data-lucide="ban" class="w-4 h-4 text-red-500"></i>' +
                    '</button>' :
                    user.status === 'BLOCKED' ?
                        '<button onclick="changeUserStatus(\'' + escapeHtml(user.userId) + '\', \'ACTIVE\')" class="p-2 hover:bg-green-50 rounded-lg transition-colors" title="차단해제">' +
                        '<i data-lucide="check-circle" class="w-4 h-4 text-green-500"></i>' +
                        '</button>' : '') +
                '</div>' +
                '</td>';
            elements.userListBody.appendChild(row);
        });
    }

    function renderPagination(totalCount, curPage, pageSize, totalPages) {
        elements.paginationControls.innerHTML = '';
        if (totalPages <= 1) return;

        var infoDiv = document.createElement('div');
        infoDiv.className = 'text-sm text-slate-500';
        infoDiv.textContent = '총 ' + formatNumber(totalCount) + '명 중 ' + ((curPage - 1) * pageSize + 1) + '-' + Math.min(curPage * pageSize, totalCount) + '명';
        elements.paginationControls.appendChild(infoDiv);

        var ul = document.createElement('ul');
        ul.className = 'flex items-center space-x-1';

        // 이전 페이지 버튼
        var prevLi = document.createElement('li');
        var prevButton = document.createElement('button');
        prevButton.className = 'p-2 rounded-lg hover:bg-slate-100 disabled:opacity-50';
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

        // 페이지 번호 (최대 5개 표시)
        var startPage = Math.max(1, curPage - 2);
        var endPage = Math.min(totalPages, startPage + 4);
        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }

        for (var i = startPage; i <= endPage; i++) {
            var li = document.createElement('li');
            var button = document.createElement('button');
            button.className = 'px-3 py-1 rounded-lg text-sm font-medium transition-colors ' + (i === curPage ? 'bg-blue-600 text-white' : 'hover:bg-slate-100');
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

        // 다음 페이지 버튼
        var nextLi = document.createElement('li');
        var nextButton = document.createElement('button');
        nextButton.className = 'p-2 rounded-lg hover:bg-slate-100 disabled:opacity-50';
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
        lucide.createIcons();
    }

    // 전역 함수로 노출
    window.showUserDetail = function(userId) {
        fetch(window.CONTEXT_PATH + '/admin/users/api/' + userId)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success && data.data) {
                    renderUserDetailModal(data.data);
                    elements.modal.classList.remove('hidden');
                } else {
                    alert('회원 정보를 불러오는데 실패했습니다.');
                }
            })
            .catch(function(error) {
                console.error('Error fetching user detail:', error);
                alert('회원 정보를 불러오는 중 오류가 발생했습니다.');
            });
    };

    window.changeUserStatus = function(userId, newStatus) {
        var statusText = newStatus === 'BLOCKED' ? '차단' : '차단해제';
        if (!confirm('해당 회원을 ' + statusText + '하시겠습니까?')) {
            return;
        }

        fetch(window.CONTEXT_PATH + '/admin/users/api/' + userId + '/status', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ status: newStatus })
        })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    alert('회원 상태가 변경되었습니다.');
                    fetchUserStats();
                    fetchUserList();
                } else {
                    alert('상태 변경에 실패했습니다: ' + (data.message || '알 수 없는 오류'));
                }
            })
            .catch(function(error) {
                console.error('Error changing user status:', error);
                alert('상태 변경 중 오류가 발생했습니다.');
            });
    };

    function renderUserDetailModal(user) {
        elements.modalContent.innerHTML =
            '<div class="space-y-4">' +
            '<div class="flex items-center gap-4 pb-4 border-b border-slate-100">' +
            '<div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-bold text-xl">' +
            escapeHtml(getInitial(user.displayName || user.email)) +
            '</div>' +
            '<div>' +
            '<div class="text-lg font-bold text-slate-800">' + escapeHtml(user.displayName || '-') + '</div>' +
            '<div class="text-sm text-slate-500">' + escapeHtml(user.email) + '</div>' +
            '</div>' +
            '</div>' +
            '<div class="grid grid-cols-2 gap-4">' +
            '<div>' +
            '<p class="text-xs text-slate-400 mb-1">회원 ID</p>' +
            '<p class="text-sm text-slate-800 font-mono">' + escapeHtml(user.userId.substring(0, 8)) + '...</p>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-slate-400 mb-1">가입 경로</p>' +
            '<span class="px-2 py-1 text-xs font-medium rounded-full ' + getProviderBadgeClass(user.provider) + '">' +
            escapeHtml(getProviderDisplayName(user.provider)) +
            '</span>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-slate-400 mb-1">상태</p>' +
            '<span class="px-2 py-1 text-xs font-medium rounded-full ' + getStatusBadgeClass(user.status) + '">' +
            escapeHtml(user.statusDisplayName) +
            '</span>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-slate-400 mb-1">예약 건수</p>' +
            '<p class="text-sm text-slate-800 font-medium">' + formatNumber(user.reservationCount) + '건</p>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-slate-400 mb-1">성별</p>' +
            '<p class="text-sm text-slate-800">' + escapeHtml(getGenderDisplayName(user.gender)) + '</p>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-slate-400 mb-1">국적</p>' +
            '<p class="text-sm text-slate-800">' + escapeHtml(user.country || '-') + '</p>' +
            '</div>' +
            '<div>' +
            '<p class="text-xs text-slate-400 mb-1">가입일</p>' +
            '<p class="text-sm text-slate-800">' + formatDate(user.createdAt) + '</p>' +
            '</div>' +
            (user.withdrawnAt ?
                '<div>' +
                '<p class="text-xs text-slate-400 mb-1">탈퇴일</p>' +
                '<p class="text-sm text-slate-800">' + formatDate(user.withdrawnAt) + '</p>' +
                '</div>' : '') +
            '</div>' +
            (user.status === 'ACTIVE' || user.status === 'BLOCKED' ?
                '<div class="pt-4 border-t border-slate-100">' +
                (user.status === 'ACTIVE' ?
                    '<button onclick="changeUserStatus(\'' + escapeHtml(user.userId) + '\', \'BLOCKED\'); closeModal();" class="w-full px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors">' +
                    '<i data-lucide="ban" class="w-4 h-4 inline-block mr-2"></i>차단하기' +
                    '</button>' :
                    '<button onclick="changeUserStatus(\'' + escapeHtml(user.userId) + '\', \'ACTIVE\'); closeModal();" class="w-full px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors">' +
                    '<i data-lucide="check-circle" class="w-4 h-4 inline-block mr-2"></i>차단해제' +
                    '</button>') +
                '</div>' : '') +
            '</div>';
        lucide.createIcons();
    }

    function closeModal() {
        elements.modal.classList.add('hidden');
    }
    window.closeModal = closeModal;

    // 유틸리티 함수들
    function formatNumber(num) {
        if (num === null || num === undefined) return '0';
        return new Intl.NumberFormat('ko-KR').format(num);
    }

    function formatDate(dateString) {
        if (!dateString) return '-';
        // LocalDateTime 배열 형식 처리: [2026, 1, 15, 10, 30, 0]
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

    function getProviderBadgeClass(provider) {
        switch (provider) {
            case 'GOOGLE': return 'bg-red-100 text-red-700';
            case 'KAKAO': return 'bg-yellow-100 text-yellow-700';
            case 'NAVER': return 'bg-green-100 text-green-700';
            case 'EMAIL': return 'bg-blue-100 text-blue-700';
            default: return 'bg-slate-100 text-slate-600';
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

/**
 * Flyway Admin - Global JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Flyway Admin initialized');

    // 1. Lucide 아이콘 초기화
    initLucideIcons();

    // 2. 현재 페이지 메뉴 활성화
    activateCurrentMenu();

    // 3. 로그아웃 확인
    setupLogoutConfirm();

    // 4. 토스트 알림 (있으면)
    showToastNotifications();
});

/**
 * Lucide 아이콘 초기화
 */
function initLucideIcons() {
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
        console.log('Lucide icons initialized');
    } else {
        console.warn('Lucide library not loaded');
    }
}

/**
 * 현재 페이지 메뉴 활성화
 */
function activateCurrentMenu() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        const href = link.getAttribute('href');

        // 정확히 일치하거나 하위 경로인 경우 활성화
        if (currentPath === href ||
            (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

/**
 * 로그아웃 확인 다이얼로그
 */
function setupLogoutConfirm() {
    const logoutForm = document.querySelector('form[action*="logout"]');

    if (logoutForm) {
        logoutForm.addEventListener('submit', function(e) {
            if (!confirm('정말 로그아웃하시겠습니까?')) {
                e.preventDefault();
            }
        });
    }
}

/**
 * 토스트 알림 표시 (옵션)
 */
function showToastNotifications() {
    // URL 파라미터에서 메시지 확인
    const params = new URLSearchParams(window.location.search);
    const message = params.get('message');
    const type = params.get('type') || 'info';

    if (message) {
        showToast(decodeURIComponent(message), type);
    }
}

/**
 * 토스트 알림 생성 (옵션)
 */
function showToast(message, type = 'info') {
    const colors = {
        success: 'bg-green-500',
        error: 'bg-red-500',
        warning: 'bg-yellow-500',
        info: 'bg-blue-500'
    };

    const toast = document.createElement('div');
    toast.className = `fixed top-20 right-8 ${colors[type]} text-white px-6 py-3 rounded-lg shadow-lg z-50 fade-in`;
    toast.textContent = message;

    document.body.appendChild(toast);

    // 3초 후 제거
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

/**
 * 데이터 새로고침 (대시보드용)
 */
function refreshDashboard() {
    console.log('Refreshing dashboard data...');
    location.reload();
}

/**
 * AJAX 헬퍼 함수
 */
async function fetchJSON(url, options = {}) {
    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Fetch error:', error);
        showToast('서버 오류가 발생했습니다.', 'error');
        throw error;
    }
}
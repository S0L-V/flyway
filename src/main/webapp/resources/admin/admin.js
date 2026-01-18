
document.addEventListener('DOMContentLoaded', function() {
    // 1. Lucide 아이콘 초기화
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    // 2. 현재 페이지 메뉴 활성화
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (currentPath.includes(href)) {
            link.classList.add('active');
            // active 스타일은 admin-style.css에 정의됨
        }
    });
});

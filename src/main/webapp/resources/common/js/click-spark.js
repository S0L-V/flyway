/**
 * 클릭 시 불꽃 효과
 * 비행기 커서가 클릭할 때 작은 불꽃이 튀는 애니메이션
 */
(function() {
    'use strict';

    // 모든 클릭 시 불꽃 효과
    document.addEventListener('click', function(e) {
        createSpark(e.clientX, e.clientY);
    });

    function createSpark(x, y) {
        const container = document.createElement('div');
        container.className = 'click-spark';

        // 비행기 뒤쪽(왼쪽 아래)에서 불꽃 시작 - 커서 기준 오프셋
        container.style.left = (x - 10) + 'px';
        container.style.top = (y + 10) + 'px';

        // 8개의 불꽃 파티클 생성
        for (let i = 0; i < 8; i++) {
            const particle = document.createElement('div');
            particle.className = 'spark-particle';

            // 비행기 뒤쪽 방향으로 날아가기 (왼쪽 아래 = 200~250도)
            // 비행기가 45도로 기울어져 있으므로 엔진은 왼쪽 아래
            const angle = (Math.random() * 60 + 200) * (Math.PI / 180); // 200~260도
            const distance = Math.random() * 35 + 20;
            const tx = Math.cos(angle) * distance;
            const ty = Math.sin(angle) * distance;

            particle.style.setProperty('--tx', tx + 'px');
            particle.style.setProperty('--ty', ty + 'px');

            container.appendChild(particle);
        }

        document.body.appendChild(container);

        // 애니메이션 끝나면 제거
        setTimeout(function() {
            container.remove();
        }, 600);
    }
})();

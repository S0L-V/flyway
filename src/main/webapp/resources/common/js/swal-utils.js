/**
 * ========================================
 * Flyway SweetAlert2 유틸리티
 * ========================================
 *
 * SweetAlert2 래퍼 함수들
 * 팀 전체에서 일관된 디자인의 알림창 사용
 *
 * 사용법:
 *
 * 1. 단순 알림 (Alert)
 *    Swal.alert('저장되었습니다.');
 *    Swal.success('저장되었습니다.');
 *    Swal.error('오류가 발생했습니다.');
 *    Swal.warning('주의가 필요합니다.');
 *    Swal.info('참고 사항입니다.');
 *
 * 2. 확인창 (Confirm)
 *    const result = await Swal.confirm('정말 삭제하시겠습니까?');
 *    if (result) { 삭제 로직 }
 *
 *    Swal.confirm('로그아웃 하시겠습니까?', {
 *        title: '로그아웃',
 *        confirmText: '로그아웃',
 *        cancelText: '취소'
 *    }).then(result => { if (result) logout(); });
 *
 * 3. 로딩 표시
 *    Swal.loading('처리 중...');
 *    // 작업 완료 후
 *    Swal.close();
 *
 * 4. 토스트 알림 (우측 상단)
 *    Swal.toast('저장되었습니다.', 'success');
 *    Swal.toast('오류 발생', 'error');
 */

// SweetAlert2가 로드되었는지 확인
if (typeof Swal === 'undefined') {
    console.error('SweetAlert2가 로드되지 않았습니다. head.jsp에 CDN을 추가하세요.');
}

// 기본 테마 설정
const SwalTheme = {
    confirmButtonColor: '#2563eb',  // blue-600
    cancelButtonColor: '#64748b',   // slate-500
    denyButtonColor: '#ef4444',     // red-500
};

/**
 * 단순 알림 - 성공
 */
Swal.success = function(message, title = '성공') {
    return Swal.fire({
        icon: 'success',
        title: title,
        text: message,
        confirmButtonColor: SwalTheme.confirmButtonColor,
        confirmButtonText: '확인'
    });
};

/**
 * 단순 알림 - 오류
 */
Swal.error = function(message, title = '오류') {
    return Swal.fire({
        icon: 'error',
        title: title,
        text: message,
        confirmButtonColor: SwalTheme.confirmButtonColor,
        confirmButtonText: '확인'
    });
};

/**
 * 단순 알림 - 경고
 */
Swal.warning = function(message, title = '경고') {
    return Swal.fire({
        icon: 'warning',
        title: title,
        text: message,
        confirmButtonColor: SwalTheme.confirmButtonColor,
        confirmButtonText: '확인'
    });
};

/**
 * 단순 알림 - 정보
 */
Swal.info = function(message, title = '알림') {
    return Swal.fire({
        icon: 'info',
        title: title,
        text: message,
        confirmButtonColor: SwalTheme.confirmButtonColor,
        confirmButtonText: '확인'
    });
};

/**
 * 단순 알림 - 기본 (아이콘 없음)
 */
Swal.alert = function(message, title = '') {
    return Swal.fire({
        title: title,
        text: message,
        confirmButtonColor: SwalTheme.confirmButtonColor,
        confirmButtonText: '확인'
    });
};

/**
 * 확인창 (Confirm)
 * @param {string} message - 메시지
 * @param {object} options - 옵션 (title, confirmText, cancelText, icon)
 * @returns {Promise<boolean>} - 확인: true, 취소: false
 */
Swal.confirm = function(message, options = {}) {
    const {
        title = '확인',
        confirmText = '확인',
        cancelText = '취소',
        icon = 'question',
        dangerMode = false
    } = options;

    return Swal.fire({
        icon: icon,
        title: title,
        text: message,
        showCancelButton: true,
        confirmButtonColor: dangerMode ? SwalTheme.denyButtonColor : SwalTheme.confirmButtonColor,
        cancelButtonColor: SwalTheme.cancelButtonColor,
        confirmButtonText: confirmText,
        cancelButtonText: cancelText,
        reverseButtons: true
    }).then(result => result.isConfirmed);
};

/**
 * 삭제 확인창 (빨간색 강조)
 */
Swal.confirmDelete = function(message = '정말 삭제하시겠습니까?', options = {}) {
    return Swal.confirm(message, {
        title: options.title || '삭제 확인',
        confirmText: options.confirmText || '삭제',
        cancelText: options.cancelText || '취소',
        icon: 'warning',
        dangerMode: true
    });
};

/**
 * 로딩 표시
 */
Swal.loading = function(message = '처리 중...') {
    return Swal.fire({
        title: message,
        allowOutsideClick: false,
        allowEscapeKey: false,
        showConfirmButton: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });
};

/**
 * 토스트 알림 (우측 상단, 자동 닫힘)
 * @param {string} message - 메시지
 * @param {string} icon - 'success' | 'error' | 'warning' | 'info'
 * @param {number} timer - 표시 시간 (ms, 기본 3000)
 */
Swal.toast = function(message, icon = 'success', timer = 3000) {
    const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: timer,
        timerProgressBar: true,
        didOpen: (toast) => {
            toast.addEventListener('mouseenter', Swal.stopTimer);
            toast.addEventListener('mouseleave', Swal.resumeTimer);
        }
    });

    return Toast.fire({
        icon: icon,
        title: message
    });
};

/**
 * 입력 받기 (Prompt 대체)
 */
Swal.prompt = function(message, options = {}) {
    const {
        title = '입력',
        inputType = 'text',
        placeholder = '',
        confirmText = '확인',
        cancelText = '취소'
    } = options;

    return Swal.fire({
        title: title,
        text: message,
        input: inputType,
        inputPlaceholder: placeholder,
        showCancelButton: true,
        confirmButtonColor: SwalTheme.confirmButtonColor,
        cancelButtonColor: SwalTheme.cancelButtonColor,
        confirmButtonText: confirmText,
        cancelButtonText: cancelText,
        reverseButtons: true,
        inputValidator: (value) => {
            if (!value) {
                return '값을 입력해주세요.';
            }
        }
    }).then(result => {
        if (result.isConfirmed) {
            return result.value;
        }
        return null;
    });
};

console.log('SweetAlert2 유틸리티 로드 완료');

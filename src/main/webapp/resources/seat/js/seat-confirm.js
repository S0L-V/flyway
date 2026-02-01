window.SeatConfirm = (() => {
    function create(app) {
        const { dom, state } = app;

        function isAgreed() {
            return dom.termsCheckbox ? !!dom.termsCheckbox.checked : true;
        }

        function isAllPassengersPicked() {
            if (!state.passengers.length) return false;
            return state.passengers.every(
                (p) => !!state.selectedSeatsByPassenger[p.passengerId]
            );
        }

        function sync() {
        }

        function bindConfirmClick() {
            dom.btnConfirm?.addEventListener("click", (e) => {
                e.preventDefault();

                // 좌석 선택 확인
                if (!isAllPassengersPicked()) {
                    alert("좌석을 선택해주세요.");
                    return;
                }

                // 약관 동의 확인
                if (!isAgreed()) {
                    alert("약관에 동의해주세요.");
                    return;
                }

                // 정상 완료
                window.opener ? window.close() : history.back();
            });
        }

        function bindTermsUI() {
            // 약관 토글 UI만 유지
            if (dom.termsToggle && dom.termsContent) {
                dom.termsToggle.addEventListener("click", (e) => {
                    e.preventDefault();
                    e.stopPropagation();

                    const willExpand = dom.termsContent.hidden;
                    dom.termsContent.hidden = !willExpand;

                    const wrapper = dom.termsToggle.closest(".seat-terms__header-wrapper");
                    wrapper?.classList.toggle(
                        "seat-terms__header--expanded",
                        willExpand
                    );
                    dom.termsToggle.setAttribute(
                        "aria-expanded",
                        String(willExpand)
                    );
                });
            }
        }

        return {
            sync,
            bindTermsUI,
            bindConfirmClick,
        };
    }

    return { create };
})();

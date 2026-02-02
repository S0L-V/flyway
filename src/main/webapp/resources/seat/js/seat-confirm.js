window.SeatConfirm = (() => {
    function create(app) {
        const { dom, state } = app;

        function isAgreed() {
            return dom.termsCheckbox ? !!dom.termsCheckbox.checked : true;
        }

        // 현재 세그먼트의 모든 승객이 좌석 선택했는지 확인
        function isCurrentSegmentComplete() {
            if (!state.passengers.length) return false;
            const segId = state.activeSegmentId;
            const seats = state.selectedSeatsBySegment[segId] || {};
            return state.passengers.every((p) => !!seats[p.passengerId]);
        }

        // 모든 세그먼트의 모든 승객이 좌석 선택했는지 확인
        function isAllSegmentsComplete() {
            if (!state.passengers.length) return false;
            if (!state.segments.length) return false;

            return state.segments.every((seg) => {
                const seats = state.selectedSeatsBySegment[seg.segmentId] || {};
                return state.passengers.every((p) => !!seats[p.passengerId]);
            });
        }

        // 미완료 세그먼트 찾기
        function findIncompleteSegment() {
            for (const seg of state.segments) {
                const seats = state.selectedSeatsBySegment[seg.segmentId] || {};
                const isComplete = state.passengers.every((p) => !!seats[p.passengerId]);
                if (!isComplete) {
                    return seg;
                }
            }
            return null;
        }

        function scrollToTermsAndHighlight() {
            const termsSection = document.querySelector(".seat-notices");
            const termsCheckbox = document.getElementById("terms-checkbox");
            const termsWrapper = document.querySelector(".seat-terms__header-wrapper");

            if (termsSection) {
                setTimeout(() => {
                    termsSection.scrollIntoView({ behavior: "smooth", block: "center" });

                    if (termsWrapper) {
                        termsWrapper.style.transition = "box-shadow 0.3s ease";
                        termsWrapper.style.boxShadow = "0 0 0 3px rgba(239, 68, 68, 0.5)";
                        termsWrapper.style.borderRadius = "8px";

                        setTimeout(() => {
                            termsWrapper.style.boxShadow = "";
                        }, 2000);
                    }

                    if (termsCheckbox) {
                        termsCheckbox.focus();
                    }
                }, 100);
            }
        }

        // 세그먼트 탭으로 전환
        function switchToSegment(segmentId) {
            const tab = document.querySelector(`#segment-tabs .segment-tab[data-segment-id="${segmentId}"]`);
            if (tab) {
                tab.click();
            }
        }

        function sync() {
        }

        function bindConfirmClick() {
            dom.btnConfirm?.addEventListener("click", async (e) => {
                e.preventDefault();

                // 모든 세그먼트 좌석 선택 확인
                if (!isAllSegmentsComplete()) {
                    const incompleteSeg = findIncompleteSegment();

                    if (incompleteSeg) {
                        const segName = `${incompleteSeg.dep} → ${incompleteSeg.arr}`;

                        Swal.warning(
                            `${segName} 구간의 모든 승객 좌석을 선택해주세요.`,
                            "좌석 미선택"
                        ).then(() => {
                            // 미완료 세그먼트로 전환
                            switchToSegment(incompleteSeg.segmentId);
                        });
                    } else {
                        Swal.warning("모든 구간의 좌석을 선택해주세요.", "좌석 미선택");
                    }
                    return;
                }

                // 약관 동의 확인
                if (!isAgreed()) {
                    Swal.warning("좌석 선택 규정에 동의해주세요.", "약관 동의 필요").then(() => {
                        scrollToTermsAndHighlight();
                    });
                    return;
                }

                // 정상 완료 - 부모 창 좌석 정보 갱신 후 닫기
                if (window.opener && typeof window.opener.refreshSeatInfo === 'function') {
                    window.opener.refreshSeatInfo();
                }
                window.opener ? window.close() : history.back();
            });
        }

        function bindTermsUI() {
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

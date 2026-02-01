window.SeatEvents = (() => {
    function bind(app, renderer) {
        const { dom, ctx, state } = app;

        bindActionSeatSelect();
        bindSeatGridClick();
        bindCancel();
        bindMinimapZone();

        // 오른쪽 액션 카드 버튼으로 activePassengerId 변경
        function bindActionSeatSelect() {
            dom.actionEl?.addEventListener("click", async (e) => {
                const btn = e.target.closest("button.js-seat-action");
                if (!btn) return;

                const pid = String(btn.dataset.passengerId || "");
                if (!pid) return;

                state.activePassengerId = pid;
                await renderer.refreshAndRender().catch(() => {});
            });
        }

        // 좌석 클릭 → HOLD/RELEASE
        function bindSeatGridClick() {
            dom.seatGridEl.addEventListener("click", async (e) => {
                const btn = e.target.closest("button.seat-item");
                if (!btn || state.isHolding) return;

                const seatNo = btn.dataset.seatNo;
                if (!seatNo) return;

                const pid = state.activePassengerId;
                const currentSeatNo = state.selectedSeatsByPassenger[pid] || null;

                // 동일 화면 중복 선택 방지
                const occupiedByOther = Object.entries(state.selectedSeatsByPassenger).some(
                    ([otherPid, s]) => otherPid !== String(pid) && s === seatNo
                );
                if (occupiedByOther) {
                    alert("다른 승객이 이미 선택한 좌석입니다.");
                    return;
                }

                // disabled 좌석은 클릭 무시 (현재 선택좌석 해제만 허용)
                if (btn.disabled && !(currentSeatNo && seatNo === currentSeatNo)) return;

                try {
                    state.isHolding = true;

                    if (currentSeatNo === seatNo) {
                        await SeatAPI.releaseHold(
                            ctx.base,
                            ctx.reservationId,
                            ctx.segmentId,
                            pid
                        );

                        delete state.selectedSeatsByPassenger[pid];
                        await renderer.refreshAndRender();
                        return;
                    } else {
                        if (currentSeatNo) {
                            await SeatAPI.releaseHold(
                                ctx.base,
                                ctx.reservationId,
                                ctx.segmentId,
                                pid
                            );
                        }
                        await SeatAPI.holdSeat(
                            ctx.base,
                            ctx.reservationId,
                            ctx.segmentId, {
                            passengerId: pid,
                            seatNo,
                        });

                        state.selectedSeatsByPassenger[pid] = seatNo;
                        await renderer.refreshAndRender();
                    }
                } catch (err) {
                    alert(`좌석 선택에 실패했습니다. 다시 시도해주세요.`);
                    await renderer.refreshAndRender().catch(() => {});
                } finally {
                    state.isHolding = false;
                }
            });
        }

        // 취소: HOLD 풀고 닫기
        function bindCancel() {
            dom.btnCancel?.addEventListener("click", async () => {
                const pids = Object.keys(state.selectedSeatsByPassenger);

                for (const pid of pids) {
                    try {
                        await SeatAPI.releaseHold(ctx.base, ctx.reservationId, ctx.segmentId, pid);
                    } catch (e) {}
                }

                window.opener ? window.close() : history.back();
            });
        }

        // 미니맵 front/mid/rear 전환
        function bindMinimapZone() {
            const minimapEl = dom.minimapEl;
            const zoneButtons = minimapEl ? minimapEl.querySelectorAll(".minimap__zone") : [];

            function setZone(zone) {
                dom.seatGridEl.classList.remove("seat-grid--front", "seat-grid--mid", "seat-grid--rear");

                if (zone === "front") dom.seatGridEl.classList.add("seat-grid--front");
                else if (zone === "mid") dom.seatGridEl.classList.add("seat-grid--mid");
                else if (zone === "rear") dom.seatGridEl.classList.add("seat-grid--rear");

                zoneButtons.forEach((b) => b.classList.toggle("is-active", b.dataset.zone === zone));
                dom.seatGridEl.scrollIntoView({ block: "start", behavior: "smooth" });
            }

            if (zoneButtons.length) setZone("front");

            zoneButtons.forEach((btn) => {
                btn.addEventListener("click", () => {
                    const zone = btn.dataset.zone;
                    if (!zone) return;
                    setZone(zone);
                });
            });
        }
    }

    return { bind };
})();

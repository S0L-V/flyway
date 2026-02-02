window.SeatEvents = (() => {
    function bind(app, renderer) {
        const { dom, ctx, state } = app;

        bindSegmentTabs();
        bindActionSeatSelect();
        bindSeatGridClick();
        bindCancel();
        bindMinimapZone();

        // 세그먼트 탭 전환 (가는편/오는편)
        function bindSegmentTabs() {
            const tabsEl = dom.segmentTabsEl;
            if (!tabsEl) return;

            // 초기 완료 상태 표시
            updateAllSegmentTabsStatus();

            tabsEl.addEventListener("click", async (e) => {
                const tab = e.target.closest(".segment-tab");
                if (!tab) return;

                const newSegmentId = tab.dataset.segmentId;
                if (!newSegmentId || newSegmentId === state.activeSegmentId) return;

                // 탭 UI 전환
                tabsEl.querySelectorAll(".segment-tab").forEach((t) => {
                    t.classList.toggle("segment-tab--active", t.dataset.segmentId === newSegmentId);
                });

                // 상태 업데이트
                state.activeSegmentId = newSegmentId;
                ctx.segmentId = newSegmentId;

                // 첫 번째 승객으로 리셋
                state.activePassengerId =
                    (state.passengers[0] && state.passengers[0].passengerId) || ctx.defaultPassengerId;

                // 좌석 정보 다시 로드
                await renderer.refreshAndRender().catch(console.error);

                // 완료 상태 업데이트
                updateAllSegmentTabsStatus();
            });
        }

        // 모든 세그먼트 탭 완료 상태 업데이트
        function updateAllSegmentTabsStatus() {
            const tabs = document.querySelectorAll("#segment-tabs .segment-tab");
            tabs.forEach((tab) => {
                const segId = tab.dataset.segmentId;
                const seats = state.selectedSeatsBySegment[segId] || {};
                const isComplete = state.passengers.length > 0 &&
                    state.passengers.every((p) => !!seats[p.passengerId]);
                tab.classList.toggle("is-complete", isComplete);
            });
        }

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

        // 좌석 클릭 → HOLD/RELEASE (Optimistic UI Update)
        function bindSeatGridClick() {
            dom.seatGridEl.addEventListener("click", async (e) => {
                const btn = e.target.closest("button.seat-item");
                if (!btn) return;

                const seatNo = btn.dataset.seatNo;
                if (!seatNo) return;

                // 처리중이면 클릭 무시(연타/더블클릭 방지)
                if (state.isHolding) {
                    Swal?.info?.("좌석 처리 중입니다. 잠시만 기다려주세요.", "처리 중");
                    return;
                }

                const pid = state.activePassengerId;
                const segId = state.activeSegmentId;

                const currentSeats = state.selectedSeatsBySegment[segId] || {};
                const currentSeatNo = currentSeats[pid] || null;

                // 동일 화면 중복 선택 방지
                const occupiedByOther = Object.entries(currentSeats).some(
                    ([otherPid, s]) => otherPid !== String(pid) && s === seatNo
                );
                if (occupiedByOther) {
                    Swal.warning("다른 승객이 이미 선택한 좌석입니다.", "좌석 선택 불가");
                    return;
                }

                // disabled 좌석은 클릭 무시 (현재 선택좌석 해제만 허용)
                if (btn.disabled && !(currentSeatNo && seatNo === currentSeatNo)) return;

                //  여기서부터 진짜 처리 시작
                state.isHolding = true;
                try {
                    // 같은 좌석 재클릭 → 해제
                    if (currentSeatNo === seatNo) {
                        // 즉시 UI 업데이트
                        SeatGrid.updateSeatUI(dom.seatGridEl, seatNo, "AVAILABLE");
                        if (state.selectedSeatsBySegment[segId]) {
                            delete state.selectedSeatsBySegment[segId][pid];
                        }
                        updateSummaryUI();

                        // 서버 해제
                        try {
                            await SeatAPI.releaseHold(ctx.base, ctx.reservationId, segId, pid);
                        } catch (err) {
                            // 실패 시 롤백
                            if (!state.selectedSeatsBySegment[segId]) state.selectedSeatsBySegment[segId] = {};
                            state.selectedSeatsBySegment[segId][pid] = seatNo;
                            SeatGrid.updateSeatUI(dom.seatGridEl, seatNo, "HOLD");
                            updateSummaryUI();
                            Swal.error("좌석 해제에 실패했습니다.", "오류");
                        }
                        return;
                    }

                    // 새 좌석 선택
                    // 즉시 UI 업데이트
                    if (currentSeatNo) {
                        SeatGrid.updateSeatUI(dom.seatGridEl, currentSeatNo, "AVAILABLE");
                    }
                    SeatGrid.updateSeatUI(dom.seatGridEl, seatNo, "HOLD");

                    if (!state.selectedSeatsBySegment[segId]) {
                        state.selectedSeatsBySegment[segId] = {};
                    }
                    state.selectedSeatsBySegment[segId][pid] = seatNo;
                    updateSummaryUI();

                    // api 호출: releaseHold를 무조건 시도 → 중복 HOLD 방지
                    try {
                        try {
                            await SeatAPI.releaseHold(ctx.base, ctx.reservationId, segId, pid);
                        } catch (_) {
                            // 기존 HOLD가 없으면 무시
                        }

                        await SeatAPI.holdSeat(ctx.base, ctx.reservationId, segId, {
                            passengerId: pid,
                            seatNo,
                        });
                    } catch (err) {
                        // 실패 시 롤백
                        SeatGrid.updateSeatUI(dom.seatGridEl, seatNo, "AVAILABLE");
                        if (currentSeatNo) {
                            state.selectedSeatsBySegment[segId][pid] = currentSeatNo;
                            SeatGrid.updateSeatUI(dom.seatGridEl, currentSeatNo, "HOLD");
                        } else {
                            delete state.selectedSeatsBySegment[segId][pid];
                        }
                        updateSummaryUI();
                        Swal.error("좌석 선택에 실패했습니다. 다시 시도해주세요.", "오류");
                    }
                } finally {
                    state.isHolding = false;
                }
            });


            // 우측 패널 요약 업데이트 (전체 렌더링 없이)
            function updateSummaryUI() {
                const segInfo = getActiveSegmentInfo();
                const segId = state.activeSegmentId;

                SeatGrid.renderSelectedSummary(dom.summaryEl, {
                    passengers: state.passengers,
                    activePassengerId: state.activePassengerId,
                    selectedSeatsByPassenger: state.selectedSeatsBySegment[segId] || {},
                    segment: segInfo,
                });
                SeatGrid.renderSeatAction(dom.actionEl, {
                    passengers: state.passengers,
                    activePassengerId: state.activePassengerId,
                    selectedSeatsByPassenger: state.selectedSeatsBySegment[segId] || {},
                    segment: segInfo,
                });

                // 세그먼트 탭 완료 상태 업데이트
                updateAllSegmentTabsStatus();
            }

            function getActiveSegmentInfo() {
                const activeSegBtn = document.querySelector("#segment-tabs .segment-tab--active");
                if (!activeSegBtn) return { routeText: "", dateTimeText: "" };
                const dep = activeSegBtn.dataset.dep || "";
                const arr = activeSegBtn.dataset.arr || "";
                const depTimeText = activeSegBtn.dataset.deptime || "";
                return {
                    routeText: dep && arr ? `${dep} → ${arr}` : "",
                    dateTimeText: depTimeText,
                };
            }
        }

        // 취소: 모든 세그먼트의 HOLD 풀고 닫기
        function bindCancel() {
            dom.btnCancel?.addEventListener("click", async () => {
                // 모든 세그먼트의 선택된 좌석 해제
                for (const segId of Object.keys(state.selectedSeatsBySegment)) {
                    const seats = state.selectedSeatsBySegment[segId];
                    for (const pid of Object.keys(seats)) {
                        try {
                            await SeatAPI.releaseHold(ctx.base, ctx.reservationId, segId, pid);
                        } catch (e) {}
                    }
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

                zoneButtons.forEach((b) =>
                    b.classList.toggle("is-active", b.dataset.zone === zone));
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

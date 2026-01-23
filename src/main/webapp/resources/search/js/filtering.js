let AIRLINES = [];

const filterState = {
    airline: new Set(),
    price: null,
    outTime: null,
    inTime: null
}

document.addEventListener("DOMContentLoaded", async () => {
    initFilters();
    initTimeChips();
    await loadAirlines();
    initAirlineFilters();
});

async function loadAirlines() {
    const res = await fetch(`${CONTEXT_PATH}/api/public/airlines`);
    const data = await res.json();

    AIRLINES = data.map(a => ({
        code: a.airlineId,
        name: a.airlineName
    }));
}

function initFilters() {
    // ✅ 버튼 클릭: 열기만 (이미 열려있으면 유지)
    document.querySelectorAll(".filter-button[data-filter]").forEach((btn) => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();

            const key = btn.dataset.filter;
            const panel = document.querySelector(`[data-filter-panel="${key}"]`);
            if (!panel) return;


            if (!panel.hidden) {
                panel.hidden = true;
                btn.setAttribute("aria-expanded", "false");
                return;
            }

            closeAllFilterPanels();
            panel.hidden = false;
            btn.setAttribute("aria-expanded", "true");
        });
    });

    document.querySelectorAll("[data-filter-panel]").forEach(panel => {
        panel.addEventListener("click", (e) => {
            e.stopPropagation();
        });
    });

    // ✅ 진짜 "바깥 클릭"일 때만 닫기
    document.addEventListener("click", (e) => {
        // filter 영역 내부(버튼/패널 포함)이면 닫지 않음
        const inside = e.target.closest(".search-filters");
        if (inside) return;

        closeAllFilterPanels();
    });

    // ESC 닫기
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") closeAllFilterPanels();
    });
}

function closeAllFilterPanels() {
    // 모든 패널 닫기
    document.querySelectorAll("[data-filter-panel]").forEach((p) => {
        p.hidden = true;
    });

    // 버튼 aria 업데이트
    document.querySelectorAll(".filter-button[data-filter]").forEach((b) => {
        b.setAttribute("aria-expanded", "false");
    });
}

function initTimeChips() {
    document.querySelectorAll('[data-filter-panel="out-time"], [data-filter-panel="in-time"]')
        .forEach(panel => {
            panel.addEventListener("click", (e) => {
                const chip = e.target.closest(".time-chip");
                if (!chip) return;

                // "전체" 칩이면 나머지 해제하고 본인만 active
                const isAll = chip.textContent.trim().includes("전체");
                if (isAll) {
                    panel.querySelectorAll(".time-chip").forEach(c => c.classList.remove("active"));
                    chip.classList.add("active");
                } else {
                    const allChip = panel.querySelector(".time-filter-header .time-chip");
                    if (allChip) allChip.classList.remove("active");
                    chip.classList.toggle("active");
                }
            });

            const applyBtn = panel.querySelector('[data-action="apply-time"]');
            if(applyBtn) {
                applyBtn.addEventListener("click", (e) => {
                    e.preventDefault();

                    updateFilterStateAndRender();

                    closeAllFilterPanels();
                });
            }
        });
}

// 가격 포멧팅
function formatPrice(price) {
    if (price === undefined || price === null) return "0";
    if (price >= 10000) {
        // 만 단위로 표시 (소수점 버림)
        return Math.floor(price / 10000) + "만";
    }
    // 만 원 미만은 콤마 찍어서 표시
    return price.toLocaleString();
}

// 가격 슬라이더를 초기화하고 이벤트를 연결하는 함수
function initPriceSlider(minPrice, maxPrice) {
    const minInput = document.getElementById('price-min-input');
    const maxInput = document.getElementById('price-max-input');
    const minDisplay = document.getElementById('price-min-display');
    const maxDisplay = document.getElementById('price-max-display');
    const rangeBar = document.getElementById('slider-range-bar');
    const totalMinLabel = document.getElementById('total-min-price');
    const totalMaxLabel = document.getElementById('total-max-price');

    if (!minInput || !maxInput) return; // 요소가 없으면 안전하게 종료

    // 1. 데이터가 없거나 이상할 경우 기본값 설정 방어 로직
    const totalMin = (minPrice !== undefined && minPrice !== null) ? minPrice : 0;
    const totalMax = (maxPrice !== undefined && maxPrice !== null && maxPrice > totalMin) ? maxPrice : (totalMin + 1000000);

    // 2. 슬라이더 범위 및 초기값 설정 (처음엔 전체 범위로 선택)
    minInput.min = totalMin; minInput.max = totalMax; minInput.value = totalMin;
    maxInput.min = totalMin; maxInput.max = totalMax; maxInput.value = totalMax;

    // 3. 양 끝 레이블 텍스트 설정
    totalMinLabel.textContent = formatPrice(totalMin);
    totalMaxLabel.textContent = formatPrice(totalMax);

    // 4. 초기 UI 그리기 (텍스트, 파란색 바 위치)
    updateSliderUI();

    // --- 이벤트 리스너 연결 ---

    // 'input' 이벤트: 드래그하는 동안 실시간으로 UI만 업데이트 (필터링 X)
    minInput.addEventListener('input', handleMinInput);
    maxInput.addEventListener('input', handleMaxInput);

    // 'change' 이벤트: 드래그를 놓았을 때 필터 적용 (렌더링 O)
    minInput.addEventListener('change', updateFilterStateAndRender);
    maxInput.addEventListener('change', updateFilterStateAndRender);

    // --- 내부 헬퍼 함수들 ---

    // 최소 핸들 움직일 때 방어 로직
    function handleMinInput() {
        const minVal = parseInt(minInput.value);
        const maxVal = parseInt(maxInput.value);
        // 최소 핸들이 최대 핸들보다 커지지 못하게 막음
        if (minVal > maxVal) {
            minInput.value = maxVal;
        }
        updateSliderUI();
    }
    // 최대 핸들 움직일 때 방어 로직
    function handleMaxInput() {
        const minVal = parseInt(minInput.value);
        const maxVal = parseInt(maxInput.value);
        // 최대 핸들이 최소 핸들보다 작아지지 못하게 막음
        if (maxVal < minVal) {
            maxInput.value = minVal;
        }
        updateSliderUI();
    }

    // UI (텍스트와 파란색 트랙 바) 업데이트 로직
    function updateSliderUI() {
        const minVal = parseInt(minInput.value);
        const maxVal = parseInt(maxInput.value);

        // 상단 텍스트 업데이트
        minDisplay.textContent = formatPrice(minVal);
        maxDisplay.textContent = formatPrice(maxVal);

        // 파란색 바의 위치(left)와 너비(width)를 백분율(%)로 계산
        const range = totalMax - totalMin || 1; // 0 나누기 방지
        const leftPercent = ((minVal - totalMin) / range) * 100;
        const widthPercent = ((maxVal - minVal) / range) * 100;

        rangeBar.style.left = leftPercent + "%";
        rangeBar.style.width = widthPercent + "%";
    }
}

// 항공사 필터
// function initAirlineFilters() {
//     // 항공사 체크박스(input[type=checkbox])들에 이벤트 걸기
//     const airlineCheckboxes = document.querySelectorAll('input[name="airline"]');
//
//     airlineCheckboxes.forEach(box => {
//         box.addEventListener('change', () => {
//             // 체크박스 상태가 변하면 즉시 필터 적용
//             updateFilterStateAndRender();
//         });
//     });
// }

function initAirlineFilters() {
    const container = document.getElementById("airlineFilterList"); // HTML에 <ul id="airlineFilterList"></ul> 가 있어야 함
    if (!container) return;

    const escapeHtml = (v) =>
        String(v).replace(/[&<>"']/g, (c) => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
        })[c]);

    container.innerHTML = AIRLINES.map(airline => `
        <li class="airline-item">
            <label class="flex items-center gap-2 cursor-pointer hover:bg-slate-50 p-1 rounded transition-colors">
                <input type="checkbox" 
                       name="airline" 
                       value="${escapeHtml(airline.code)}" 
                       class="airline-checkbox accent-blue-600 w-4 h-4" 
                       checked> 
                <span class="text-sm text-slate-700">${escapeHtml(airline.name)}</span>
            </label>
        </li>
    `).join("");

    filterState.airline.clear();
    AIRLINES.forEach(a => filterState.airline.add(a.code));

    // 3. 이벤트 리스너 (이벤트 위임 사용)
    container.addEventListener("change", (e) => {
        if (e.target.classList.contains("airline-checkbox")) {
            updateFilterStateAndRender();
        }
    });
}

function getSelectedAirlines() {
    return Array.from(
        document.querySelectorAll('input[name="airline"]:checked')
    ).map(el => el.value);
}

// 선택된 항공사 배열로
// function getSelectedAirlines() {
//     return Array.from(
//         document.querySelectorAll('input[name="airline"]:checked')
//     ).map(el => el.value);
// }

// 시간대 필터링 값 가져오기
function getSelectedTimeRanges(type) {
    const panel = document.querySelector(`[data-filter-panel="${type}"]`);
    if (!panel) return [];

    const allChip = panel.querySelector('.time-chip[data-range="ALL"]');
    if (allChip && allChip.classList.contains('active')) {
        return null;
    }

    // .time-chip이면서 active 클래스를 가진 요소를 찾음 (단, ALL은 제외)
    const activeChips = panel.querySelectorAll('.time-chip.active:not([data-range="ALL"])');

    // 요소들을 배열로 변환 후 data-range 값만 뽑아냄
    return Array.from(activeChips).map(chip => chip.dataset.range);
}

// 필터 상태 업데이트 및 리렌더링 트리거
function updateFilterStateAndRender() {
    // 1. DOM에서 현재 선택된 값들을 읽어와 state 업데이트
    filterState.airline = new Set(getSelectedAirlines()); // 배열을 Set으로 변환 (원하시면 배열 그대로 써도 됨)
    filterState.outTime = getSelectedTimeRanges('out-time');
    filterState.inTime = getSelectedTimeRanges('in-time');

    // 가격 정보 저장
    // const minInput = document.getElementById('price-min-input');
    // const maxInput = document.getElementById('price-max-input');
    //
    // if (minInput && maxInput) {
    //     const currentMin = parseInt(minInput.value);
    //     const currentMax = parseInt(maxInput.value);
    //     const totalMin = parseInt(minInput.min);
    //     const totalMax = parseInt(maxInput.max);
    //
    //     // 사용자가 전체 범위를 다 선택했으면 굳이 필터링할 필요 없음
    //     // 최소값이나 최대값을 조금이라도 움직였을 때만 상태에 저장
    //     if (currentMin > totalMin || currentMax < totalMax) {
    //         filterState.price = { min: currentMin, max: currentMax };
    //     } else {
    //         filterState.price = null; // 전체 범위면 필터 해제
    //     }
    // }

    console.log("변경된 필터 상태:", filterState);

    // 2. 실제 필터링 로직 실행
    onFilterChanged();
}


// 검색결과에 필터링 적용
function onFilterChanged(){
    const filtered = applyFilters(allOptions, filterState);
    renderByTripType(filtered);
}

// 필터링 적용 로직
function applyFilters(allFlights, state) {
    if(!allFlights || allFlights.length === 0) return[];

    console.log("비행기 데이터 구조 확인:", allFlights[0]);

    return allFlights.filter(flight => {
        // 1. 항공사 필터
        if (state.airline && state.airline.size > 0) {
            const outflightNum = flight.outbound.flightNumber;
            const outAirlineCode = outflightNum && outflightNum.length >= 2
                ? outflightNum.substring(0, 2)
                : null;
            if (!outAirlineCode || !state.airline.has(outAirlineCode)) {
                return false;
            }

            if (flight.inbound) {
                const inFlightNum = flight.inbound.flightNumber;
                const inAirlineCode = inFlightNum && inFlightNum.length >= 2
                    ? inFlightNum.substring(0, 2)
                    : null;
                if (!inAirlineCode || !state.airline.has(inAirlineCode)) {
                    return false;
                }
            }
        }

        // 2. 가는 날 시간대
        if (state.outTime && state.outTime.length > 0) {
            const timeArray = flight.outbound.departureTime;
            const hour = timeArray[3]; // 시간

            if (!isHourInRange(hour, state.outTime)) {
                return false;
            }
        }

        // 3. 오는 날 시간대
        if (state.inTime && state.inTime.length > 0 && flight.inbound) {
            const timeArray = flight.inbound.departureTime;
            const hour = timeArray[3];

            if (!isHourInRange(hour, state.inTime)) {
                return false;
            }
        }

        // 4. 가격 필터
        // if (state.price) {
        //     const price = flight.totalPrice;
        //
        //     if (price === undefined || price === null || isNAN(price)) {
        //         return false;
        //     }
        //
        //     if (price < state.price.min || price > state.price.max) {
        //         return false;
        //     }
        // }
        // 모두 통과
        return true;
    });
}

// 시간 조건 일치 여부
function isHourInRange(hour, ranges) {
    // hour가 유효한 숫자가 아니면 제외
    if (hour === undefined || hour === null) return false;

    return ranges.some(range => {
        const start = parseInt(range.substring(0, 2), 10);
        const end = parseInt(range.substring(2, 4), 10);

        return hour >= start && hour < end;
    });
}

// 검색 업데이트 시 필터 초기화
function resetFilters() {
    filterState.airline.clear();
    AIRLINES.forEach(a => filterState.airline.add(a.code));
    filterState.outTime = null;
    filterState.inTime = null;
    filterState.price = null;

    // button 초기화
    document.querySelectorAll('input[name="airline"]').forEach(box => {
        box.checked = true;
    });

    document.querySelectorAll('[data-filter-panel]').forEach(panel => {
        panel.querySelectorAll('.time-chip').forEach(chip => {
            chip.classList.remove('active');
        });

        const allChip = panel.querySelector('.time-chip[data-range="ALL"]');
        if (allChip) allChip.classList.add('active');
    });

    closeAllFilterPanels();
}
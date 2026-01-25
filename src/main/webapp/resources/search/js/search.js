let DEP_AIRPORTS = [];
let ARR_AIRPORTS = [];
let allOptions = [];
let displayedOptions = [];
let details = {};

async function loadDepAirports() {
    const res = await fetch(`${CONTEXT_PATH}/api/public/depAirports`);
    const data = await res.json();
    DEP_AIRPORTS = data.map(a => ({
        code: a.airportId,
        name: a.city,
        country: a.country
    }));
}

document.addEventListener("DOMContentLoaded", async () => {
    await loadDepAirports();
    initTripTabs();
    initDropdowns();
    initAirports();
    initDates();
    initPaxCabin();
    initSearchButton();
});

const state = {
    tripType: "RT",
    from: null, // { code, name }
    to: null,   // { code, name }
    dateStart: null, // "YYYY-MM-DD"
    dateEnd: null,
    passengers: 1,
    cabin: "ECO",
};

function initTripTabs() {
    const tabsWrap = document.querySelector(".search-tabs");
    if (!tabsWrap) return;

    const tabs = Array.from(tabsWrap.querySelectorAll(".search-tab"));
    if (tabs.length === 0) return;

    // 초기 UI 동기화
    syncTripUI();

    tabsWrap.addEventListener("click", (e) => {
        const btn = e.target.closest(".search-tab");
        if (!btn) return;

        const trip = btn.dataset.trip; // "RT" | "OW"
        if (trip !== "RT" && trip !== "OW") return;

        // 상태 변경
        if (state.tripType !== trip) {
            state.tripType = trip;

            if (trip === "OW") {

                if (state.dateStart) {
                    setFieldText("dates", `${state.dateStart}`);
                } else {
                    setFieldText("dates", "날짜 선택");
                }

                const endInput = document.getElementById("dateEnd");
                if(endInput) endInput.value = "";

            } else {

                if (state.dateStart && state.dateEnd && state.dateStart > state.dateEnd) {
                    state.dateEnd = null; // 날짜가 꼬였으므로 이때는 초기화
                }

                // 텍스트 복구 로직
                const s = state.dateStart;
                const e = state.dateEnd;

                if (s && e) {
                    setFieldText("dates", `${s} ~ ${e}`);
                    // 인풋에도 다시 값 채워주기
                    const endInput = document.getElementById("dateEnd");
                    if(endInput) endInput.value = e;
                } else if (s) {
                    setFieldText("dates", `${s} ~ 날짜 선택`);
                } else {
                    setFieldText("dates", "날짜 선택");
                }
            }

            document.dispatchEvent(new CustomEvent("tripTypeChanged"));
        }

        // active 클래스 토글
        tabs.forEach(t => t.classList.toggle("search-tab--active", t === btn));

        // UI 반영
        syncTripUI();
        syncDateSummary();
    });
}

function syncTripUI() {
    const rtOnly = document.querySelectorAll("[data-rt-only]");
    rtOnly.forEach(el => {
        el.hidden = (state.tripType !== "RT");
    });

    syncDateSummary();
}

function syncDateSummary() {
    const el = document.getElementById("dateSummary");
    if (!el) return;

    const out = state.dateStart ?? "-";
    if (state.tripType === "RT") {
        const inn = state.dateEnd ?? "-";
        el.textContent = `${out} ~ ${inn}`;
    } else {
        el.textContent = `${out}`;
    }
}

// -------------- Dropdown 공통 --------------
function initDropdowns() {
    document.querySelectorAll(".dropdown").forEach(dd => {
        const toggle = dd.querySelector(".dropdown-toggle");
        const panel  = dd.querySelector(".dropdown-panel");

        toggle.addEventListener("click", (e) => {
            e.preventDefault();

            // 이미 열려있으면 유지(닫지 않음)
            if (!panel.hidden) {
                panel.hidden = true;
                dd.setAttribute("aria-expanded", "false");
                return;
            }

            closeAllDropdowns();
            panel.hidden = false;
            toggle.setAttribute("aria-expanded", "true");

            const search = dd.querySelector(".dropdown-search");
            if (search) search.focus();
        });
    });

    document.addEventListener("click", (e) => {
        const clickedInsideAnyDropdown = !!e.target.closest(".dropdown");
        if (!clickedInsideAnyDropdown) closeAllDropdowns();
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") closeAllDropdowns();
    });
}

function closeAllDropdowns() {
    document.querySelectorAll(".dropdown").forEach(dd => {
        const toggle = dd.querySelector(".dropdown-toggle");
        const panel  = dd.querySelector(".dropdown-panel");
        panel.hidden = true;
        toggle.setAttribute("aria-expanded", "false");
    });
}


function setFieldText(fieldName, mainText, hintText = "") {
    const dd = document.querySelector(`.dropdown[data-field="${fieldName}"]`);
    if (!dd) return;
    dd.querySelector("[data-value]").textContent = mainText;
    const hintEl = dd.querySelector("[data-hint]");
    if (hintEl) hintEl.textContent = hintText;
}

async function loadArrAirports(depCode) {
    if (!depCode) return;

    const res = await fetch(
        `${CONTEXT_PATH}/api/public/arrAirports?depAirport=${depCode}`
    );
    const data = await res.json();

    ARR_AIRPORTS = data.map(a => ({
        code: a.airportId,
        name: a.city,
        country: a.country
    }));
}

function initAirports() {
    setupAirportDropdown("from", () => DEP_AIRPORTS);
    setupAirportDropdown("to",   () => ARR_AIRPORTS);
}

function groupByCountry(items) {
    return items.reduce((acc, cur) => {
        if (!acc[cur.country]) acc[cur.country] = [];
        acc[cur.country].push(cur);
        return acc;
    }, {});
}

function setupAirportDropdown(fieldName, getDataFn) {
    const dd = document.querySelector(`.dropdown[data-field="${fieldName}"]`);
    const ul = dd.querySelector("[data-list]");
    const input = dd.querySelector(".dropdown-search");

    const render = (items) => {
        const grouped = groupByCountry(items);

        ul.innerHTML = Object.entries(grouped).map(([country, airports]) => `
        <li class="country-group">
          <div class="country-title">${country}</div>
          <ul class="country-airports">
            ${airports.map(a => `
              <li class="airport-item"
                  data-code="${a.code}"
                  data-name="${a.name}">
                ${a.name} (${a.code})
              </li>
            `).join("")}
          </ul>
        </li>
      `).join("");
    };

    const getItems = () => getDataFn ? getDataFn() : [];

    render(getItems());

    input.addEventListener("input", () => {
        const q = input.value.trim().toLowerCase();
        const filtered = getItems().filter(a =>
            a.country.toLowerCase().includes(q) || a.code.toLowerCase().includes(q) || a.name.toLowerCase().includes(q)
        );
        render(filtered);
    });

    ul.addEventListener("click", async (e) => {
        const item = e.target.closest(".airport-item");
        if (!item) return;

        const code = item.dataset.code;
        const name = item.dataset.name;

        state[fieldName] = { code, name };
        setFieldText(fieldName, `${name}(${code})`);
        closeAllDropdowns();

        if (fieldName === "from") {
            state.to = null;
            setFieldText("to", "도착 공항 선택");
            document.dispatchEvent(new CustomEvent("departureChanged"));
            await loadArrAirports(code);

            const toInput = document.querySelector(
                '.dropdown[data-field="to"] .dropdown-search'
            );
            toInput.dispatchEvent(new Event("input"));
        }
    });
}

// -------------- 날짜 --------------
function initDates() {
    const start = document.getElementById("dateStart");
    const end   = document.getElementById("dateEnd");
    const err   = document.getElementById("dateError");

    function syncDateUIByTripType() {
        const isRT = state.tripType === "RT";

        // end input(또는 end를 감싸는 row)을 숨기고 싶으면 여기서 처리
        // (end만 숨겨도 되고, end가 들어있는 wrapper div가 있으면 그걸 hidden 처리하는 게 더 예쁨)
        end.hidden = !isRT;

        if (!isRT) {
            end.value = "";
            //state.dateEnd = null;
            err.hidden = true;
        }
    }

    syncDateUIByTripType();

    document.addEventListener("tripTypeChanged", syncDateUIByTripType);

    // 시작일 변경 시: 종료일 min을 시작일로 맞춤 (왕복일 때만 의미 있음)
    start.addEventListener("change", () => {
        if (start.value) end.min = start.value;
    });

    // 적용 버튼
    document.querySelector('[data-action="applyDates"]').addEventListener("click", () => {
        const s = start.value;
        const e = end.value;
        const isRT = state.tripType === "RT";

        // 공통: 출발일은 항상 필수
        if (!s) {
            err.textContent = "출발일을 선택해 주세요.";
            err.hidden = false;
            return;
        }

        // 왕복: 도착일도 필수 + 검증
        if (isRT) {
            if (!e) {
                err.textContent = "도착일을 선택해 주세요.";
                err.hidden = false;
                return;
            }
            if (e < s) {
                err.textContent = "도착일은 출발일보다 같거나 이후여야 해요.";
                err.hidden = false;
                return;
            }

            err.hidden = true;
            state.dateStart = s;
            state.dateEnd = e;

            setFieldText("dates", `${s} ~ ${e}`);
            closeAllDropdowns();
            return;
        }

        // 편도: end는 null로 고정
        err.hidden = true;
        state.dateStart = s;
        state.dateEnd = null;

        setFieldText("dates", `${s}`);
        closeAllDropdowns();
    });
}

// -------------- 인원 + 좌석 --------------
function initPaxCabin() {
    const paxEl = document.getElementById("paxCount");

    // +/- 버튼
    document.querySelector('.dropdown[data-field="paxCabin"]').addEventListener("click", (e) => {
        const action = e.target?.dataset?.action;
        if (!action) return;

        if (action === "dec") state.passengers = Math.max(1, state.passengers - 1);
        if (action === "inc") state.passengers = Math.min(9, state.passengers + 1); // 임시 상한 9
        paxEl.textContent = String(state.passengers);
    });

    // 좌석 라디오
    document.querySelectorAll('input[name="cabin"]').forEach(r => {
        r.addEventListener("change", () => {
            if (r.checked) state.cabin = r.value;
        });
    });

    // 적용 버튼
    document.querySelector('[data-action="applyPaxCabin"]').addEventListener("click", () => {
        const cabinLabel = cabinText(state.cabin);
        setFieldText("paxCabin", `탑승 인원 ${state.passengers} / ${cabinLabel}`);
        closeAllDropdowns();
    });

    // 초기 표시
    setFieldText("paxCabin", `탑승 인원 ${state.passengers} / ${cabinText(state.cabin)}`);
}

function cabinText(code) {
    if (code === "FST") return "퍼스트";
    if (code === "BIZ") return "비즈니스";
    return "이코노미";
}

// -------------- 검색 버튼 --------------
function initSearchButton() {
    document.getElementById("btnSearch").addEventListener("click", async () => {

        // 최소 검증
        if (!state.from || !state.to) {
            alert("출발/도착 공항을 선택해 주세요.");
            return;
        }

        // 출발일은 항상 필수
        if (!state.dateStart) {
            alert("출발일을 선택해 주세요.");
            return;
        }

        // 왕복이면 도착일도 필수
        if (state.tripType === "RT" && !state.dateEnd) {
            alert("도착일을 선택해 주세요.");
            return;
        }

        // 검색 필터 초기화
        if (typeof resetFilters === 'function'){
            resetFilters();
        }

        if (typeof currentSortType !== "undefined") {
            currentSortType = null;
        }

        // 2) DTO 1개로 보낼 payload 만들기 (POST)
        const payload = {
            tripType: state.tripType,
            from: state.from.code,
            to: state.to.code,
            dateStart: state.dateStart,
            dateEnd: state.tripType === "RT" ? state.dateEnd : null,
            passengers: state.passengers,
            cabinClass: state.cabin
        };

        try {
            // 3) 검색 API 호출 (POST)
            const res = await fetch(`${CONTEXT_PATH}/api/public/flights/search`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const json = await res.json();

            if (!res.ok) {
                console.error("search failed", json);
                alert(json?.message ?? "검색 중 오류가 발생했습니다.");
                return;
            }

            const data = json.data ?? json;

            handleSearchResult(data);

            syncTripUI();

            console.log(json);

        } catch (err) {
            console.error(err);
            alert("네트워크 오류가 발생했습니다.");
        }
    });
}

function handleSearchResult(data){
    allOptions = data.options ?? [];
    details = data.details;
    displayedOptions = [...allOptions];

    if(allOptions.length > 0) {
        const prices = allOptions.map(f => f.totalPrice).filter(p => p !== undefined && p !== null && !isNaN(p));

        if(prices.length > 0) {
            const minPrice = Math.min(...prices);
            const rawMax = Math.max(...prices);

            const maxPrice = Math.ceil(rawMax / 10000) * 10000;

            console.log("최소", minPrice, "최대", maxPrice);
            if(typeof initPriceSlider === 'function'){
                initPriceSlider(minPrice, maxPrice);
            }
        } else {
            // 가격 정보가 없는 경우
            if (typeof initPriceSlider === 'function') initPriceSlider(0, 1000000);
        }
    }
    if (typeof updateFilterStateAndRender === 'function') {
        updateFilterStateAndRender();
    } else {
        renderByTripType(allOptions);
    }
}

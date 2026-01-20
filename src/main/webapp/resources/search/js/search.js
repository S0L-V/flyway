let AIRPORTS = [];
let allOptions = [];

async function loadAirports() {
    const res = await fetch(`${CONTEXT_PATH}/api/public/airports`);
    const data = await res.json();
    AIRPORTS = data.map(a => ({
        code: a.airportId,
        name: a.city,
        country: a.country
    }));
}

document.addEventListener("DOMContentLoaded", async () => {
    await loadAirports();
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

            // 편도로 바꾸면 오는날 값/표시 초기화
            if (trip === "OW") {
                state.dateEnd = null;
                // 왕복용 인풋이 있다면 값도 같이 비움
                const inInput = document.querySelector('[data-in-date-input]');
                if (inInput) inInput.value = "";
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
        // hidden 속성 쓰면 간단
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

    // ✅ 진짜 "바깥 클릭"일 때만 닫기
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

function initAirports() {
    setupAirportDropdown("from");
    setupAirportDropdown("to");
}

function groupByCountry(items) {
    return items.reduce((acc, cur) => {
        if (!acc[cur.country]) acc[cur.country] = [];
        acc[cur.country].push(cur);
        return acc;
    }, {});
}

function setupAirportDropdown(fieldName) {
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


    render(AIRPORTS);

    input.addEventListener("input", () => {
        const q = input.value.trim().toLowerCase();
        const filtered = AIRPORTS.filter(a =>
            a.country.toLowerCase().includes(q) || a.code.toLowerCase().includes(q) || a.name.toLowerCase().includes(q)
        );
        render(filtered);
    });

    ul.addEventListener("click", (e) => {
        const item = e.target.closest(".airport-item");
        if (!item) return;

        const code = item.dataset.code;
        const name = item.dataset.name;

        state[fieldName] = { code, name };
        setFieldText(fieldName, `${name}(${code})`);
        closeAllDropdowns();
    });
}

// -------------- 날짜 --------------
function initDates() {
    const start = document.getElementById("dateStart");
    const end   = document.getElementById("dateEnd");
    const err   = document.getElementById("dateError");

    // ✅ 편도일 때 end input을 숨기고 값 초기화하는 헬퍼
    function syncDateUIByTripType() {
        const isRT = state.tripType === "RT";

        // end input(또는 end를 감싸는 row)을 숨기고 싶으면 여기서 처리
        // (end만 숨겨도 되고, end가 들어있는 wrapper div가 있으면 그걸 hidden 처리하는 게 더 예쁨)
        end.hidden = !isRT;

        if (!isRT) {
            end.value = "";
            state.dateEnd = null;
            err.hidden = true;
        }
    }

    // ✅ 초기 1회 반영
    syncDateUIByTripType();

    // ✅ 탭 전환 시에도 반영하려면 (initTripTabs에서 커스텀 이벤트를 쏘는 방식)
    // initTripTabs에서 document.dispatchEvent(new CustomEvent("tripTypeChanged"));
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

        // ✅ 공통: 출발일은 항상 필수
        if (!s) {
            err.textContent = "출발일을 선택해 주세요.";
            err.hidden = false;
            return;
        }

        // ✅ 왕복: 도착일도 필수 + 검증
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

        // ✅ 편도: end는 null로 고정
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

    if(allOptions.length > 0) {
        const prices = allOptions.map(f => f.totalPrice).filter(p => p !== undefined && p !== null && !isNaN(p));

        if(prices.length > 0) {
            const minPrice = Math.min(...prices);
            const maxPrice = Math.max(...prices);

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
    renderByTripType(allOptions);
}

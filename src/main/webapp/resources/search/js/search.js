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

    const hasQuery = loadStateFromQuery();
    if (hasQuery) {
        syncSearchBarFromState();
        executeSearch(); // ← 검색 API 호출만 분리
    }
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

// Flatpickr에서 접근할 수 있도록 전역으로 노출
window.state = state;

function initTripTabs() {
    // 새로운 Hero 스타일 탭
    const tripSelector = document.querySelector(".trip-selector");
    if (tripSelector) {
        const tripBtns = Array.from(tripSelector.querySelectorAll(".trip-btn"));

        tripSelector.addEventListener("click", (e) => {
            const btn = e.target.closest(".trip-btn");
            if (!btn) return;

            const trip = btn.dataset.trip;
            if (trip !== "RT" && trip !== "OW") return;

            handleTripChange(trip);

            // 인디케이터 이동
            tripSelector.setAttribute("data-trip", trip);

            // active 클래스 토글
            tripBtns.forEach(t => t.classList.toggle("active", t === btn));

            syncTripUI();
            syncDateSummary();
        });

        syncTripUI();
        return;
    }

    // 기존 스타일 탭 (search.jsp 등에서 사용)
    const tabsWrap = document.querySelector(".search-tabs");
    if (!tabsWrap) return;

    const tabs = Array.from(tabsWrap.querySelectorAll(".search-tab"));
    if (tabs.length === 0) return;

    syncTripUI();

    tabsWrap.addEventListener("click", (e) => {
        const btn = e.target.closest(".search-tab");
        if (!btn) return;

        const trip = btn.dataset.trip;
        if (trip !== "RT" && trip !== "OW") return;

        handleTripChange(trip);

        tabs.forEach(t => t.classList.toggle("search-tab--active", t === btn));

        syncTripUI();
        syncDateSummary();
    });
}

function handleTripChange(trip) {
    if (state.tripType === trip) return;

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
            state.dateEnd = null;
        }

        const s = state.dateStart;
        const e = state.dateEnd;

        if (s && e) {
            setFieldText("dates", `${s} ~ ${e}`);
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
    // 새로운 .search-field와 기존 .dropdown 모두 지원
    const dropdowns = document.querySelectorAll(".dropdown, .search-field[data-field]");

    dropdowns.forEach(dd => {
        const toggle = dd.querySelector(".dropdown-toggle");
        const panel  = dd.querySelector(".dropdown-panel");

        if (!toggle || !panel) return;

        toggle.addEventListener("click", (e) => {
            e.preventDefault();

            if (!panel.hidden) {
                panel.hidden = true;
                toggle.setAttribute("aria-expanded", "false");
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
        const clickedInsideAnyDropdown = !!e.target.closest(".dropdown, .search-field[data-field]");
        if (!clickedInsideAnyDropdown) closeAllDropdowns();
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") closeAllDropdowns();
    });
}

function closeAllDropdowns() {
    const dropdowns = document.querySelectorAll(".dropdown, .search-field[data-field]");
    dropdowns.forEach(dd => {
        const toggle = dd.querySelector(".dropdown-toggle");
        const panel  = dd.querySelector(".dropdown-panel");
        if (panel) panel.hidden = true;
        if (toggle) toggle.setAttribute("aria-expanded", "false");
    });
}


function setFieldText(fieldName, mainText, hintText = "") {
    // 새로운 .search-field와 기존 .dropdown 모두 지원
    const dd = document.querySelector(`.search-field[data-field="${fieldName}"], .dropdown[data-field="${fieldName}"]`);
    if (!dd) return;
    const valueEl = dd.querySelector("[data-value]");
    if (valueEl) valueEl.textContent = mainText;
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
    let activeIndex = -1;
    let selectedCountry = null;

    const dd = document.querySelector(`.search-field[data-field="${fieldName}"], .dropdown[data-field="${fieldName}"]`);
    if (!dd) return;
    const ul = dd.querySelector("[data-list]");
    const input = dd.querySelector(".dropdown-search");
    if (!ul || !input) return;

    // 나라 목록 렌더링
    const renderCountries = (items) => {
        const grouped = groupByCountry(items);
        const countries = Object.keys(grouped).sort((a, b) => a.localeCompare(b, 'ko'));

        ul.innerHTML = `
            <div class="dropdown-header">
                <span class="dropdown-header-title">국가 선택</span>
            </div>
        ` + countries.map(country => `
            <li class="country-item" data-country="${country}">
                <span class="country-name">${country}</span>
                <span class="country-count">${grouped[country].length}개 공항</span>
                <i class="fa-solid fa-chevron-right country-arrow"></i>
            </li>
        `).join("");
    };

    // 공항 목록 렌더링 (특정 나라)
    const renderAirports = (country, items) => {
        const grouped = groupByCountry(items);
        const airports = (grouped[country] || []).sort((a, b) => a.name.localeCompare(b.name, 'ko'));

        ul.innerHTML = `
            <div class="dropdown-header">
                <button type="button" class="back-btn" data-action="back">
                    <i class="fa-solid fa-chevron-left"></i>
                </button>
                <span class="dropdown-header-title">${country}</span>
            </div>
        ` + airports.map(a => `
            <li class="airport-item" data-code="${a.code}" data-name="${a.name}">
                <div class="airport-info">
                    <span class="airport-name">${a.name}</span>
                </div>
                <span class="airport-code">${a.code}</span>
            </li>
        `).join("");
    };

    // 검색 결과 렌더링 (기존 방식)
    const renderSearchResults = (items) => {
        if (items.length === 0) {
            ul.innerHTML = `<li class="no-results">검색 결과가 없습니다</li>`;
            return;
        }

        const grouped = groupByCountry(items);
        ul.innerHTML = Object.entries(grouped).map(([country, airports]) => `
            <li class="country-group">
                <div class="country-title">${country}</div>
                <ul class="country-airports">
                    ${airports.map(a => `
                        <li class="airport-item" data-code="${a.code}" data-name="${a.name}">
                            <span class="airport-name">${a.name}</span>
                            <span class="airport-code">${a.code}</span>
                        </li>
                    `).join("")}
                </ul>
            </li>
        `).join("");
    };

    const getItems = () => getDataFn ? getDataFn() : [];

    // 초기 렌더링 (나라 목록)
    const initRender = () => {
        selectedCountry = null;
        renderCountries(getItems());
    };

    initRender();

    // 검색 입력
    input.addEventListener("input", () => {
        const q = input.value.trim().toLowerCase();

        if (q === "") {
            // 검색어 없으면 나라 목록으로 복귀
            selectedCountry = null;
            renderCountries(getItems());
        } else {
            // 검색어 있으면 전체 검색
            const filtered = getItems().filter(a =>
                a.country.toLowerCase().includes(q) ||
                a.code.toLowerCase().includes(q) ||
                a.name.toLowerCase().includes(q)
            );
            renderSearchResults(filtered);
        }
        activeIndex = -1;
    });

    input.addEventListener("keydown", (e) => {
        const items = ul.querySelectorAll(".airport-item, .country-item");
        if (!items.length) return;

        switch (e.key) {
            case "ArrowDown":
                e.preventDefault();
                activeIndex = (activeIndex + 1) % items.length;
                updateActive(items);
                break;

            case "ArrowUp":
                e.preventDefault();
                activeIndex = (activeIndex - 1 + items.length) % items.length;
                updateActive(items);
                break;

            case "Enter":
                if (activeIndex >= 0) {
                    e.preventDefault();
                    items[activeIndex].click();
                }
                break;

            case "Escape":
                closeAllDropdowns();
                input.blur();
                break;
        }
    });

    function updateActive(items) {
        items.forEach((el, idx) => {
            el.classList.toggle("active", idx === activeIndex);
        });

        const activeItem = items[activeIndex];
        if (activeItem) {
            activeItem.scrollIntoView({ block: "nearest" });
        }
    }

    ul.addEventListener("click", async (e) => {
        e.stopPropagation();

        // 뒤로가기 버튼
        const backBtn = e.target.closest("[data-action='back']");
        if (backBtn) {
            e.preventDefault();
            selectedCountry = null;
            renderCountries(getItems());
            return;
        }

        // 나라 선택
        const countryItem = e.target.closest(".country-item");
        if (countryItem) {
            e.preventDefault();
            const country = countryItem.getAttribute("data-country");
            if (country) {
                selectedCountry = country;
                renderAirports(selectedCountry, getItems());
            }
            return;
        }

        // 공항 선택
        const item = e.target.closest(".airport-item");
        if (!item) return;

        const code = item.dataset.code;
        const name = item.dataset.name;

        state[fieldName] = { code, name };
        setFieldText(fieldName, `${name}(${code})`);
        closeAllDropdowns();

        // 다음에 열 때 초기 상태로
        selectedCountry = null;

        if (fieldName === "from") {
            state.to = null;
            setFieldText("to", "도착지 선택");
            document.dispatchEvent(new CustomEvent("departureChanged"));
            await loadArrAirports(code);

            // 도착 공항 드롭다운 자동 열기
            const toField = document.querySelector('.search-field[data-field="to"], .dropdown[data-field="to"]');
            if (toField) {
                const toToggle = toField.querySelector(".dropdown-toggle");
                const toPanel = toField.querySelector(".dropdown-panel");
                const toInput = toField.querySelector(".dropdown-search");

                if (toPanel && toToggle) {
                    // 도착지 드롭다운도 초기화
                    setTimeout(() => {
                        toPanel.hidden = false;
                        toToggle.setAttribute("aria-expanded", "true");
                        if (toInput) {
                            toInput.value = "";
                            toInput.dispatchEvent(new Event("input"));
                            toInput.focus();
                        }
                    }, 100);
                }
            }
        }
    });

    // 드롭다운 열릴 때마다 초기 상태로
    const toggle = dd.querySelector(".dropdown-toggle");
    if (toggle) {
        toggle.addEventListener("click", () => {
            if (input.value === "") {
                initRender();
            }
        });
    }
}

// -------------- 날짜 --------------
function initDates() {
    // 날짜는 Flatpickr 레인지 피커가 처리함 (home.jsp)
    // search.js에서는 state 동기화만 담당

    const start = document.getElementById("dateStart");
    const end = document.getElementById("dateEnd");

    if (!start) return;

    // hidden input의 값이 변경될 때 state 동기화
    const observer = new MutationObserver(() => {
        if (start.value) state.dateStart = start.value;
        if (end && end.value) state.dateEnd = end.value;
    });

    observer.observe(start, { attributes: true, attributeFilter: ['value'] });
    if (end) {
        observer.observe(end, { attributes: true, attributeFilter: ['value'] });
    }

    // input change 이벤트로도 처리
    start.addEventListener('change', () => {
        if (start.value) state.dateStart = start.value;
    });

    if (end) {
        end.addEventListener('change', () => {
            if (end.value) state.dateEnd = end.value;
        });
    }
}

// -------------- 인원 + 좌석 --------------
function initPaxCabin() {
    const paxEl = document.getElementById("paxCount");
    const paxField = document.querySelector('.search-field[data-field="paxCabin"], .dropdown[data-field="paxCabin"]');
    if (!paxField) return;

    // +/- 버튼
    paxField.addEventListener("click", (e) => {
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
        const params = new URLSearchParams({
            tripType: state.tripType,
            from: state.from.code,
            to: state.to.code,
            dateStart: state.dateStart,
            dateEnd: state.tripType === "RT" ? state.dateEnd : null,
            passengers: state.passengers,
            cabinClass: state.cabin
        });

        console.log("메인값 전달", params);

        window.location.href = `${CONTEXT_PATH}/search?${params.toString()}`;
    });
}

function syncSearchBarFromState() {
    setFieldText("from", `${state.from.name}(${state.from.code})`);
    setFieldText("to", `${state.to.name}(${state.to.code})`);

    // 날짜 표시 업데이트 (레인지 피커용)
    const dateRangeText = document.getElementById("dateRangeText");
    if (dateRangeText) {
        if (state.tripType === "RT" && state.dateEnd) {
            dateRangeText.textContent = `${state.dateStart}  →  ${state.dateEnd}`;
        } else if (state.dateStart) {
            dateRangeText.textContent = state.dateStart;
        }
    } else {
        // 기존 방식 (search.jsp 등)
        if (state.tripType === "RT" && state.dateEnd) {
            setFieldText("dates", `${state.dateStart} ~ ${state.dateEnd}`);
        } else {
            setFieldText("dates", state.dateStart);
            state.dateEnd = null;
        }
    }

    setFieldText(
        "paxCabin",
        `탑승 인원 ${state.passengers} / ${cabinText(state.cabin)}`
    );

    // 새로운 trip-btn 스타일
    document.querySelectorAll(".trip-btn").forEach(t => t.classList.remove("active"));
    document.querySelector(`.trip-btn[data-trip="${state.tripType}"]`)?.classList.add("active");
    const tripSelector = document.querySelector(".trip-selector");
    if (tripSelector) tripSelector.setAttribute("data-trip", state.tripType);

    // 기존 search-tab 스타일
    document.querySelectorAll(".search-tab").forEach(t => t.classList.remove("search-tab--active"));
    document.querySelector(`.search-tab[data-trip="${state.tripType}"]`)?.classList.add("search-tab--active");
}

function loadStateFromQuery() {
    const params = new URLSearchParams(window.location.search);

    if (!params.has("from")) return false;

    state.tripType = params.get("tripType") || "RT";

    const fromCode = params.get("from");
    const toCode   = params.get("to");

    if (fromCode) {
        const dep = DEP_AIRPORTS.find(a => a.code === fromCode);
        state.from = dep
            ? { code: dep.code, name: dep.name }
            : { code: fromCode, name: fromCode };
    }

    if (toCode) {
        const arr = DEP_AIRPORTS.find(a => a.code === toCode);
        state.to = arr
            ? { code: arr.code, name: arr.name }
            : { code: toCode, name: toCode };
    }

    state.dateStart = params.get("dateStart");
    state.dateEnd   = params.get("dateEnd") || null;
    state.passengers = Number(params.get("passengers") || 1);
    state.cabin = params.get("cabinClass") || "ECO";

    return true;
}

async function executeSearch() {
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
}

function handleSearchResult(data){
    allOptions = data.options ?? [];
    details = data.details;
    displayedOptions = [...allOptions];

    if(allOptions.length > 0) {
        const prices = allOptions.map(f => f.totalPrice).filter(p => p !== undefined && p !== null && !isNaN(p));

        if(prices.length > 0) {
            const rawMin = Math.min(...prices);
            const rawMax = Math.max(...prices);

            const minPrice = Math.floor(rawMin / 10000) * 10000;
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
        renderPage(1);
    }
}

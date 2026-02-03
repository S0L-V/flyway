import { $, textOrDash, getContextPath } from "./utils.js";
import { fetchJson, fetchOk } from "./api.js";
import { state } from "./state.js";
import { updateDashboardProfile } from "./render/dashboard.js";

let birthPickerInitialized = false;
let passportDatePickerInitialized = false;

// 커스텀 년/월 드롭다운 플러그인 생성
function createMonthSelectPlugin() {
    return function(fp) {
        let yearDropdown, monthDropdown;
        let yearBtn, monthBtn;
        let yearList, monthList;

        function createCustomDropdown(type, items, onChange) {
            const wrapper = document.createElement("div");
            wrapper.className = `fp-custom-dropdown fp-${type}-dropdown`;

            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "fp-dropdown-btn";
            btn.innerHTML = `<span class="fp-dropdown-text"></span><svg class="fp-dropdown-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m6 9 6 6 6-6"/></svg>`;

            const list = document.createElement("div");
            list.className = `fp-dropdown-list fp-${type}-list`;

            items.forEach(item => {
                const option = document.createElement("div");
                option.className = "fp-dropdown-option";
                option.dataset.value = item.value;
                option.textContent = item.label;
                option.addEventListener("click", (e) => {
                    e.stopPropagation();
                    onChange(item.value);
                    closeAllDropdowns();
                });
                list.appendChild(option);
            });

            // 리스트를 body에 추가 (z-index 문제 해결)
            document.body.appendChild(list);

            btn.addEventListener("click", (e) => {
                e.stopPropagation();
                const isOpen = wrapper.classList.contains("open");
                closeAllDropdowns();
                if (!isOpen) {
                    wrapper.classList.add("open");
                    list.classList.add("open");

                    // 드롭다운 위치 계산 (버튼 바로 아래 중앙 정렬)
                    const btnRect = btn.getBoundingClientRect();
                    list.style.top = (btnRect.bottom + 4) + "px";
                    list.style.left = btnRect.left + "px";
                    list.style.width = btnRect.width + "px";

                    // 선택된 항목으로 스크롤
                    const selected = list.querySelector(".selected");
                    if (selected) {
                        setTimeout(() => {
                            selected.scrollIntoView({ block: "center" });
                        }, 10);
                    }
                }
            });

            wrapper.appendChild(btn);

            return { wrapper, btn, list };
        }

        function closeAllDropdowns() {
            document.querySelectorAll(".fp-custom-dropdown.open").forEach(d => d.classList.remove("open"));
            document.querySelectorAll(".fp-dropdown-list.open").forEach(d => d.classList.remove("open"));
        }

        function buildDropdowns() {
            const currentYear = new Date().getFullYear();
            const minYear = 1920;
            const maxYear = currentYear + 20;

            // 년도 아이템
            const yearItems = [];
            for (let y = maxYear; y >= minYear; y--) {
                yearItems.push({ value: y, label: y + "년" });
            }

            // 월 아이템
            const monthItems = [];
            const months = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];
            months.forEach((m, i) => {
                monthItems.push({ value: i, label: m });
            });

            const yearDropdownResult = createCustomDropdown("year", yearItems, (val) => {
                fp.changeYear(parseInt(val));
            });
            yearDropdown = yearDropdownResult.wrapper;
            yearBtn = yearDropdownResult.btn;
            yearList = yearDropdownResult.list;

            const monthDropdownResult = createCustomDropdown("month", monthItems, (val) => {
                fp.changeMonth(parseInt(val), false);
            });
            monthDropdown = monthDropdownResult.wrapper;
            monthBtn = monthDropdownResult.btn;
            monthList = monthDropdownResult.list;
        }

        function updateDropdowns() {
            if (yearBtn && monthBtn) {
                yearBtn.querySelector(".fp-dropdown-text").textContent = fp.currentYear + "년";
                monthBtn.querySelector(".fp-dropdown-text").textContent = (fp.currentMonth + 1) + "월";

                // 선택 상태 업데이트
                yearList.querySelectorAll(".fp-dropdown-option").forEach(opt => {
                    opt.classList.toggle("selected", parseInt(opt.dataset.value) === fp.currentYear);
                });
                monthList.querySelectorAll(".fp-dropdown-option").forEach(opt => {
                    opt.classList.toggle("selected", parseInt(opt.dataset.value) === fp.currentMonth);
                });
            }
        }

        // 외부 클릭 시 드롭다운 닫기
        document.addEventListener("click", closeAllDropdowns);

        return {
            onReady: function() {
                buildDropdowns();

                const monthNav = fp.calendarContainer.querySelector(".flatpickr-current-month");
                if (monthNav) {
                    monthNav.innerHTML = "";
                    monthNav.className = "flatpickr-current-month fp-custom-nav";
                    monthNav.appendChild(yearDropdown);
                    monthNav.appendChild(monthDropdown);
                }

                updateDropdowns();
            },
            onMonthChange: updateDropdowns,
            onYearChange: updateDropdowns,
            onOpen: updateDropdowns,
            onClose: closeAllDropdowns
        };
    };
}

// 생년월일 달력 초기화
export function initBirthDatePicker() {
    if (birthPickerInitialized) return;

    const input = $("profileBirth");
    if (!input) return;

    if (typeof flatpickr === "undefined") {
        console.warn("Flatpickr not loaded");
        return;
    }

    birthPickerInitialized = true;

    const fp = flatpickr(input, {
        locale: "ko",
        dateFormat: "Y-m-d",
        maxDate: "today",
        disableMobile: true,
        allowInput: false,
        clickOpens: true,
        plugins: [createMonthSelectPlugin()]
    });

    // 아이콘 클릭 시에도 달력 열기
    const wrapper = input.closest('.date-picker-wrapper');
    if (wrapper) {
        const icon = wrapper.querySelector('.date-picker-icon');
        if (icon) {
            icon.addEventListener('click', () => fp.open());
        }
    }
}

// 여권 만료일 달력 초기화
export function initPassportDatePicker() {
    if (passportDatePickerInitialized) return;

    const input = $("profilePassportExpiryDate");
    if (!input) return;

    if (typeof flatpickr === "undefined") {
        console.warn("Flatpickr not loaded");
        return;
    }

    passportDatePickerInitialized = true;

    const fp = flatpickr(input, {
        locale: "ko",
        dateFormat: "Y-m-d",
        minDate: "today",
        disableMobile: true,
        allowInput: false,
        clickOpens: true,
        plugins: [createMonthSelectPlugin()]
    });

    // 아이콘 클릭 시에도 달력 열기
    const wrapper = input.closest('.date-picker-wrapper');
    if (wrapper) {
        const icon = wrapper.querySelector('.date-picker-icon');
        if (icon) {
            icon.addEventListener('click', () => fp.open());
        }
    }
}

export function updateProfileTab(profile) {
    const name = profile?.name || "";

    const elHeaderInitial = $("profileHeaderInitial");
    const elHeaderName = $("profileHeaderName");
    const elHeaderEmail = $("profileHeaderEmail");
    const elHeaderPhone = $("profileHeaderPhone");

    const displayName = name
        || `${profile?.krLastName || ""}${profile?.krFirstName || ""}`.trim()
        || `${profile?.lastName || ""} ${profile?.firstName || ""}`.trim()
        || "-";

    const initial = (profile?.lastName || profile?.firstName)
        ? `${profile?.lastName ? profile.lastName[0] : ""}${profile?.firstName ? profile.firstName[0] : ""}`.toUpperCase()
        : (displayName || "-").substring(0, 1);

    if (elHeaderInitial) elHeaderInitial.textContent = initial || "-";
    if (elHeaderName) elHeaderName.textContent = displayName;
    if (elHeaderEmail) elHeaderEmail.textContent = textOrDash(profile?.email);
    if (elHeaderPhone) elHeaderPhone.textContent = textOrDash(formatPhoneNumber(profile?.phoneNumber));

    const elKrLast = $("profileKrLastName");
    const elKrFirst = $("profileKrFirstName");
    const elLast = $("profileLastName");
    const elFirst = $("profileFirstName");
    const elGender = $("profileGender");
    const elBirth = $("profileBirth");
    const elCountry = $("profileCountry");
    const elPassportIssueCountry = $("profilePassportIssueCountry");
    const elPassportNo = $("profilePassportNo");
    const elPassportExpiryDate = $("profilePassportExpiryDate");

    if (elKrLast) elKrLast.value = profile?.krLastName || "";
    if (elKrFirst) elKrFirst.value = profile?.krFirstName || "";
    if (elLast) elLast.value = profile?.lastName || "";
    if (elFirst) elFirst.value = profile?.firstName || "";
    if (elGender) elGender.value = profile?.gender || "";
    if (elBirth) elBirth.value = profile?.birth || "";
    if (elCountry) elCountry.value = normalizeCountryValue(profile?.country);
    if (elPassportIssueCountry) elPassportIssueCountry.value = normalizeCountryValue(profile?.passportIssueCountry);
    if (elPassportNo) elPassportNo.value = profile?.passportNo || "";

    // 생년월일 달력 초기화
    initBirthDatePicker();
    if (elBirth && profile?.birth) {
        const fpBirth = elBirth._flatpickr;
        if (fpBirth) {
            fpBirth.setDate(profile.birth, true);
        } else {
            elBirth.value = profile.birth;
        }
    }

    // 여권 만료일 달력 초기화
    initPassportDatePicker();
    if (elPassportExpiryDate && profile?.passportExpiryDate) {
        const fpExpiry = elPassportExpiryDate._flatpickr;
        if (fpExpiry) {
            fpExpiry.setDate(profile.passportExpiryDate, true);
        } else {
            elPassportExpiryDate.value = profile.passportExpiryDate;
        }
    }

    initProfileInputGuards();
}

function normalizeCountryValue(value) {
    if (!value) return "";
    const trimmed = String(value).trim();
    const map = {
        "대한민국": "KOR",
        "대한민국 (KOR)": "KOR",
        "미국": "USA",
        "미국 (USA)": "USA",
        "일본": "JPN",
        "일본 (JPN)": "JPN",
        "중국": "CHN",
        "중국 (CHN)": "CHN",
        "베트남": "VNM",
        "베트남 (VNM)": "VNM",
    };
    return map[trimmed] || trimmed;
}

let guardsBound = false;
let profileSaveBound = false;

export function initProfileInputGuards() {
    if (guardsBound) return;
    guardsBound = true;
    document.addEventListener("input", handleGuardInput, true);
    document.addEventListener("compositionend", handleGuardInput, true);
}

let withdrawBound = false;
let withdrawInFlight = false;

async function handleWithdrawClick(event) {
    if (withdrawInFlight) return false;
    withdrawInFlight = true;
    if (event && typeof event.preventDefault === "function") {
        event.preventDefault();
        if (typeof event.stopImmediatePropagation === "function") {
            event.stopImmediatePropagation();
        }
        if (typeof event.stopPropagation === "function") {
            event.stopPropagation();
        }
    }
    let ok = false;
    if (typeof Swal !== "undefined" && typeof Swal.confirmDelete === "function") {
        ok = await Swal.confirmDelete("탈퇴 시 모든 개인정보 및 예약 내역이 삭제됩니다.", {
            title: "정말로 탈퇴하시겠습니까?",
            confirmText: "탈퇴",
            cancelText: "취소"
        });
    } else {
        ok = window.confirm("정말로 탈퇴하시겠습니까?\n탈퇴 시 모든 개인정보 및 예약 내역이 삭제됩니다.");
    }
    if (!ok) {
        withdrawInFlight = false;
        return false;
    }

    const withdrawButton = $("withdrawButton");
    try {
        if (withdrawButton) withdrawButton.disabled = true;
        const base = getContextPath();
        const res = await fetchOk(`${base}/api/user/withdraw`, { method: "POST" });
        if (res && res.status === 204) {
            window.location.replace(window.APP?.contextPath || "/");
            return false;
        }
        window.location.replace(window.APP?.contextPath || "/");
        return false;
    } catch (e) {
        console.error(e);
        if (typeof Swal !== "undefined") {
            Swal.error("회원 탈퇴에 실패했습니다.");
        }
        if (withdrawButton) withdrawButton.disabled = false;
        withdrawInFlight = false;
        return false;
    }
}

export function initProfileSave() {
    if (profileSaveBound) return;
    profileSaveBound = true;
    const btn = $("profileSaveButton");
    if (!btn) return;
    btn.addEventListener("click", async () => {
        const payload = {};
        const krLastName = $("profileKrLastName")?.value?.trim();
        const krFirstName = $("profileKrFirstName")?.value?.trim();
        const lastName = $("profileLastName")?.value?.trim();
        const firstName = $("profileFirstName")?.value?.trim();
        const gender = $("profileGender")?.value?.trim();
        const birth = $("profileBirth")?.value;
        const country = $("profileCountry")?.value?.trim();
        const passportIssueCountry = $("profilePassportIssueCountry")?.value?.trim();
        const passportNo = $("profilePassportNo")?.value?.trim();
        const passportExpiryDate = $("profilePassportExpiryDate")?.value;

        if (krLastName) payload.krLastName = toKoreanOnly(krLastName);
        if (krFirstName) payload.krFirstName = toKoreanOnly(krFirstName);
        if (lastName) payload.lastName = toUppercaseLetters(lastName);
        if (firstName) payload.firstName = toUppercaseLetters(firstName);
        if (gender) payload.gender = gender;
        if (birth) payload.birth = birth;
        if (country) payload.country = country;
        if (passportIssueCountry) payload.passportIssueCountry = passportIssueCountry;
        if (passportNo) payload.passportNo = toPassportOnly(passportNo);
        if (passportExpiryDate) payload.passportExpiryDate = passportExpiryDate;

        if (Object.keys(payload).length === 0) {
            if (typeof window.showToast === "function") {
                window.showToast("변경사항이 없습니다.");
            }
            return;
        }

        try {
            const base = getContextPath();
            const res = await fetchJson(`${base}/api/profile`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });
            state.profile = res.data;
            updateDashboardProfile(res.data);
            updateProfileTab(res.data);
            if (typeof window.showToast === "function") {
                window.showToast("회원 정보가 저장되었습니다.");
            }
        } catch (e) {
            console.error(e);
            if (typeof window.showToast === "function") {
                window.showToast("저장에 실패했습니다.");
            }
        }
    });
}

export function initWithdrawHandler() {
    if (withdrawBound) return;
    withdrawBound = true;
    const btn = $("withdrawButton");
    if (btn) {
        btn.addEventListener("click", handleWithdrawClick);
    }
    window.handleWithdrawClick = handleWithdrawClick;
}

function toUppercaseLetters(value) {
    if (!value) return "";
    return value.replace(/[^a-zA-Z\s-]/g, "").toUpperCase();
}

function toKoreanOnly(value) {
    if (!value) return "";
    return value.replace(/[^ㄱ-ㅎ가-힣\s]/g, "");
}

function toPassportOnly(value) {
    if (!value) return "";
    return value.replace(/[^a-zA-Z0-9]/g, "").toUpperCase();
}

function formatPhoneNumber(value) {
    if (!value) return "";
    const digits = String(value).replace(/\D/g, "");
    if (digits.length === 11) {
        return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
    }
    if (digits.length === 10) {
        return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
    }
    return value;
}

function handleGuardInput(e) {
    const target = e.target;
    if (!target || !target.id) return;
    if (target.id === "profileKrLastName" || target.id === "profileKrFirstName") {
        target.value = toKoreanOnly(target.value);
        return;
    }
    if (target.id === "profileLastName" || target.id === "profileFirstName") {
        target.value = toUppercaseLetters(target.value);
        return;
    }
    if (target.id === "profilePassportNo") {
        target.value = toPassportOnly(target.value);
    }
}

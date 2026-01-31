import { $, formatDate, textOrDash } from "../utils.js";
import { fetchJson } from "../api.js";

const SERVICE_TYPE = {
    BAGGAGE: "0",
    MEAL: "1",
};

const COUNTRY_MAP = {
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

function normalizeCountryValue(value) {
    if (!value) return "";
    const trimmed = String(value).trim();
    return COUNTRY_MAP[trimmed] || trimmed;
}

function formatKoreanName(profile) {
    if (!profile) return "-";
    const name = `${profile.krLastName || ""}${profile.krFirstName || ""}`.trim();
    return name || "-";
}

function formatEnglishName(profile) {
    if (!profile) return "-";
    const name = `${profile.lastName || ""} ${profile.firstName || ""}`.trim();
    return name ? name.toUpperCase() : "-";
}

function formatInitial(profile) {
    if (!profile) return "-";
    if (profile.lastName || profile.firstName) {
        const ln = profile.lastName ? profile.lastName[0] : "";
        const fn = profile.firstName ? profile.firstName[0] : "";
        return `${ln}${fn}`.toUpperCase() || "-";
    }
    return (profile.krLastName || profile.krFirstName || "-").substring(0, 2);
}

function formatGender(gender) {
    const key = (gender || "").toString().toUpperCase();
    if (key === "M" || key === "MALE") return "남성";
    if (key === "F" || key === "FEMALE") return "여성";
    return gender || "-";
}

function formatPhoneNumber(value) {
    if (!value) return "-";
    const digits = String(value).replace(/\D/g, "");
    if (digits.length === 11) {
        return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
    }
    if (digits.length === 10) {
        return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
    }
    return value;
}

function formatSegmentLabel(segmentOrder) {
    return segmentOrder === 2 ? "오는 편" : "가는 편";
}

function summarizeSeats(segments) {
    const list = (segments || [])
        .filter((seg) => seg?.seat?.seatNo)
        .slice()
        .sort((a, b) => (a.segmentOrder || 0) - (b.segmentOrder || 0))
        .map((seg) => `${formatSegmentLabel(seg.segmentOrder)} ${seg.seat.seatNo}`);
    return list.length ? list.join(" / ") : "-";
}

function parseBaggageDetails(serviceDetails) {
    if (!serviceDetails) return { extraKg: 0, extraBags: 0 };
    try {
        const parsed = JSON.parse(serviceDetails);
        return {
            extraKg: Number(parsed?.extraKg || 0),
            extraBags: Number(parsed?.extraBags || 0),
        };
    } catch (e) {
        return { extraKg: 0, extraBags: 0 };
    }
}

function summarizeBaggageItems(segments) {
    const items = [];
    const list = (segments || []).slice().sort((a, b) => (a.segmentOrder || 0) - (b.segmentOrder || 0));
    list.forEach((seg) => {
        const services = Array.isArray(seg?.services) ? seg.services : [];
        let totalKg = 0;
        let totalBags = 0;
        services
            .filter((svc) => svc?.serviceType === SERVICE_TYPE.BAGGAGE)
            .forEach((svc) => {
                const details = parseBaggageDetails(svc.serviceDetails);
                totalKg += details.extraKg || 0;
                totalBags += details.extraBags || 0;
            });
        const parts = [];
        if (totalKg > 0) parts.push(`추가 ${totalKg}kg`);
        if (totalBags > 0) parts.push(`추가 ${totalBags}개`);
        items.push({
            label: formatSegmentLabel(seg.segmentOrder),
            value: parts.length ? parts.join(" · ") : "선택 없음",
        });
    });
    return items;
}

function summarizeMealItems(segments) {
    const items = [];
    const list = (segments || []).slice().sort((a, b) => (a.segmentOrder || 0) - (b.segmentOrder || 0));
    list.forEach((seg) => {
        const services = Array.isArray(seg?.services) ? seg.services : [];
        const names = services
            .filter((svc) => svc?.serviceType === SERVICE_TYPE.MEAL)
            .map((svc) => svc.mealName || svc.mealId)
            .filter((value) => value);
        items.push({
            label: formatSegmentLabel(seg.segmentOrder),
            value: names.length ? names.join(", ") : "선택 없음",
        });
    });
    return items;
}

function renderServiceSummary(el, items) {
    if (!el) return;
    el.innerHTML = "";
    if (!items || items.length === 0) {
        el.textContent = "선택 없음";
        return;
    }
    const wrapper = document.createElement("div");
    wrapper.className = "service-summary";
    items.forEach((item) => {
        const row = document.createElement("div");
        row.className = "service-summary-item";
        const label = document.createElement("span");
        label.className = "service-summary-label";
        label.textContent = item.label;
        const value = document.createElement("span");
        value.className = "service-summary-value";
        value.textContent = item.value || "선택 없음";
        row.appendChild(label);
        row.appendChild(value);
        wrapper.appendChild(row);
    });
    el.appendChild(wrapper);
}

function formatPassportStatus(passport) {
    const countryValue = passport?.country || passport?.issueCountry;
    const hasAny = !!(passport?.passportNo || passport?.expiryDate || passport?.issueCountry || passport?.country);
    const isComplete = !!(passport?.passportNo && passport?.expiryDate && passport?.issueCountry && countryValue);
    if (isComplete) {
        return { label: "등록 완료", className: "bg-primary-50 text-primary-700 border-primary-200" };
    }
    if (hasAny) {
        return { label: "일부 등록", className: "bg-amber-50 text-amber-700 border-amber-200" };
    }
    return { label: "미등록", className: "bg-slate-50 text-slate-600 border-slate-200" };
}

function ensureSelectValue(selectEl, value) {
    if (!selectEl) return "";
    const normalized = normalizeCountryValue(value);
    if (!normalized) {
        selectEl.value = "";
        return "";
    }
    const hasOption = Array.from(selectEl.options).some((opt) => opt.value === normalized);
    if (!hasOption) {
        const option = document.createElement("option");
        option.value = normalized;
        option.textContent = normalized;
        selectEl.appendChild(option);
    }
    selectEl.value = normalized;
    return normalized;
}

function setOriginalValue(el, value) {
    if (!el) return;
    el.dataset.original = value ?? "";
}

function getOriginalValue(el) {
    return el?.dataset?.original ?? "";
}

function normalizePassportNo(value) {
    if (!value) return "";
    return String(value).replace(/[^a-zA-Z0-9]/g, "").toUpperCase();
}

function showPassportMessage(el, message, tone = "info") {
    if (!el) return;
    el.textContent = message;
    el.classList.remove("hidden");
    el.classList.remove("text-slate-400", "text-primary-600", "text-red-500");
    if (tone === "success") {
        el.classList.add("text-primary-600");
    } else if (tone === "error") {
        el.classList.add("text-red-500");
    } else {
        el.classList.add("text-slate-400");
    }
}

function hidePassportMessage(el) {
    if (!el) return;
    el.classList.add("hidden");
    el.textContent = "";
}

export function renderSeatSummary(passengers) {
    const container = $("seatSummaryList");
    const empty = $("seatSummaryEmpty");
    const template = $("seatSummaryTemplate");
    if (!container || !template) return;

    container.innerHTML = "";
    if (!passengers || passengers.length === 0) {
        if (empty) empty.classList.remove("hidden");
        return;
    }
    if (empty) empty.classList.add("hidden");

    passengers.forEach((p) => {
        const node = template.content.cloneNode(true);
        const profile = p.profile || {};
        const name = profile.lastName || profile.firstName
            ? `${profile.lastName || ""} ${profile.firstName || ""}`.trim()
            : `${profile.krLastName || ""}${profile.krFirstName || ""}`.trim();
        const initial = (profile.lastName || profile.firstName)
            ? `${profile.lastName ? profile.lastName[0] : ""}${profile.firstName ? profile.firstName[0] : ""}`.toUpperCase()
            : (profile.krLastName || profile.krFirstName || "-").substring(0, 2);

        const segments = Array.isArray(p.segments) ? p.segments : [];
        const sorted = segments.slice().sort((a, b) => (a.segmentOrder || 0) - (b.segmentOrder || 0));
        const firstSeg = sorted[0];
        const seatValue = firstSeg?.seat?.seatNo || "-";
        const segmentLabel = firstSeg?.segmentOrder === 2 ? "오는 편" : "가는 편";

        const elInitial = node.querySelector('[data-field="initial"]');
        const elName = node.querySelector('[data-field="name"]');
        const elSeat = node.querySelector('[data-field="seat"]');
        const elSegmentLabel = node.querySelector('[data-field="segmentLabel"]');
        if (elInitial) elInitial.textContent = initial || "-";
        if (elName) elName.textContent = name || "-";
        if (elSeat) elSeat.textContent = seatValue;
        if (elSegmentLabel) elSegmentLabel.textContent = segmentLabel;

        container.appendChild(node);
    });
}

export function renderSeatModalPassengers(passengers) {
    const container = $("seatModalPassengers");
    const empty = $("seatModalPassengersEmpty");
    const template = $("seatModalPassengerTemplate");
    if (!container || !template) return;

    container.innerHTML = "";
    if (!passengers || passengers.length === 0) {
        if (empty) empty.classList.remove("hidden");
        return;
    }
    if (empty) empty.classList.add("hidden");

    passengers.forEach((p) => {
        const node = template.content.cloneNode(true);
        const profile = p.profile || {};
        const name = profile.lastName || profile.firstName
            ? `${profile.lastName || ""} ${profile.firstName || ""}`.trim()
            : `${profile.krLastName || ""}${profile.krFirstName || ""}`.trim();
        const initial = (profile.lastName || profile.firstName)
            ? `${profile.lastName ? profile.lastName[0] : ""}${profile.firstName ? profile.firstName[0] : ""}`.toUpperCase()
            : (profile.krLastName || profile.krFirstName || "-").substring(0, 2);

        const segments = Array.isArray(p.segments) ? p.segments : [];
        const sorted = segments.slice().sort((a, b) => (a.segmentOrder || 0) - (b.segmentOrder || 0));
        const firstSeg = sorted[0];
        const seatValue = firstSeg?.seat?.seatNo || "-";

        const elInitial = node.querySelector('[data-field="initial"]');
        const elName = node.querySelector('[data-field="name"]');
        const elSeat = node.querySelector('[data-field="seat"]');
        if (elInitial) elInitial.textContent = initial || "-";
        if (elName) elName.textContent = name || "-";
        if (elSeat) elSeat.textContent = seatValue;

        container.appendChild(node);
    });
}

export function renderPassengerInfo(passengers, reservationId) {
    const container = $("passengerInfoList");
    const empty = $("passengerInfoEmpty");
    const template = $("passengerInfoTemplate");
    if (!container || !template) return;

    container.innerHTML = "";
    if (!passengers || passengers.length === 0) {
        if (empty) empty.classList.remove("hidden");
        return;
    }
    if (empty) empty.classList.add("hidden");

    passengers.forEach((p) => {
        const node = template.content.cloneNode(true);
        const profile = p.profile || {};
        const passport = profile.passport || {};
        const segments = Array.isArray(p.segments) ? p.segments : [];

        console.log(p.profile)

        const nameEn = formatEnglishName(profile);
        const nameKr = formatKoreanName(profile);
        const initial = formatInitial(profile);
        const status = formatPassportStatus(passport);

        const elInitial = node.querySelector('[data-field="initial"]');
        const elNameEn = node.querySelector('[data-field="nameEn"]');
        const elNameKr = node.querySelector('[data-field="nameKr"]');
        const elStatus = node.querySelector('[data-field="passportStatus"]');
        const elGenderBirth = node.querySelector('[data-field="genderBirth"]');
        const elPhone = node.querySelector('[data-field="phone"]');
        const elEmail = node.querySelector('[data-field="email"]');
        const elSeats = node.querySelector('[data-field="seats"]');
        const elPassportCountry = node.querySelector('[data-field="passportCountryInput"]');
        const elPassportIssueCountry = node.querySelector('[data-field="passportIssueCountryInput"]');
        const elPassportNo = node.querySelector('[data-field="passportNoInput"]');
        const elPassportExpiry = node.querySelector('[data-field="passportExpiryInput"]');
        const elPassportMessage = node.querySelector('[data-field="passportMessage"]');
        const elPassportSave = node.querySelector('[data-action="savePassport"]');
        const elBaggage = node.querySelector('[data-field="baggage"]');
        const elMeal = node.querySelector('[data-field="meal"]');

        if (elInitial) elInitial.textContent = initial;
        if (elNameEn) elNameEn.textContent = nameEn;
        if (elNameKr) elNameKr.textContent = nameKr;
        if (elStatus) {
            elStatus.textContent = status.label;
            elStatus.className = `px-2.5 py-1 rounded-full text-xs font-semibold border ${status.className}`;
        }
        if (elGenderBirth) {
            const genderText = formatGender(profile.gender);
            const birthText = formatDate(profile.birth);
            elGenderBirth.textContent = `${genderText} / ${birthText}`;
        }
        if (elPhone) elPhone.textContent = formatPhoneNumber(profile.phoneNumber);
        if (elEmail) elEmail.textContent = textOrDash(profile.email);
        if (elSeats) elSeats.textContent = summarizeSeats(segments);
        const normalizedCountry = ensureSelectValue(elPassportCountry, passport.country || passport.issueCountry);
        const normalizedIssueCountry = ensureSelectValue(elPassportIssueCountry, passport.issueCountry);
        if (elPassportNo) elPassportNo.value = passport.passportNo || "";
        if (elPassportExpiry) elPassportExpiry.value = passport.expiryDate || "";

        setOriginalValue(elPassportCountry, normalizedCountry);
        setOriginalValue(elPassportIssueCountry, normalizedIssueCountry);
        setOriginalValue(elPassportNo, passport.passportNo || "");
        setOriginalValue(elPassportExpiry, passport.expiryDate || "");
        hidePassportMessage(elPassportMessage);

        if (elPassportNo) {
            elPassportNo.addEventListener("input", (event) => {
                const target = event.target;
                const normalized = normalizePassportNo(target.value);
                if (target.value !== normalized) target.value = normalized;
            });
        }

        if (elPassportSave && reservationId && p.passengerId) {
            elPassportSave.addEventListener("click", async () => {
                hidePassportMessage(elPassportMessage);
                const passportNoValue = normalizePassportNo(elPassportNo?.value?.trim());
                const expiryValue = elPassportExpiry?.value || "";
                const countryValue = normalizeCountryValue(elPassportCountry?.value?.trim());
                const issueCountryValue = normalizeCountryValue(elPassportIssueCountry?.value?.trim());

                if (!passportNoValue || !expiryValue || !countryValue || !issueCountryValue) {
                    showPassportMessage(elPassportMessage, "여권 정보를 모두 입력해 주세요.", "error");
                    if (typeof window.showToast === "function") {
                        window.showToast("여권 정보를 모두 입력해 주세요.");
                    }
                    return;
                }

                const changed = {
                    passportNo: passportNoValue !== getOriginalValue(elPassportNo),
                    expiryDate: expiryValue !== getOriginalValue(elPassportExpiry),
                    country: countryValue !== getOriginalValue(elPassportCountry),
                    issueCountry: issueCountryValue !== getOriginalValue(elPassportIssueCountry),
                };

                if (!changed.passportNo && !changed.expiryDate && !changed.country && !changed.issueCountry) {
                    showPassportMessage(elPassportMessage, "변경사항이 없습니다.", "info");
                    if (typeof window.showToast === "function") {
                        window.showToast("변경사항이 없습니다.");
                    }
                    return;
                }

                const payload = {
                    passportNo: passportNoValue,
                    expiryDate: expiryValue,
                    country: countryValue,
                    issueCountry: issueCountryValue,
                };

                const originalText = elPassportSave.textContent;
                elPassportSave.disabled = true;
                elPassportSave.textContent = "저장 중...";
                try {
                    await fetchJson(`/api/users/me/reservations/${reservationId}/passengers/${p.passengerId}/passport`, {
                        method: "PATCH",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(payload),
                    });
                    setOriginalValue(elPassportCountry, countryValue);
                    setOriginalValue(elPassportIssueCountry, issueCountryValue);
                    setOriginalValue(elPassportNo, passportNoValue);
                    setOriginalValue(elPassportExpiry, expiryValue);

                    const statusNow = formatPassportStatus({
                        passportNo: passportNoValue,
                        expiryDate: expiryValue,
                        issueCountry: issueCountryValue,
                        country: countryValue,
                    });
                    if (elStatus) {
                        elStatus.textContent = statusNow.label;
                        elStatus.className = `px-2.5 py-1 rounded-full text-xs font-semibold border ${statusNow.className}`;
                    }

                    showPassportMessage(elPassportMessage, "여권 정보가 저장되었습니다.", "success");
                    if (typeof window.showToast === "function") {
                        window.showToast("여권 정보가 저장되었습니다.");
                    }
                } catch (e) {
                    console.error(e);
                    showPassportMessage(elPassportMessage, "저장에 실패했습니다.", "error");
                    if (typeof window.showToast === "function") {
                        window.showToast("저장에 실패했습니다.");
                    }
                } finally {
                    elPassportSave.disabled = false;
                    elPassportSave.textContent = originalText || "정보 저장";
                }
            });
        }
        if (elBaggage) renderServiceSummary(elBaggage, summarizeBaggageItems(segments));
        if (elMeal) renderServiceSummary(elMeal, summarizeMealItems(segments));

        container.appendChild(node);
    });

    if (window.lucide?.createIcons) {
        window.lucide.createIcons();
    }
}

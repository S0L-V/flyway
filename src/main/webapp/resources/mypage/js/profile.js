import { $, splitKoreanName, textOrDash } from "./utils.js";
import { fetchJson, fetchOk } from "./api.js";
import { state } from "./state.js";
import { updateDashboardProfile } from "./render/dashboard.js";

export function updateProfileTab(profile) {
    const name = profile?.name || "";
    const kr = splitKoreanName(name);

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

    if (elKrLast) elKrLast.value = profile?.krLastName || kr.last || "";
    if (elKrFirst) elKrFirst.value = profile?.krFirstName || kr.first || "";
    if (elLast) elLast.value = profile?.lastName || "";
    if (elFirst) elFirst.value = profile?.firstName || "";
    if (elGender) elGender.value = profile?.gender || "";
    if (elBirth) elBirth.value = profile?.birth || "";
    if (elCountry) elCountry.value = normalizeCountryValue(profile?.country);
    if (elPassportIssueCountry) elPassportIssueCountry.value = normalizeCountryValue(profile?.passportIssueCountry);
    if (elPassportNo) elPassportNo.value = profile?.passportNo || "";
    if (elPassportExpiryDate) elPassportExpiryDate.value = profile?.passportExpiryDate || "";

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
    const ok = window.confirm("정말로 탈퇴하시겠습니까?\n탈퇴 시 모든 개인정보 및 예약 내역이 삭제됩니다.");
    if (!ok) {
        withdrawInFlight = false;
        return false;
    }

    const withdrawButton = $("withdrawButton");
    try {
        if (withdrawButton) withdrawButton.disabled = true;
        const res = await fetchOk("/api/user/withdraw", { method: "POST" });
        if (res && res.status === 204) {
            window.location.replace(window.APP?.contextPath || "/");
            return false;
        }
        window.location.replace(window.APP?.contextPath || "/");
        return false;
    } catch (e) {
        console.error(e);
        alert("회원 탈퇴에 실패했습니다.");
        if (withdrawButton) withdrawButton.disabled = false;
        withdrawInFlight = false;
        return false;
    }
}

export function initProfileSave() {
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
        // phoneNumber is displayed in header only
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
            } else {
                alert("변경사항이 없습니다.");
            }
            return;
        }

        try {
            const res = await fetchJson("/api/profile", {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });
            state.profile = res.data;
            updateDashboardProfile(res.data);
            updateProfileTab(res.data);
            if (typeof window.showToast === "function") {
                window.showToast("회원 정보가 저장되었습니다.");
            } else {
                alert("회원 정보가 저장되었습니다.");
            }
        } catch (e) {
            console.error(e);
            if (typeof window.showToast === "function") {
                window.showToast("저장에 실패했습니다.");
            } else {
                alert("저장에 실패했습니다.");
            }
        }
    });
}

export function initWithdrawHandler() {
    if (withdrawBound) return;
    withdrawBound = true;
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

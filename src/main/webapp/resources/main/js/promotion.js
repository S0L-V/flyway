document.addEventListener("DOMContentLoaded", () => {
    loadPromotionCard();

    const sliderWrapper = document.getElementById("slider-wrapper");
    if (sliderWrapper) {
        sliderWrapper.addEventListener("mouseenter", stopAutoSlide);
        sliderWrapper.addEventListener("mouseleave", startAutoSlide);
    }
});

window.addEventListener("load", () => {
    requestAnimationFrame(() => {
        document.getElementById("offerTrack")
            .classList.remove("is-entering");
    });
});

let currentIndex = 0;

function getIndex(i, length) {
    return (i + length) % length;
}

function updateCarousel(cards) {
    const len = cards.length;

    cards.forEach((card, i) => {
        card.className = "offer-card";

        const diff = getIndex(i - currentIndex, len);

        if (diff === 0) card.classList.add("center");
        else if (diff === 1) card.classList.add("right");
        else if (diff === 2) card.classList.add("right2");
        else if (diff === len - 1) card.classList.add("left");
        else if (diff === len - 2) card.classList.add("left2");
        else card.classList.add("hidden");
    });
}

document.getElementById("offerNext").onclick = () => {
    stopAutoSlide();
    moveNext();
    startAutoSlide();
};

document.getElementById("offerPrev").onclick = () => {
    stopAutoSlide();
    movePrev();
    startAutoSlide();
};

// >
function movePrev() {
    const cards = document.querySelectorAll(".offer-card");
    currentIndex = (currentIndex - 1 + cards.length) % cards.length;
    updateCarousel(cards);
}

// > 화살표 클릭시
function moveNext() {
    const cards = document.querySelectorAll(".offer-card");
    currentIndex = (currentIndex + 1) % cards.length;
    updateCarousel(cards);
}

// api 호출
async function loadPromotionCard() {
    try {
        const res = await fetch(`${CONTEXT_PATH}/api/public/promotions`);
        if(!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }
        const result = await res.json();

        console.log("받아온 데이터:", result);

        renderPromotionCard(result.data);
    } catch (e) {
        console.error("특가 카드 로딩 실패", e);
    }
}

function renderPromotionCard(list) {
    const track = document.querySelector(".main-slider-track");
    track.innerHTML = "";

    const nextBtn = document.getElementById("offerNext");
    const prevBtn = document.getElementById("offerPrev");

    if (!Array.isArray(list) || list.length === 0) {
        stopAutoSlide();
        if (nextBtn) nextBtn.disabled = true;
        if (prevBtn) prevBtn.disabled = true;
        return;
    }
    if (nextBtn) nextBtn.disabled = false;
    if (prevBtn) prevBtn.disabled = false;

    list.forEach((item, index) => {
        const div = document.createElement("div");
        div.className = "offer-card";

        div.style.setProperty("--i", index);

        if(item.imageUrl) {
            div.style.backgroundImage = `url('${item.imageUrl}')`;
        }

        div.addEventListener("click", () => {
            goToBooking(item);
        });

        const safeAirline = escapeHtml(item.airlineName);
        const safeTitle = escapeHtml(item.title);
        const safeArrival = escapeHtml(item.arrivalAirportName);
        const safeDate = escapeHtml(renderDate(item.departureTime));
        const safePax = escapeHtml(item.passengerCount);
        const safeOriginal = escapeHtml(formatSalePrice(item.originalPrice));
        const safeSale = escapeHtml(formatSalePrice(item.salePrice));

        div.innerHTML = `
            <div class="card-badges">${renderTag(item.tags)}</div>
            <div class="offer-overlay">
                <div class="offer-header">
                    <div class="offer-airline">${safeAirline}</div>
                    <h3 class="offer-title">${safeTitle}</h3>
                    <div class="offer-city">${safeArrival}</div>
                </div>
        
                <div class="offer-date-row">
                    <span class="offer-date">📅 ${safeDate}</span>
                    <span class="offer-person"><span class="icon-only">👤</span> ${safePax}명</span>
                </div>
        
                <div class="offer-footer">
                    <div class="price-info-left">
                        <span class="price-label">성인 1인 편도</span>
                        <span class="original-price">₩${safeOriginal}</span>
                    </div>
                    <div class="price-info-right">
                        <span class="final-price">₩${safeSale}</span>
                    </div>
                </div>
            </div>
        `

        track.appendChild(div);
    })

    requestAnimationFrame(() => {
        track.classList.remove("is-entering");

        requestAnimationFrame(() => {
            updateCarousel(document.querySelectorAll(".offer-card"));
            startAutoSlide();
        });
    });
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
    }

function renderTag(tagString) {
    if (!tagString) return '';

    return tagString
        .split(',')
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0)
        .map(tag => `<span class="card-badge blue">${escapeHtml(tag)}</span>`)
        .join('')
}

function renderDate(dateString) {
    if(!dateString) {
        return '';
    }

    const date = new Date(dateString);
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    const weekDays = ['일', '월', '화', '수', '목', '금', '토'];
    const dayOfWeek = weekDays[date.getDay()];

    return `${month}.${day}(${dayOfWeek})`;
}

function formatSalePrice(price) {
    return Number(price).toLocaleString('ko-KR');
}

// 자동 슬라이드
let autoSlideTimer = null;

function startAutoSlide() {
    stopAutoSlide(); // 중복 방지
    autoSlideTimer = setInterval(() => {
        moveNext();
    }, 3000);
}

function stopAutoSlide() {
    if (autoSlideTimer) {
        clearInterval(autoSlideTimer);
        autoSlideTimer = null;
    }
}

const sliderWrapper = document.getElementById("slider-wrapper");

sliderWrapper.addEventListener("mouseenter", stopAutoSlide);
sliderWrapper.addEventListener("mouseleave", startAutoSlide);

// 클릭 시 예매페이지로
function goToBooking(item) {

    // hidden 폼에 값 설정
    document.getElementById("hiddenOutFlightId").value = item.flightId;
    document.getElementById("hiddenInFlightId").value = "";
    document.getElementById("hiddenPassengerCount").value = item.passengerCount;
    document.getElementById("hiddenCabinClassCode").value = item.cabinClassCode;
    document.getElementById("hiddenOutPrice").value = item.salePrice;
    document.getElementById("hiddenInPrice").value = 0;

    // 폼 제출 → /reservations/draft → 동의 페이지로 redirect
    document.getElementById("reservationForm").submit();
}
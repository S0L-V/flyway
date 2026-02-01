document.addEventListener("DOMContentLoaded", () => {
    if (!window.SeatConfirm || typeof window.SeatConfirm.create !== "function") {
        console.error("[seat-main] SeatConfirm missing or not loaded");
        return;
    }

    const app = window.SeatState?.init?.();
    if (!app) {
        console.error("[seat-main] SeatState.init() returned null");
        return;
    }

    const confirm = window.SeatConfirm.create(app);

    confirm.bindTermsUI();
    confirm.bindConfirmClick();

    const renderer = window.SeatRenderer?.create?.(app, confirm);
    if (!renderer) {
        console.error("[seat-main] SeatRenderer missing or create() failed");
        return;
    }

    renderer.refreshAndRender().catch((e) =>
        console.error("[seat-main] refreshAndRender failed", e)
    );

    if (!window.SeatEvents?.bind) {
        console.error("[seat-main] SeatEvents missing");
        return;
    }
    window.SeatEvents.bind(app, renderer);
});

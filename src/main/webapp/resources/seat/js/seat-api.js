(function (global) {

    async function safeJson(res) {
        const ct = res.headers.get("content-type") || "";
        const text = await res.text();
        if (!ct.includes("application/json")) throw new Error(`JSON이 아닌 응답: ${res.status}`);

        let body;
        try { body = text ? JSON.parse(text) : null; }
        catch { throw new Error(`JSON 파싱 실패: ${res.status}`); }

        if (!res.ok) {
            const msg = body?.message || body?.error || body?.msg || `HTTP ${res.status}`;
            throw new Error(msg);
        }
        return body;
    }

    function fetchSeatMap(ctx, reservationId, segmentId) {
        return csrfFetch(`${ctx}/api/seats/reservations/${encodeURIComponent(reservationId)}/segments/${encodeURIComponent(segmentId)}`, {
            credentials: 'include'  // 쿠키 포함 (JWT 인증용)
        }).then(safeJson).then(d => d?.data ?? []);
    }

    function holdSeat(ctx, reservationId, segmentId, body) {
        return csrfFetch(`${ctx}/api/seats/reservations/${encodeURIComponent(reservationId)}/segments/${encodeURIComponent(segmentId)}/hold`,
            {
                method: "POST",
                headers: { "Content-Type": "application/json", "Accept": "application/json" },
                credentials: 'include',
                body: JSON.stringify(body)
            }).then(safeJson);
    }
    function releaseHold(ctx, reservationId, segmentId, passengerId) {
        return csrfFetch(`${ctx}/api/seats/reservations/${encodeURIComponent(reservationId)}/segments/${encodeURIComponent(segmentId)}/hold/${encodeURIComponent(passengerId)}`, {
            method: "DELETE",
            headers: { "Accept": "application/json" },
            credentials: 'include'
        }).then(safeJson);
    }

    global.SeatAPI = { fetchSeatMap, holdSeat, releaseHold };
})(window);

(function () {
    const base = window.APP?.contextPath ?? "";
    const emailInput = document.getElementById("email");
    const sendBtn = document.getElementById("sendVerifyBtn");
    const sendStatus = document.getElementById("sendStatus");

    const verifyBox = document.getElementById("verifyBox");
    const verifyBtn = document.getElementById("verifyBtn");
    const verifyStatus = document.getElementById("verifyStatus");

    const emailVerifiedHidden = document.getElementById("emailVerified");
    const attemptIdHidden = document.getElementById("attemptId");
    const signupForm = document.getElementById("signupForm");

    function setText(el, msg, ok) {
        el.textContent = msg || "";
        el.style.color = ok ? "#1a7f37" : "#d1242f";
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    emailInput.addEventListener("input", function () {
        emailVerifiedHidden.value = "false";
        attemptIdHidden.value = "";
        verifyBox.classList.add("is-hidden");
        setText(sendStatus, "", true);
        setText(verifyStatus, "", true);
    });

    sendBtn.addEventListener("click", async function () {
        const email = (emailInput.value || "").trim();
        if (!isValidEmail(email)) {
            setText(sendStatus, "올바른 이메일을 입력해 주세요.", false);
            return;
        }

        sendBtn.disabled = true;
        setText(sendStatus, "인증메일을 발송 중입니다...", true);

        try {
            const res = await fetch(base + "/api/auth/email/issue", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                },
                body: "email=" + encodeURIComponent(email)
            });

            let data = null;
            try {
                data = await res.json();
                attemptIdHidden.value = data.data.attemptId;
            } catch (e) {
                data = null;
            }

            if (!res.ok) {
                const msg = data?.message || "인증메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.";
                setText(sendStatus, msg, false);
                return;
            }

            const msg = data?.message || "인증메일을 발송했습니다. 메일함의 링크를 클릭해 주세요.";
            setText(sendStatus, msg, true);
            verifyBox.classList.remove("is-hidden");
        } catch (e) {
            setText(sendStatus, "네트워크 오류가 발생했습니다.", false);
        } finally {
            sendBtn.disabled = false;
        }
    });

    verifyBtn.addEventListener("click", async function () {
        const email = (emailInput.value || "").trim();

        if (!isValidEmail(email)) {
            setText(verifyStatus, "올바른 이메일을 입력해 주세요.", false);
            return;
        }

        verifyBtn.disabled = true;
        setText(verifyStatus, "인증 확인 중입니다...", true);

        try {
            const query = new URLSearchParams({
                email: email,
                attemptId: attemptIdHidden.value
            });
            const res = await fetch(base + "/api/auth/email/status?" + query.toString());

            if (!res.ok) {
                setText(verifyStatus, "인증 확인에 실패했습니다.", false);
                emailVerifiedHidden.value = "false";
                return;
            }

            const data = await res.json();
            if (data?.success && data?.data === true) {
                setText(verifyStatus, "이메일 인증이 완료되었습니다.", true);
                emailVerifiedHidden.value = "true";
            } else {
                setText(verifyStatus, "아직 인증이 완료되지 않았습니다. 메일의 링크를 확인해 주세요.", false);
                emailVerifiedHidden.value = "false";
            }
        } catch (e) {
            setText(verifyStatus, "네트워크 오류가 발생했습니다.", false);
            emailVerifiedHidden.value = "false";
        } finally {
            verifyBtn.disabled = false;
        }
    });

    signupForm.addEventListener("submit", function (e) {
        if (emailVerifiedHidden.value !== "true") {
            e.preventDefault();
            alert("이메일 인증을 완료해 주세요.");
        }
    });

})();

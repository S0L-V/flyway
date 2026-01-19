(function () {
    const ctx = "${pageContext.request.contextPath}";
    const emailInput = document.getElementById("email");
    const sendBtn = document.getElementById("sendVerifyBtn");
    const sendStatus = document.getElementById("sendStatus");

    const verifyBox = document.getElementById("verifyBox");
    const codeInput = document.getElementById("verifyCode");
    const verifyBtn = document.getElementById("verifyBtn");
    const verifyStatus = document.getElementById("verifyStatus");

    const emailVerifiedHidden = document.getElementById("emailVerified");
    const signupForm = document.getElementById("signupForm");

    function setText(el, msg, ok) {
        el.textContent = msg || "";
        el.style.color = ok ? "#1a7f37" : "#d1242f";
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    // 이메일 변경되면 인증 상태 초기화
    emailInput.addEventListener("input", function () {
        emailVerifiedHidden.value = "false";
        verifyBox.style.display = "none";
        codeInput.value = "";
        setText(sendStatus, "", true);
        setText(verifyStatus, "", true);
    });

    // 인증메일 발송
    sendBtn.addEventListener("click", async function () {
        const email = (emailInput.value || "").trim();
        if (!isValidEmail(email)) {
            setText(sendStatus, "올바른 이메일을 입력해 주세요.", false);
            return;
        }

        sendBtn.disabled = true;
        setText(sendStatus, "인증메일을 발송 중입니다...", true);

        try {
            const res = await fetch(ctx + "/auth/mail/send", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                },
                body: "email=" + encodeURIComponent(email)
            });

            if (!res.ok) {
                setText(sendStatus, "인증메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.", false);
                return;
            }

            // 서버가 JSON을 주면 읽고 싶으면 아래 주석 해제
            // const data = await res.json();

            setText(sendStatus, "인증메일을 발송했습니다. 메일함에서 인증 코드를 확인해 주세요.", true);
            verifyBox.style.display = "block";
        } catch (e) {
            setText(sendStatus, "네트워크 오류가 발생했습니다.", false);
        } finally {
            sendBtn.disabled = false;
        }
    });

    // 인증 확인
    verifyBtn.addEventListener("click", async function () {
        const email = (emailInput.value || "").trim();
        const code = (codeInput.value || "").trim();

        if (!isValidEmail(email)) {
            setText(verifyStatus, "올바른 이메일을 입력해 주세요.", false);
            return;
        }
        if (!code) {
            setText(verifyStatus, "인증 코드를 입력해 주세요.", false);
            return;
        }

        verifyBtn.disabled = true;
        setText(verifyStatus, "인증 확인 중입니다...", true);

        try {
            const res = await fetch(ctx + "/auth/mail/verify", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                },
                body: "email=" + encodeURIComponent(email) + "&code=" + encodeURIComponent(code)
            });

            if (!res.ok) {
                setText(verifyStatus, "인증에 실패했습니다. 코드를 다시 확인해 주세요.", false);
                emailVerifiedHidden.value = "false";
                return;
            }

            setText(verifyStatus, "이메일 인증이 완료되었습니다.", true);
            emailVerifiedHidden.value = "true";
        } catch (e) {
            setText(verifyStatus, "네트워크 오류가 발생했습니다.", false);
            emailVerifiedHidden.value = "false";
        } finally {
            verifyBtn.disabled = false;
        }
    });

    // 가입하기 눌렀을 때 인증 여부 체크(프론트 1차)
    signupForm.addEventListener("submit", function (e) {
        // oauthSignUp이어도 이메일 인증을 요구한다면 그대로 체크
        if (emailVerifiedHidden.value !== "true") {
            e.preventDefault();
            alert("이메일 인증을 완료해 주세요.");
        }
    });
})();
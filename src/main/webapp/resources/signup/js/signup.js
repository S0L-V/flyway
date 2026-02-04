/* Signup page flow + verification */
/* global showToast, lucide */

(function () {
  const contextPath = window.APP?.contextPath ?? "";
  const body = document.body;
  const isOauth = body?.dataset?.oauthSignup === "true";
  const hasError = body?.dataset?.hasError === "true";

  const steps = [1, 2, 3, 4]
    .map((step) => ({ step, el: document.getElementById(`step-${step}`) }))
    .filter((item) => item.el);

  let currentMethod = null;

  function getStepParam() {
    const params = new URLSearchParams(window.location.search);
    const step = Number(params.get("step"));
    return Number.isFinite(step) ? step : null;
  }

  function setStepParam(step, replace = false) {
    const params = new URLSearchParams(window.location.search);
    params.set("step", String(step));
    const nextUrl = `${window.location.pathname}?${params.toString()}`;
    if (replace) {
      window.history.replaceState({ step }, "", nextUrl);
    } else {
      window.history.pushState({ step }, "", nextUrl);
    }
  }

  function showStep(step) {
    steps.forEach(({ el }) => el.classList.add("hidden"));
    const target = steps.find((item) => item.step === step);
    if (target) {
      target.el.classList.remove("hidden");
    }
  }

  function toggleAllAgreements(source) {
    const checkboxes = document.querySelectorAll(".agree-item");
    checkboxes.forEach((cb) => {
      cb.checked = source.checked;
    });
  }

  function checkAllStatus() {
    const all = document.querySelectorAll(".agree-item");
    const main = document.getElementById("agree-all");
    if (!main) return;
    let checkedCount = 0;
    all.forEach((cb) => {
      if (cb.checked) checkedCount += 1;
    });
    main.checked = checkedCount === all.length;
  }

  function goToStep(step) {
    if (isOauth) {
      step = 3;
    }
    if (step === 2) {
      const required = document.querySelectorAll('.agree-item[data-required="true"]');
      for (const cb of required) {
        if (!cb.checked) {
          if (typeof showToast === "function") {
            showToast("필수 약관에 동의해주세요.", "error");
          } else {
            alert("필수 약관에 동의해주세요.");
          }
          return;
        }
      }
    }
    showStep(step);
    setStepParam(step);
    if (typeof lucide !== "undefined" && typeof lucide.createIcons === "function") {
      lucide.createIcons();
    }
  }

  function selectMethod(method) {
    currentMethod = method;
    if (method === "kakao") {
      if (!isOauth) {
        window.location.href = `${contextPath}/auth/kakao`;
        return;
      }
    }
    goToStep(3);
  }

  function togglePasswordVisibility(id, btn) {
    const input = document.getElementById(id);
    if (!input || !btn) return;
    if (input.type === "password") {
      input.type = "text";
      btn.innerHTML = '<i data-lucide="eye-off" class="w-[18px] h-[18px]"></i>';
    } else {
      input.type = "password";
      btn.innerHTML = '<i data-lucide="eye" class="w-[18px] h-[18px]"></i>';
    }
    if (typeof lucide !== "undefined" && typeof lucide.createIcons === "function") {
      lucide.createIcons({ root: btn });
    }
  }

  function validatePassword(pw) {
    return /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/.test(pw);
  }

  const emailInput = document.getElementById("email");
  const sendBtn = document.getElementById("sendVerifyBtn");
  const sendStatus = document.getElementById("sendStatus");
  const sendErrorStatus = document.getElementById("sendErrorStatus");
  const verifySentBox = document.getElementById("verifySentBox");
  const resendBtn = document.getElementById("resendBtn");
  const verifyBtn = document.getElementById("verifyBtn");
  const verifyStatus = document.getElementById("verifyStatus");
  const verifySuccessBox = document.getElementById("verifySuccessBox");
  const verifyErrorStatus = document.getElementById("verifyErrorStatus");
  const changeEmailBtn = document.getElementById("changeEmailBtn");
  const emailVerifiedHidden = document.getElementById("emailVerified");
  const attemptIdHidden = document.getElementById("attemptId");
  const signupForm = document.getElementById("signupForm");
  const passwordInput = document.getElementById("rawPassword");
  const passwordConfirmInput = document.getElementById("passwordConfirm");
  const passwordMatchStatus = document.getElementById("passwordMatchStatus");
  const passwordRuleStatus = document.getElementById("passwordRuleStatus");
  const nameInput = document.getElementById("name");
  const phoneNumberInput = document.getElementById("phoneNumber");
  const submitBtn = signupForm ? signupForm.querySelector('button[type="submit"]') : null;

    const sendSmsBtn = document.getElementById("sendSmsBtn");
    const resendSmsBtn = document.getElementById("resendSmsBtn");
    const smsStatus = document.getElementById("smsStatus");
    const smsErrorStatus = document.getElementById("smsErrorStatus");
    const smsSentBox = document.getElementById("smsSentBox");
    const smsSuccessBox = document.getElementById("smsSuccessBox");
    const smsCodeInput = document.getElementById("smsCode");
    const verifySmsBtn = document.getElementById("verifySmsBtn");
    const smsVerifyStatus = document.getElementById("smsVerifyStatus");
    const phoneVerifiedHidden = document.getElementById("phoneVerified");
  function setStatus(el, msg, ok) {
    if (!el) return;
    el.textContent = msg || "";
    if (ok === true) {
      el.style.color = "#15803d";
    } else if (ok === false) {
      el.style.color = "#b91c1c";
    } else {
      el.style.color = "";
    }
  }

  function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  function isValidPhone(value) {
    if (!value) return false;
    const digits = String(value).replace(/\D/g, "");
    return digits.length >= 9;
  }

  function updateSubmitState() {
    if (!submitBtn) return;
    const nameOk = Boolean(nameInput?.value?.trim());
    const emailOk = Boolean(emailInput?.value?.trim());
    const phoneOk = isValidPhone(phoneNumberInput?.value?.trim());
    let ok = nameOk && emailOk && phoneOk;

    if (!isOauth) {
      const verifiedOk = emailVerifiedHidden?.value === "true";
      const pw = passwordInput?.value || "";
      const confirm = passwordConfirmInput?.value || "";
        const phoneVerifiedOk = phoneVerifiedHidden?.value === "true";
        ok = ok && verifiedOk && phoneVerifiedOk && validatePassword(pw) && pw === confirm;
    }

    submitBtn.disabled = !ok;
    submitBtn.classList.toggle("opacity-50", !ok);
    submitBtn.classList.toggle("cursor-not-allowed", !ok);
  }

  function resetEmailState() {
    if (emailVerifiedHidden) emailVerifiedHidden.value = "false";
    if (attemptIdHidden) attemptIdHidden.value = "";
    if (verifySentBox) verifySentBox.classList.add("hidden");
    if (verifySuccessBox) verifySuccessBox.classList.add("hidden");
    if (verifyErrorStatus) verifyErrorStatus.classList.add("hidden");
    if (sendErrorStatus) sendErrorStatus.classList.add("hidden");
    setStatus(sendStatus, "", null);
    setStatus(verifyStatus, "", null);
    if (sendBtn) sendBtn.classList.remove("hidden");
    if (changeEmailBtn) changeEmailBtn.classList.add("hidden");
    if (emailInput) emailInput.readOnly = false;
    updateSubmitState();
  }

  if (emailInput && !isOauth) {
    emailInput.addEventListener("input", resetEmailState);
  }

  if (changeEmailBtn && !isOauth) {
    changeEmailBtn.addEventListener("click", function () {
      resetEmailState();
      if (emailInput) emailInput.focus();
    });
  }

  async function handleSendVerification() {
      if (!sendBtn) return;
      if (sendBtn) sendBtn.classList.remove("hidden");
      if (changeEmailBtn) changeEmailBtn.classList.add("hidden");
      const email = (emailInput?.value || "").trim();
      if (!isValidEmail(email)) {
        if (sendErrorStatus) {
          sendErrorStatus.textContent = "올바른 이메일을 입력해 주세요.";
          sendErrorStatus.classList.remove("hidden");
        }
        if (verifySentBox) verifySentBox.classList.add("hidden");
        return;
      }

      const originalLabel = sendBtn.dataset.originalLabel || sendBtn.textContent;
      sendBtn.dataset.originalLabel = originalLabel;
      sendBtn.disabled = true;
      sendBtn.innerHTML = `
        <span class="inline-flex items-center gap-2">
          <svg class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle class="opacity-25" cx="12" cy="12" r="10"></circle>
            <path class="opacity-75" d="M22 12a10 10 0 0 1-10 10"></path>
          </svg>
          전송 중
        </span>
      `;
      setStatus(sendStatus, "인증메일을 발송 중입니다...", null);
      if (sendErrorStatus) sendErrorStatus.classList.add("hidden");
      if (verifySentBox) verifySentBox.classList.add("hidden");

      try {
        const res = await fetch(`${contextPath}/api/auth/email/issue`, {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
          },
          body: `email=${encodeURIComponent(email)}`,
        });

        let data = null;
        try {
          data = await res.json();
          if (attemptIdHidden && data?.data?.attemptId) {
            attemptIdHidden.value = data.data.attemptId;
          }
        } catch (e) {
          const text = await res.text().catch(() => "");
          data = text ? { message: text } : {};
        }

        if (!res.ok) {
          const msg = data?.message || "인증메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.";
          if (sendErrorStatus) {
            sendErrorStatus.textContent = msg;
            sendErrorStatus.classList.remove("hidden");
          }
          if (verifySentBox) verifySentBox.classList.add("hidden");
          return;
        }

        const msg = data?.message || "인증메일을 발송했습니다. 메일함의 링크를 클릭해 주세요.";
        setStatus(sendStatus, msg, null);
        if (verifySentBox) verifySentBox.classList.remove("hidden");
        if (verifySuccessBox) verifySuccessBox.classList.add("hidden");
        if (verifyErrorStatus) verifyErrorStatus.classList.add("hidden");
        if (sendBtn) sendBtn.classList.add("hidden");
        if (changeEmailBtn) changeEmailBtn.classList.remove("hidden");
        if (emailInput) emailInput.readOnly = true;
      } catch (e) {
        if (sendErrorStatus) {
          sendErrorStatus.textContent = "네트워크 오류가 발생했습니다.";
          sendErrorStatus.classList.remove("hidden");
        }
        if (verifySentBox) verifySentBox.classList.add("hidden");
      } finally {
        sendBtn.disabled = false;
        sendBtn.textContent = sendBtn.dataset.originalLabel || "인증메일";
      }
  }

  if (sendBtn && !isOauth) {
    sendBtn.addEventListener("click", handleSendVerification);
  }

  if (resendBtn && !isOauth) {
    resendBtn.addEventListener("click", handleSendVerification);
  }

  if (verifyBtn && !isOauth) {
    verifyBtn.addEventListener("click", async function () {
      const email = (emailInput?.value || "").trim();
      if (!isValidEmail(email)) {
        if (verifyErrorStatus) {
          verifyErrorStatus.textContent = "올바른 이메일을 입력해 주세요.";
          verifyErrorStatus.classList.remove("hidden");
        }
        if (verifySuccessBox) verifySuccessBox.classList.add("hidden");
        return;
      }

      verifyBtn.disabled = true;
      if (verifyErrorStatus) verifyErrorStatus.classList.add("hidden");
      setStatus(verifyStatus, "인증 확인 중입니다...", true);

      try {
        const query = new URLSearchParams({
          email,
          attemptId: attemptIdHidden?.value || "",
        });
        const res = await fetch(`${contextPath}/api/auth/email/status?${query.toString()}`);

        if (!res.ok) {
          if (verifyErrorStatus) {
            verifyErrorStatus.textContent = "인증 확인에 실패했습니다.";
            verifyErrorStatus.classList.remove("hidden");
          }
          if (verifySuccessBox) verifySuccessBox.classList.add("hidden");
          if (emailVerifiedHidden) emailVerifiedHidden.value = "false";
          return;
        }

        const data = await res.json();
        if (data?.success && data?.data === true) {
          setStatus(verifyStatus, "인증되었습니다.", null);
        if (verifySuccessBox) verifySuccessBox.classList.remove("hidden");
        if (verifySentBox) verifySentBox.classList.add("hidden");
        if (verifyErrorStatus) verifyErrorStatus.classList.add("hidden");
        if (emailInput) emailInput.readOnly = true;
        if (emailVerifiedHidden) emailVerifiedHidden.value = "true";
        updateSubmitState();
      } else {
        if (verifyErrorStatus) {
          verifyErrorStatus.textContent = "아직 인증이 완료되지 않았습니다. 메일의 링크를 확인해 주세요.";
          verifyErrorStatus.classList.remove("hidden");
        }
        if (verifySuccessBox) verifySuccessBox.classList.add("hidden");
        if (emailVerifiedHidden) emailVerifiedHidden.value = "false";
        updateSubmitState();
      }
    } catch (e) {
      if (verifyErrorStatus) {
        verifyErrorStatus.textContent = "네트워크 오류가 발생했습니다.";
        verifyErrorStatus.classList.remove("hidden");
      }
      if (verifySuccessBox) verifySuccessBox.classList.add("hidden");
      if (emailVerifiedHidden) emailVerifiedHidden.value = "false";
      updateSubmitState();
    } finally {
        verifyBtn.disabled = false;
      }
    });
  }

  if (signupForm) {
    signupForm.addEventListener("submit", function (e) {
      if (!isOauth && emailVerifiedHidden && emailVerifiedHidden.value !== "true") {
          e.preventDefault();
          if (typeof showToast === "function") {
              showToast("이메일 인증을 완료해 주세요.", "error");
          } else {
              alert("이메일 인증을 완료해 주세요.");
          }
          return;
          // SMS 인증 체크
          if (!isOauth && phoneVerifiedHidden && phoneVerifiedHidden.value !== "true") {
              e.preventDefault();
              if (typeof showToast === "function") {
                  showToast("전화번호 인증을 완료해 주세요.", "error");
              } else {
                  alert("전화번호 인증을 완료해 주세요.");
              }
              return;
          }
      }

      if (!isOauth) {
        const pw = document.getElementById("rawPassword")?.value || "";
        const pwConfirm = document.getElementById("passwordConfirm")?.value || "";
        if (!validatePassword(pw)) {
          e.preventDefault();
          if (typeof showToast === "function") {
            showToast("비밀번호 규칙을 확인해주세요.", "error");
          } else {
            alert("비밀번호 규칙을 확인해주세요.");
          }
          return;
        }
        if (pw !== pwConfirm) {
          e.preventDefault();
          if (typeof showToast === "function") {
            showToast("비밀번호가 일치하지 않습니다.", "error");
          } else {
            alert("비밀번호가 일치하지 않습니다.");
          }
          return;
        }
      }
    });
  }

  function updatePasswordMatchStatus() {
    if (isOauth || !passwordInput || !passwordConfirmInput || !passwordMatchStatus) return;
    const pw = passwordInput.value || "";
    const confirm = passwordConfirmInput.value || "";

    if (!pw && !confirm) {
      passwordMatchStatus.textContent = "";
      passwordMatchStatus.classList.add("hidden");
      return;
    }

    if (confirm.length === 0) {
      passwordMatchStatus.textContent = "";
      passwordMatchStatus.classList.add("hidden");
      return;
    }

    passwordMatchStatus.classList.remove("hidden");
    if (pw === confirm) {
      passwordMatchStatus.textContent = "비밀번호가 일치합니다.";
      passwordMatchStatus.className = "mt-2 text-xs text-green-600";
    } else {
      passwordMatchStatus.textContent = "비밀번호가 일치하지 않습니다.";
      passwordMatchStatus.className = "mt-2 text-xs text-red-600";
    }
  }

  function updatePasswordRuleStatus() {
    if (isOauth || !passwordInput || !passwordRuleStatus) return;
    const pw = passwordInput.value || "";

    if (!pw) {
      passwordRuleStatus.textContent = "";
      passwordRuleStatus.classList.add("hidden");
      return;
    }

    passwordRuleStatus.classList.remove("hidden");
    if (validatePassword(pw)) {
      passwordRuleStatus.textContent = "비밀번호 규칙을 충족합니다.";
      passwordRuleStatus.className = "mt-2 text-xs text-green-600";
    } else {
      passwordRuleStatus.textContent = "비밀번호 규칙을 충족하지 않습니다.";
      passwordRuleStatus.className = "mt-2 text-xs text-red-600";
    }
  }

  if (!isOauth && passwordInput && passwordConfirmInput) {
    passwordInput.addEventListener("input", () => {
      updatePasswordRuleStatus();
      updatePasswordMatchStatus();
      updateSubmitState();
    });
    passwordConfirmInput.addEventListener("input", () => {
      updatePasswordMatchStatus();
      updateSubmitState();
    });
  }

  if (nameInput) nameInput.addEventListener("input", updateSubmitState);
  if (emailInput) emailInput.addEventListener("input", updateSubmitState);
  if (phoneNumberInput) phoneNumberInput.addEventListener("input", updateSubmitState);

    function resetSmsState() {
        if (phoneVerifiedHidden) phoneVerifiedHidden.value = "false";
        if (smsSentBox) smsSentBox.classList.add("hidden");
        if (smsSuccessBox) smsSuccessBox.classList.add("hidden");
        if (smsErrorStatus) smsErrorStatus.classList.add("hidden");
        if (sendSmsBtn) {
            sendSmsBtn.disabled = false;
            sendSmsBtn.classList.remove("hidden");
        }
        if (phoneNumberInput) phoneNumberInput.readOnly = false;
        updateSubmitState();
    }

    if (phoneNumberInput && !isOauth) {
        phoneNumberInput.addEventListener("input", resetSmsState);
    }

    async function handleSendSms() {
        const phone = (phoneNumberInput?.value || "").replace(/\D/g, "");
        if (!isValidPhone(phone)) {
            if (smsErrorStatus) {
                smsErrorStatus.textContent = "올바른 전화번호를 입력해 주세요.";
                smsErrorStatus.classList.remove("hidden");
            }
            return;
        }

        if (sendSmsBtn) sendSmsBtn.disabled = true;
        if (smsErrorStatus) smsErrorStatus.classList.add("hidden");

        try {
            const res = await fetch(`${contextPath}/api/sms/send?phoneNumber=${encodeURIComponent(phone)}`, {
                method: "POST"
            });
            const data = await res.json();

            if (!res.ok) {
                if (smsErrorStatus) {
                    smsErrorStatus.textContent = data?.message || "인증번호 발송에 실패했습니다.";
                    smsErrorStatus.classList.remove("hidden");
                }
                return;
            }

            if (smsStatus) smsStatus.textContent = data?.message || "인증번호가 발송되었습니다.";
            if (smsSentBox) smsSentBox.classList.remove("hidden");
            if (smsSuccessBox) smsSuccessBox.classList.add("hidden");
            if (sendSmsBtn) sendSmsBtn.classList.add("hidden");
            if (smsCodeInput) smsCodeInput.focus();
        } catch (e) {
            if (smsErrorStatus) {
                smsErrorStatus.textContent = "네트워크 오류가 발생했습니다.";
                smsErrorStatus.classList.remove("hidden");
            }
        } finally {
            if (sendSmsBtn) sendSmsBtn.disabled = false;
        }
    }

    if (sendSmsBtn && !isOauth) {
        sendSmsBtn.addEventListener("click", handleSendSms);
    }

    if (resendSmsBtn && !isOauth) {
        resendSmsBtn.addEventListener("click", handleSendSms);
    }

    if (verifySmsBtn && !isOauth) {
        verifySmsBtn.addEventListener("click", async function () {
            const phone = (phoneNumberInput?.value || "").replace(/\D/g, "");
            const code = (smsCodeInput?.value || "").trim();

            if (!code || code.length !== 6) {
                if (smsErrorStatus) {
                    smsErrorStatus.textContent = "6자리 인증번호를 입력해 주세요.";
                    smsErrorStatus.classList.remove("hidden");
                }
                return;
            }

            verifySmsBtn.disabled = true;
            if (smsErrorStatus) smsErrorStatus.classList.add("hidden");

            try {
                const params = new URLSearchParams({ phoneNumber: phone, code: code });
                const res = await fetch(`${contextPath}/api/sms/verify?${params.toString()}`, {
                    method: "POST"
                });
                const data = await res.json();

                if (data?.data === true) {
                    if (smsVerifyStatus) smsVerifyStatus.textContent = "전화번호가 인증되었습니다.";
                    if (smsSuccessBox) smsSuccessBox.classList.remove("hidden");
                    if (smsSentBox) smsSentBox.classList.add("hidden");
                    if (phoneVerifiedHidden) phoneVerifiedHidden.value = "true";
                    if (phoneNumberInput) phoneNumberInput.readOnly = true;
                    if (sendSmsBtn) sendSmsBtn.disabled = true;
                    if (smsCodeInput) smsCodeInput.readOnly = true;
                    updateSubmitState();
                } else {
                    if (smsErrorStatus) {
                        smsErrorStatus.textContent = data?.message || "인증번호가 일치하지 않습니다.";
                        smsErrorStatus.classList.remove("hidden");
                    }
                    if (phoneVerifiedHidden) phoneVerifiedHidden.value = "false";
                }
            } catch (e) {
                if (smsErrorStatus) {
                    smsErrorStatus.textContent = "네트워크 오류가 발생했습니다.";
                    smsErrorStatus.classList.remove("hidden");
                }
                if (phoneVerifiedHidden) phoneVerifiedHidden.value = "false";
            } finally {
                verifySmsBtn.disabled = false;
            }
        });
    }

    window.goToStep = goToStep;
  window.toggleAllAgreements = toggleAllAgreements;
  window.checkAllStatus = checkAllStatus;
  window.selectMethod = selectMethod;
  window.togglePasswordVisibility = togglePasswordVisibility;

  if (typeof lucide !== "undefined" && typeof lucide.createIcons === "function") {
    lucide.createIcons();
  }

  if (isOauth || hasError) {
    currentMethod = isOauth ? "kakao" : "email";
    showStep(3);
    setStepParam(3, true);
  } else {
    const paramStep = getStepParam();
    if (paramStep && steps.some((item) => item.step === paramStep)) {
      showStep(paramStep);
    } else {
      showStep(1);
      setStepParam(1, true);
    }
  }
  updateSubmitState();

  window.addEventListener("popstate", () => {
    const paramStep = getStepParam();
    if (paramStep && steps.some((item) => item.step === paramStep)) {
      showStep(paramStep);
    }
  });
})();

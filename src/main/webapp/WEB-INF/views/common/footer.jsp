<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>

<footer class="bg-white/50 backdrop-blur-sm border-t border-blue-100 py-8 mt-20">
    <div class="max-w-7xl mx-auto px-10">
        <div class="flex flex-col md:flex-row items-center justify-between gap-4">
            <!-- Logo -->
            <a href="${pageContext.request.contextPath}/" class="flex items-center hover:opacity-80 transition-opacity">
                <img src="${pageContext.request.contextPath}/resources/common/img/logo.svg" alt="flyway" class="h-7 opacity-60" />
            </a>

            <!-- Links -->
            <div class="flex items-center gap-6 text-sm text-gray-500">
                <a href="https://github.com/S0L-V/flyway" target="_blank"
                   class="inline-flex items-center justify-center w-9 h-9 rounded-full border border-slate-200 bg-white/70 text-slate-500
                          no-underline hover:text-[#2F93F7] hover:border-blue-200 transition-colors">
                    <i class="fa-brands fa-github text-lg"></i>
                </a>
                <button onclick="copyEmail()"
                        class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-slate-200 bg-white/70 text-slate-500
                               hover:text-[#2F93F7] hover:border-blue-200 transition-colors cursor-pointer"
                        title="클릭하여 이메일 복사">
                    <i class="fa-regular fa-envelope text-sm"></i>
                    <span id="emailText">solv.developers@gmail.com</span>
                    <i class="fa-regular fa-copy text-xs opacity-50"></i>
                </button>
                <button onclick="openPolicyModal()"
                        class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-slate-200 bg-white/70 text-slate-500
                               hover:text-[#2F93F7] hover:border-blue-200 transition-colors cursor-pointer">
                    <i class="fa-solid fa-scale-balanced text-sm"></i>
                    <span>가격정책</span>
                </button>
            </div>

            <!-- Copyright -->
            <p class="text-xs text-gray-400">
                &copy; 2026 flyway. All rights reserved.
            </p>
        </div>
    </div>
</footer>

<!-- Policy Modal -->
<div id="policyModal" class="policy-modal-overlay">
    <div class="policy-modal-container">
        <div class="policy-modal-header">
            <h3 class="policy-modal-title">가격 정책 안내</h3>
            <button onclick="closePolicyModal()" class="policy-modal-close">
                <i class="fa-solid fa-xmark"></i>
            </button>
        </div>
        <div class="policy-modal-body">
            <jsp:include page="/WEB-INF/views/policy/policy.jsp" />
        </div>
    </div>
</div>

<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/policy/css/policy.css">

<script>
    function copyEmail() {
        const email = 'solv.developers@gmail.com';
        navigator.clipboard.writeText(email).then(() => {
            const emailText = document.getElementById('emailText');
            const original = emailText.textContent;
            emailText.textContent = '복사됨!';
            setTimeout(() => {
                emailText.textContent = original;
            }, 1500);
        });
    }

    function openPolicyModal() {
        document.getElementById('policyModal').classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closePolicyModal() {
        document.getElementById('policyModal').classList.remove('active');
        document.body.style.overflow = '';
    }

    // Close modal when clicking outside
    document.getElementById('policyModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closePolicyModal();
        }
    });

    // Close modal on escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && document.getElementById('policyModal').classList.contains('active')) {
            closePolicyModal();
        }
    });
</script>

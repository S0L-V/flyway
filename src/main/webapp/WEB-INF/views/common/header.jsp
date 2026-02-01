<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Vanilla Tilt -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/vanilla-tilt/1.8.0/vanilla-tilt.min.js"></script>

<header class="glass-header py-4" id="mainHeader">
  <!-- Glass Background -->
  <div class="glass-header-bg"></div>

  <!-- Inner highlight -->
  <div class="absolute inset-[1px] rounded-[39px] bg-gradient-to-b from-white/15 via-transparent to-transparent pointer-events-none"></div>

  <div class="px-10 flex items-center justify-between relative z-10">
    <!-- Logo -->
    <a href="${pageContext.request.contextPath}/" class="logo-3d flex items-center">
      <img src="${pageContext.request.contextPath}/resources/common/img/logo.svg" alt="flyway" class="h-10 logo-text-3d" />
    </a>

    <!-- User Buttons -->
    <div class="flex items-center space-x-3">
      <sec:authorize access="isAuthenticated()">
        <a href="${pageContext.request.contextPath}/mypage"
           class="tilt-btn btn-text auth-text-btn px-3 py-2 rounded-full
                          no-underline transition-all duration-300 active:scale-95"
           data-tilt data-tilt-glare data-tilt-max-glare="0.3"
           data-tilt-scale="1.02" data-tilt-max="8" data-tilt-speed="400">
          마이페이지
        </a>
        <form action="${pageContext.request.contextPath}/auth/logout" method="post" class="inline">
          <button type="submit"
                  class="tilt-btn btn-text auth-text-btn px-3 py-2 rounded-full
                                   transition-all duration-300 active:scale-95"
                  data-tilt data-tilt-glare data-tilt-max-glare="0.4"
                  data-tilt-scale="1.02" data-tilt-max="8" data-tilt-speed="400">
            로그아웃
          </button>
        </form>
      </sec:authorize>

      <sec:authorize access="isAnonymous()">
        <a href="${pageContext.request.contextPath}/login"
           class="tilt-btn btn-text auth-text-btn px-3 py-2 rounded-full
                          no-underline transition-all duration-300 active:scale-95"
           data-tilt data-tilt-glare data-tilt-max-glare="0.3"
           data-tilt-scale="1.02" data-tilt-max="8" data-tilt-speed="400">
          로그인
        </a>
        <a href="${pageContext.request.contextPath}/signup"
           class="tilt-btn btn-text auth-text-btn px-3 py-2 rounded-full
                          no-underline transition-all duration-300 active:scale-95"
           data-tilt data-tilt-glare data-tilt-max-glare="0.4"
           data-tilt-scale="1.02" data-tilt-max="8" data-tilt-speed="400">
          회원가입
        </a>
      </sec:authorize>
    </div>

    <!-- Mobile Menu Button -->
    <button class="lg:hidden text-slate-700 p-2" id="mobileMenuBtn">
      <i class="fa-solid fa-bars text-xl"></i>
    </button>
  </div>
</header>

<!-- Header Spacer (not needed on hero pages) -->
<div class="header-spacer h-24"></div>

<!-- Scroll Effect + Vanilla Tilt Init -->
<script>
  (function() {
    const header = document.getElementById('mainHeader');

    function handleScroll() {
      if (window.scrollY > 20) {
        document.body.classList.add('scrolled');
        header.classList.remove('py-4');
        header.classList.add('py-3');
      } else {
        document.body.classList.remove('scrolled');
        header.classList.remove('py-3');
        header.classList.add('py-4');
      }
    }

    window.addEventListener('scroll', handleScroll);
    handleScroll();

    // Vanilla Tilt 초기화 (버튼)
    if (typeof VanillaTilt !== 'undefined') {
      VanillaTilt.init(document.querySelectorAll(".tilt-btn"), {
        max: 8,
        speed: 400,
        scale: 1.02,
        glare: true,
        "max-glare": 0.3
      });
    }
  })();
</script>

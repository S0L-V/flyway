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
    <div class="flex items-center space-x-1 sm:space-x-2 md:space-x-3">
      <sec:authorize access="isAuthenticated()">
        <!-- User Profile Dropdown -->
        <div class="user-dropdown-wrapper">
          <button type="button" class="user-dropdown-trigger">
            <span class="user-avatar">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
              </svg>
            </span>
            <span class="user-email-short"><sec:authentication property="principal.user.email"/></span>
            <svg class="dropdown-arrow" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd"/>
            </svg>
          </button>

          <!-- Dropdown Menu -->
          <div class="user-dropdown-menu">
            <div class="dropdown-user-info">
              <div class="dropdown-avatar">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
                </svg>
              </div>
              <div class="dropdown-email"><sec:authentication property="principal.user.email"/></div>
            </div>
            <div class="dropdown-divider"></div>
            <a href="${pageContext.request.contextPath}/mypage" class="dropdown-item">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
              </svg>
              마이페이지
            </a>
            <form action="${pageContext.request.contextPath}/auth/logout" method="post" class="dropdown-form">
              <sec:csrfInput/>
              <button type="submit" class="dropdown-item dropdown-logout">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
                </svg>
                로그아웃
              </button>
            </form>
          </div>
        </div>
      </sec:authorize>

      <sec:authorize access="isAnonymous()">
        <a href="${pageContext.request.contextPath}/login"
           class="tilt-btn btn-text auth-text-btn px-2 md:px-3 py-2 rounded-full
                          no-underline transition-all duration-300 active:scale-95 whitespace-nowrap"
           data-tilt data-tilt-glare data-tilt-max-glare="0.3"
           data-tilt-scale="1.02" data-tilt-max="8" data-tilt-speed="400">
          로그인
        </a>
        <a href="${pageContext.request.contextPath}/signup"
           class="tilt-btn btn-text auth-text-btn px-2 md:px-3 py-2 rounded-full
                          no-underline transition-all duration-300 active:scale-95 whitespace-nowrap"
           data-tilt data-tilt-glare data-tilt-max-glare="0.4"
           data-tilt-scale="1.02" data-tilt-max="8" data-tilt-speed="400">
          회원가입
        </a>
      </sec:authorize>
    </div>
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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<header class="header">
  <div class="header__content">
    <div class="header__logo">
      <a href="${pageContext.request.contextPath}/">
        <img src="${pageContext.request.contextPath}/resources/common/img/logo.svg" alt="Flyway" />
      </a>
    </div>

    <nav class="header__menu">
      <sec:authorize access="isAnonymous()">
        <a href="${pageContext.request.contextPath}/login" class="header__menu-item">로그인</a>
        <a href="${pageContext.request.contextPath}/signup" class="header__menu-item">회원가입</a>
      </sec:authorize>

      <sec:authorize access="isAuthenticated()">
        <a href="${pageContext.request.contextPath}/mypage" class="header__menu-item">마이페이지</a>
        <form class="header__menu-form"
              action="${pageContext.request.contextPath}/auth/logout"
              method="post">
          <button type="submit"
                  class="header__menu-item header__menu-item--logout">로그아웃</button>
        </form>
      </sec:authorize>
    </nav>
  </div>
</header>
<div class="header__margin"></div>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://cdn.tailwindcss.com"></script>
<script>
  tailwind.config = {
    corePlugins: {
      preflight: false,
    }
  };
</script>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/layout.css?v=<%= System.currentTimeMillis() %>">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/header-hero.css?v=<%= System.currentTimeMillis() %>">

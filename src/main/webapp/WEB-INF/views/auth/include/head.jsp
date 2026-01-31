<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://cdn.tailwindcss.com"></script>
<script>
  tailwind.config = {
    theme: {
      extend: {
        colors: {
          primary: {
            50: '#eff6ff', 100: '#dbeafe', 200: '#bfdbfe', 300: '#93c5fd',
            400: '#60a5fa', 500: '#3b82f6', 600: '#2563eb', 700: '#1d4ed8',
            800: '#1e40af', 900: '#1e3a8a',
          }
        },
        fontFamily: {
          sans: ['Pretendard', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'Helvetica Neue', 'Arial', 'sans-serif'],
        }
      }
    }
  }
</script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/layout.css?v=<%= System.currentTimeMillis() %>">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/mypage/css/styles.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/custom-page-header.css">
<link href="https://cdnjs.cloudflare.com/ajax/libs/pretendard/1.3.9/static/pretendard.css" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css">
<script src="https://unpkg.com/lucide@latest"></script>

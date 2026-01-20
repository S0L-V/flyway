<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title><c:out value="${title}"/></title>
    <style>
        body {
            margin: 0;
            padding: 0;
            background: #f4f6f9;
            font-family: Arial, sans-serif;
        }
        .wrap {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .card {
            background: #fff;
            padding: 28px 32px;
            border-radius: 12px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.08);
            text-align: center;
        }
        .card h1 {
            margin: 0 0 12px;
            font-size: 20px;
            color: #222;
        }
        .status {
            margin: 0 0 10px;
            font-weight: 600;
        }
        .status.ok {
            color: #1a7f37;
        }
        .status.fail {
            color: #d1242f;
        }
        .hint {
            margin: 0;
            color: #555;
            font-size: 14px;
        }
    </style>
</head>
<body>
<div class="wrap">
    <div class="card">
        <h1><c:out value="${title}"/></h1>
        <p class="status <c:out value='${success ? "ok" : "fail"}'/>">
            <c:out value="${statusMessage}"/>
        </p>
        <p class="hint"><c:out value="${hintMessage}"/></p>
    </div>
</div>
</body>
</html>

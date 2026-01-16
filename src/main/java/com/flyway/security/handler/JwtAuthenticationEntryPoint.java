package com.flyway.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<?> body = ApiResponse.error(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}


package com.flyway.template.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.RequestDispatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ErrorControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ErrorController()).build();
    }

    @Test
    @DisplayName("GET /error 요청에 403 상태가 전달되면 403 에러 페이지를 렌더링한다")
    void getError_with403_rendersForbiddenPage() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }

    @Test
    @DisplayName("POST /error 요청에 403 상태가 전달되어도 403 에러 페이지를 렌더링한다")
    void postError_with403_rendersForbiddenPage() throws Exception {
        mockMvc.perform(post("/error")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }
}

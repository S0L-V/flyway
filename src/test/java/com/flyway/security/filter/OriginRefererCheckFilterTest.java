package com.flyway.security.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OriginRefererCheckFilterTest {

    @Test
    @DisplayName("API POST + 허용 Origin이면 통과한다")
    void apiPost_withAllowedOrigin_passes() throws Exception {
        OriginRefererCheckFilter filter = apiFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = apiRequest("POST", "/api/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Origin", requestOrigin(request));

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
    }

    @Test
    @DisplayName("API POST + 비허용 Origin이면 403 차단한다")
    void apiPost_withDisallowedOrigin_blocks() throws Exception {
        OriginRefererCheckFilter filter = apiFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = apiRequest("POST", "/api/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Origin", "https://evil.com");

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    @DisplayName("API POST + Origin 없고 허용 Referer면 통과한다")
    void apiPost_withAllowedReferer_passes() throws Exception {
        OriginRefererCheckFilter filter = apiFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = apiRequest("POST", "/api/payments/confirm");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Referer", requestAppBase(request) + "/reservations/abc/booking");

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
    }

    @Test
    @DisplayName("API POST + Origin/Referer 모두 없으면 403 차단한다")
    void apiPost_withoutOriginAndReferer_blocks() throws Exception {
        OriginRefererCheckFilter filter = apiFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = apiRequest("POST", "/api/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    @DisplayName("OPTIONS 요청은 무조건 통과한다")
    void optionsRequest_alwaysPasses() throws Exception {
        OriginRefererCheckFilter filter = apiFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = apiRequest("OPTIONS", "/api/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Origin", "https://evil.com");

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
    }

    @Test
    @DisplayName("Web 필터는 비대상 경로에서는 동작하지 않는다")
    void webFilter_nonTargetPath_isSkipped() throws Exception {
        OriginRefererCheckFilter filter = webFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = apiRequest("POST", "/search/flights");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Origin", "https://evil.com");

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
    }

    @Test
    @DisplayName("Web 필터는 대상 경로의 비허용 Origin을 403 차단한다")
    void webFilter_targetPath_disallowedOrigin_blocks() throws Exception {
        OriginRefererCheckFilter filter = webFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = apiRequest("POST", "/reservations/abc/passengers/api");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Origin", "https://evil.com");

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    @DisplayName("Web 필터는 예외 경로(/auth/**)는 검사하지 않는다")
    void webFilter_excludedPath_isSkipped() throws Exception {
        OriginRefererCheckFilter filter = webFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = apiRequest("POST", "/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Origin", "https://evil.com");

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
    }

    @Test
    @DisplayName("contextPath가 있어도 경로 판별이 정상 동작한다")
    void resolvesPathWithContextPath() throws Exception {
        OriginRefererCheckFilter filter = apiFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest request = apiRequest("POST", "/flyway/api/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContextPath("/flyway");
        request.addHeader("Origin", requestOrigin(request));

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
    }

    private MockHttpServletRequest apiRequest(String method, String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, requestUri);
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        return request;
    }

    private String requestOrigin(MockHttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    private String requestAppBase(MockHttpServletRequest request) {
        return requestOrigin(request) + request.getContextPath();
    }

    private OriginRefererCheckFilter apiFilter() {
        return OriginRefererCheckFilter.forApi(defaultAllowedOrigins());
    }

    private OriginRefererCheckFilter webFilter() {
        return OriginRefererCheckFilter.forWeb(defaultAllowedOrigins());
    }

    private List<String> defaultAllowedOrigins() {
        return Arrays.asList("https://flyway.kr", "http://localhost:8080");
    }
}

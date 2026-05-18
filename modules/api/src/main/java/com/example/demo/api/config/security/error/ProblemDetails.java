package com.example.demo.api.config.security.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

import java.io.IOException;
import java.net.URI;

/**
 * 필터 단계에서 사용하는 RFC 7807 응답 헬퍼.
 * MVC 진입 전 발생하는 인증/레이트리밋 에러를 일관된 ProblemDetail JSON으로 직렬화한다.
 */
public final class ProblemDetails {

    public static final URI TYPE_BLANK = URI.create("about:blank");

    private ProblemDetails() {}

    public static void write(HttpServletResponse response,
                             ObjectMapper mapper,
                             HttpStatus status,
                             String title,
                             String detail) throws IOException {
        write(response, mapper, status, title, detail, null, -1L);
    }

    public static void write(HttpServletResponse response,
                             ObjectMapper mapper,
                             HttpStatus status,
                             String title,
                             String detail,
                             String code,
                             long retryAfterSeconds) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(TYPE_BLANK);
        problem.setTitle(title);
        if (detail != null) {
            problem.setDetail(detail);
        }
        if (code != null) {
            problem.setProperty("code", code);
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        if (retryAfterSeconds >= 0) {
            response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
            problem.setProperty("retryAfterSeconds", retryAfterSeconds);
        }
        mapper.writeValue(response.getOutputStream(), problem);
    }
}

package faang.school.postservice.config.context;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserHeaderFilter implements Filter {

    private final UserContext userContext;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String userIdHeader = httpRequest.getHeader("X-User-Id");

        if (userIdHeader != null) {
            try {
                long userId = Long.parseLong(userIdHeader);
                userContext.setUserId(userId);
                log.info("User ID set to: {}", userId);
            } catch (NumberFormatException e) {
                log.error("Invalid X-User-Id header value: {}", userIdHeader, e);
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-User-Id header");
                return;
            }
        } else {
            log.debug("No X-User-Id header found, skipping");
        }

        try {
            chain.doFilter(request, response);
        } finally {
            userContext.clear();
            log.debug("UserContext cleared after request");
        }
    }
}
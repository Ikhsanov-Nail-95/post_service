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
public class ProjectHeaderFilter implements Filter {

    private final ProjectContext projectContext;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String projectIdHeader = httpRequest.getHeader("X-Project-Id");

        if (projectIdHeader != null) {
            try {
                long projectId = Long.parseLong(projectIdHeader);
                projectContext.setProjectId(projectId);
                log.info("Project ID set to: {}", projectId);
            } catch (NumberFormatException e) {
                log.error("Invalid X-Project-Id header value: {}", projectIdHeader, e);
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid X-Project-Id format");
                return;
            }
        } else {
            log.debug("No X-Project-Id header found, skipping");
        }

        try {
            chain.doFilter(request, response);
        } finally {
            projectContext.clear();
            log.debug("ProjectContext cleared after request");
        }
    }
}
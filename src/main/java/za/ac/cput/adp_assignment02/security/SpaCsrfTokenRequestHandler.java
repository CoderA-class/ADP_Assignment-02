package za.ac.cput.adp_assignment02.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/*
 * SpaCsrfTokenRequestHandler.java
 * CSRF protection for a single-page app (from the Spring Security reference docs).
 * The token is stored in the XSRF-TOKEN cookie; the React app sends it back in the
 * X-XSRF-TOKEN header on every POST/PUT/DELETE.
 */
public final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        // Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of the token value
        this.delegate.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        // A header means the SPA sent the raw cookie value, so read it as-is
        if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
            return super.resolveCsrfTokenValue(request, csrfToken);
        }
        // Otherwise (e.g. a normal HTML form) use the masked value
        return this.delegate.resolveCsrfTokenValue(request, csrfToken);
    }
}

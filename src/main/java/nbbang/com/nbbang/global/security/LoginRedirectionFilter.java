package nbbang.com.nbbang.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static nbbang.com.nbbang.global.security.SecurityPolicy.TOKEN_COOKIE_KEY;
import static nbbang.com.nbbang.global.security.SecurityPolicy.TOKEN_EXPIRE_TIME;

@Component
@RequiredArgsConstructor
public class LoginRedirectionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        String redirect_uri = request.getParameter("redirect_uri");
        if (redirect_uri != null) {
            //CookieUtils.addCookie(response, "redirect_uri", redirect_uri, 180);
            addRedirectUriCookie(response, redirect_uri);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        if (path.startsWith("/oauth2") || path.startsWith("/login")) {
            return false;
        }
        return true;
    }

    private void addRedirectUriCookie(HttpServletResponse response, String value) {
        CookieUtils.addResponseCookie(response, "redirect_uri", value, true, true, 180, "none", "", "/");
    }
}

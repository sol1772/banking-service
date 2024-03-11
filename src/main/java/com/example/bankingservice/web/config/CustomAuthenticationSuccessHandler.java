package com.example.bankingservice.web.config;

import com.example.bankingservice.model.User;
import com.example.bankingservice.service.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication auth)
            throws IOException {
        handle(req, resp, auth);
        clearAuthenticationAttributes(req);
    }

    protected void handle(HttpServletRequest req, HttpServletResponse resp, Authentication auth) throws IOException {
        String targetUrl = "";
        if (auth != null && logger.isInfoEnabled()) {
            User user = auth.getPrincipal() == null ? null : ((UserDetailsImpl) auth.getPrincipal()).getUser();
            if (!Objects.isNull(user)) {
                HttpSession session = req.getSession();
                session.setAttribute("user", user);
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                targetUrl = "/user/" + user.getId();
                if (logger.isInfoEnabled()) {
                    String refererUrl = req.getHeader("Referer") + targetUrl;
                    logger.info("User {} logged in to {}", user, refererUrl);
                }
            } else {
                targetUrl = "/login";
            }
        }

        if (resp.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }
        redirectStrategy.sendRedirect(req, resp, targetUrl);
    }

    protected void clearAuthenticationAttributes(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}

package com.sss.app.jwtToken;

import com.sss.app.entity.UserSession;
import com.sss.app.entity.roles.Role;
import com.sss.app.entity.users.User;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.UserSessionRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter implements Filter {

    private static final String BEARER_PREFIX = "Bearer ";

    // Public per Section 3 of the kickoff spec: invite-based auth means only
    // the login/reset/signup entry points are reachable without a token.
    // Everything else — including the dev password-hash generator and the
    // invite-creation endpoint (an admin action) — requires authentication.
    private static final List<String> PUBLIC_PATHS = List.of(
            "/sss/api/login/user",
            "/sss/api/login/forgot-password",
            "/sss/api/login/reset-password",
            "/sss/api/signup",
            // So a genuine server error renders as its real status instead of being masked
            // by this filter rejecting the container's internal /error forward.
            "/sss/error",
            // Uploaded images (library items, org logos) are read back via plain
            // <img src> requests, which carry no Authorization header.
            "/sss/files/",
            // Meta (Facebook/Instagram) Lead Ads webhook — secured by real
            // HMAC signature verification (see MetaSignatureVerifier), not a
            // JWT. Kept in sync with SecurityConfig's requestMatchers().
            "/sss/api/integrations/meta/webhook"
    );

    // The generic-channel webhook lead-intake endpoint (WebhookLeadController's
    // /{orgUid}/leads) needs its own check rather than a plain prefix in
    // PUBLIC_PATHS above — IntegrationConnectionController's authenticated
    // /webhook/connect and /webhook/disconnect admin actions share the same
    // "/sss/api/integrations/webhook/" prefix, and a bare startsWith() let
    // those bypass auth entirely (found 2026-08-21). Kept in sync with
    // SecurityConfig's requestMatchers().
    private static final String WEBHOOK_LEAD_INTAKE_PREFIX = "/sss/api/integrations/webhook/";
    private static final String WEBHOOK_LEAD_INTAKE_SUFFIX = "/leads";

    @Autowired
    private UserSessionRepository userSessionRepo;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        if (isPublicEndpoint(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            sendErrorResponse(httpResponse, HttpStatus.UNAUTHORIZED, "Missing or malformed Authorization header");
            return;
        }
        String token = authHeader.substring(BEARER_PREFIX.length());

        // Only JWT/session/user resolution is guarded — chain.doFilter() must run
        // outside this try, otherwise an exception from downstream (the controller,
        // or an outer filter's post-processing after the response is already
        // written) gets misreported as "invalid JWT" and this filter then tries to
        // write a second response onto an already-committed one.
        try {
            String username = JwtValidator.extractUsername(token);
            String sessionIdStr = JwtValidator.extractSessionId(token);

            // A user can hold multiple concurrent sessions (one per device)
            // now, so "is this token valid" is resolved per-session-id, not
            // by username alone — a token with no/invalid sessionId claim
            // (e.g. issued before this feature existed) can't be matched to
            // any row and is rejected, same as an unknown session.
            UUID sessionId = parseSessionId(sessionIdStr);
            Optional<UserSession> session = sessionId == null ? Optional.empty() : userSessionRepo.findById(sessionId);
            if (session.isEmpty() || !session.get().isActive() || !token.equals(session.get().getJwtToken())) {
                sendErrorResponse(httpResponse, HttpStatus.UNAUTHORIZED, "Invalid or expired session");
                return;
            }
            // lastAccessed isn't read anywhere today, but keep it roughly fresh in
            // case something needs it later — throttled so this doesn't cost a
            // write on every single request (was previously unconditional).
            LocalDateTime now = LocalDateTime.now();
            UserSession userSession = session.get();
            if (userSession.getLastAccessed() == null || userSession.getLastAccessed().isBefore(now.minusMinutes(5))) {
                userSession.setLastAccessed(now);
                userSessionRepo.save(userSession);
            }

            User user = userRepository.findByEmailWithRoles(username).orElse(null);
            if (user == null) {
                sendErrorResponse(httpResponse, HttpStatus.UNAUTHORIZED, "Unknown user");
                return;
            }
            if (Boolean.TRUE.equals(user.getBlocked())) {
                // Blocked means logged out everywhere, not just this one
                // device/session.
                userSessionRepo.revokeAllActiveForUser(user.getSeqp(), now);
                sendErrorResponse(httpResponse, HttpStatus.FORBIDDEN, "Your account has been blocked. Please contact your organization administrator.");
                return;
            }

            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(link -> link.getRole())
                    .filter(Objects::nonNull)
                    .map(Role::getName)
                    .filter(Objects::nonNull)
                    .map(name -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + name.toUpperCase()))
                    .collect(Collectors.toList());

            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities));
        } catch (Exception e) {
            sendErrorResponse(httpResponse, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        chain.doFilter(request, response);
    }

    private UUID parseSessionId(String sessionIdStr) {
        if (!StringUtils.hasText(sessionIdStr)) {
            return null;
        }
        try {
            return UUID.fromString(sessionIdStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        JSONObject content = new JSONObject();
        content.put("status", status.value());
        content.put("name", status.name());
        content.put("message", message);
        response.getWriter().write(content.toString());
        response.getWriter().flush();
    }

    private boolean isPublicEndpoint(String uri) {
        if (uri.startsWith(WEBHOOK_LEAD_INTAKE_PREFIX)) {
            return uri.endsWith(WEBHOOK_LEAD_INTAKE_SUFFIX);
        }
        return PUBLIC_PATHS.stream().anyMatch(uri::startsWith);
    }
}

package com.sss.app.jwtToken;

import com.sss.app.entity.UserSession;
import com.sss.app.repository.UserSessionRepository;
import com.sss.app.service.AuthenticationService;
import jakarta.servlet.*;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
@Component
public class JwtAuthenticationFilter implements Filter {
    @Autowired
    private UserSessionRepository userSessionRepo;
    @Autowired
    AuthenticationService authServices;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws RuntimeException, ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        System.out.println("DoFilter Start===");
        String requestURI = httpRequest.getRequestURI();
        String authHeader = httpRequest.getHeader("Authorization");
        System.out.println("DoFilter Start requestURI ===" + requestURI);

        if (isPublicEndpoint(requestURI)) {
            System.out.println("DoFilter Start If===");
            chain.doFilter(request, response);
            return;
        }

        if (requestURI.startsWith("/sss/api/login/user")) {
            chain.doFilter(request, response);
            return;
        }
        else if(!requestURI.startsWith("/sss/api/login/forgot-password") &&
                !requestURI.startsWith("/sss/api/login/reset-password")){
            try {
                JwtValidator.validateToken(authHeader);

                String username = JwtValidator.extractUsername(authHeader);
                System.out.println("Authenticated user: " + username);

                //Check if this token exists in DB
                Optional<UserSession> session = userSessionRepo.findById(username);
                if (session.isPresent() && session.get().getJwtToken().equals(authHeader)) {
                    // valid session — optionally update lastAccessed
                    session.get().setLastAccessed(LocalDateTime.now());
                    userSessionRepo.save(session.get());

                    // Optionally, authenticate user in Spring Security
                } else {
                    sendErrorResponse(httpServletResponse, HttpStatus.UNAUTHORIZED, "Invalid or expired session");
                    //Invalid or expired session — reject
                    return;
                }
                chain.doFilter(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(httpServletResponse, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
            }
        } else {
            chain.doFilter(request, response);
            return;
        }
    }
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        //To-Do :: update error response content
        JSONObject content = new JSONObject();
        content.put("status", status.value());
        content.put("name", status.name());
        content.put("message", message);
        response.getWriter().write(content.toString());
        response.getWriter().flush();
    }
    private boolean isPublicEndpoint(String uri) {
        return uri.startsWith("/sss/api/login/hello")
                || uri.startsWith("/sss/api/login/forgot-password")
                || uri.startsWith("/sss/newuser/invite")
                || uri.startsWith("/sss/newuser/redirect")
                || uri.startsWith("/sss/api/login/reset-password");
    }
}


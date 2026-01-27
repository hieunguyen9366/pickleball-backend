package com.pickleball.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Skip JWT filter for auth endpoints (login, register, etc.)
        String path = request.getRequestURI();
        if (path != null && (path.startsWith("/api/auth/") || path.startsWith("/api/v1/auth/"))) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            userEmail = jwtUtil.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Token không hợp lệ (có thể đã hết hạn)
                    handleJwtError(response, "Token is invalid or expired", HttpStatus.UNAUTHORIZED);
                    return;
                }
            }
        } catch (ExpiredJwtException ex) {
            // Token đã hết hạn
            handleJwtError(response, "Token has expired", HttpStatus.UNAUTHORIZED);
            return;
        } catch (MalformedJwtException | SignatureException ex) {
            // Token không đúng định dạng hoặc signature không hợp lệ
            handleJwtError(response, "Invalid token format", HttpStatus.UNAUTHORIZED);
            return;
        } catch (JwtException ex) {
            // Các lỗi JWT khác
            handleJwtError(response, "Invalid token: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
            return;
        } catch (Exception ex) {
            // Các lỗi khác (ví dụ: user không tồn tại)
            handleJwtError(response, "Authentication failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleJwtError(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Void> errorResponse = ApiResponse.error(message, status.value());
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}

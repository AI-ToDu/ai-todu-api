package com.aitodu.platform.iam.infrastructure.security;

import com.aitodu.platform.shared.domain.DomainViolation;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
  @Bean
  org.springframework.security.core.userdetails.UserDetailsService noPasswordLogin() {
    return username -> {
      throw new org.springframework.security.core.userdetails.UsernameNotFoundException(username);
    };
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http, SessionService sessions, @Value("${app.cors.origins}") String origins)
      throws Exception {
    http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .cors(
            c ->
                c.configurationSource(
                    request -> {
                      var config = new CorsConfiguration();
                      config.setAllowedOrigins(Arrays.asList(origins.split(",")));
                      config.setAllowedMethods(
                          List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
                      config.setAllowedHeaders(
                          List.of(
                              "Authorization",
                              "Content-Type",
                              "Accept-Language",
                              "Idempotency-Key"));
                      return config;
                    }))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers(
                        org.springframework.http.HttpMethod.POST,
                        "/api/v1/auth/login",
                        "/api/v1/auth/register")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint((q, r, x) -> error(r, 401, "UNAUTHENTICATED"))
                    .accessDeniedHandler((q, r, x) -> error(r, 403, "FORBIDDEN")));
    http.addFilterBefore(
        new OncePerRequestFilter() {
          protected void doFilterInternal(
              HttpServletRequest q, HttpServletResponse r, FilterChain chain)
              throws ServletException, IOException {
            String header = q.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
              try {
                var principal = sessions.authenticate(header.substring(7));
                SecurityContextHolder.getContext()
                    .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()))));
              } catch (DomainViolation ex) {
                error(r, 401, "UNAUTHENTICATED");
                return;
              }
            }
            chain.doFilter(q, r);
          }
        },
        UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  private static void error(HttpServletResponse r, int status, String code) throws IOException {
    r.setStatus(status);
    r.setContentType("application/problem+json");
    r.getWriter()
        .write(
            "{\"status\":"
                + status
                + ",\"code\":\""
                + code
                + "\",\"detail\":\""
                + (status == 401 ? "Authentication required." : "Access denied.")
                + "\"}");
  }
}

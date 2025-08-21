package org.example.jaego.Security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/api/inventories/category/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()  // 사용자 API 허용
                        .requestMatchers("/api/chatrooms/**").permitAll()  // 채팅방 API 허용
                        .requestMatchers("/api/chatrooms?**").permitAll()
                        .requestMatchers("/api/chatrooms").permitAll()
                        .requestMatchers("/ws/", "/ws", "/sockjs/", "/websocket/", "/info/","/ws/**").permitAll()  // WebSocket 허용
                        .requestMatchers("/", "/health").permitAll()  // 루트, 헬스체크 허용
                        .anyRequest().permitAll()  // 나머지도 모두 허용
                )
                .cors(cors-> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOrigins(List.of("*"));
                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(List.of("*"));
                    corsConfig.setAllowCredentials(false);
                    corsConfig.setMaxAge(3600L);
                    return corsConfig;

                }))
                .exceptionHandling(ex ->
                        ex.accessDeniedHandler((request, response, accessDeniedException) -> {
                            setCorsHeaders(response);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        }).authenticationEntryPoint((request, response, authException) -> {
                            setCorsHeaders(response);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny()) // X-Frame-Options 설정
                );
        return http.build();


    }
    // 에러 시에도 CORS 헤더를 응답에 추가하는 헬퍼 메서드
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "https://sssiion.github.io");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "false");
    }
}
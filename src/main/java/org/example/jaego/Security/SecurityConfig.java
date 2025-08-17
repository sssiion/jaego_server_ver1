package org.example.jaego.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()  // 사용자 API 허용
                        .requestMatchers("/api/chatrooms/**").permitAll()  // 채팅방 API 허용
                        .requestMatchers("/api/chatrooms?**").permitAll()
                        .requestMatchers("/api/chatrooms").permitAll()
                        .requestMatchers("/ws/", "/ws", "/sockjs/", "/websocket/", "/info/","/ws/**").permitAll()  // WebSocket 허용
                        .requestMatchers("/", "/health").permitAll()  // 루트, 헬스체크 허용
                        .anyRequest().permitAll()  // 나머지도 모두 허용
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny()) // X-Frame-Options 설정
                )
                .build();
    }
}
package com.ong.backend.infrastructure.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails secretaria = User.builder()
                .username("secretaria")
                .password(passwordEncoder.encode("secretaria123"))
                .roles("SECRETARIA")
                .build();

        UserDetails auditor = User.builder()
                .username("auditor")
                .password(passwordEncoder.encode("auditor123"))
                .roles("AUDITOR")
                .build();

        return new InMemoryUserDetailsManager(secretaria, auditor);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Arquivos estáticos do console web e console H2
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico",
                                "/h2-console/**"
                        ).permitAll()
                        // Consultas de leitura: permitidas para SECRETARIA e AUDITOR
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("SECRETARIA", "AUDITOR")
                        // Operações de escrita (cadastro, nova versão e arquivamento): restritas à SECRETARIA
                        .requestMatchers(HttpMethod.POST, "/api/v1/documentos/**").hasRole("SECRETARIA")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/documentos/**").hasRole("SECRETARIA")
                        // Qualquer outro recurso da API requer autenticação
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            response.getWriter().write("""
                                {
                                  "type": "about:blank",
                                  "title": "Não Autenticado",
                                  "status": 401,
                                  "detail": "Autenticação requerida via HTTP Basic para acessar a API.",
                                  "instance": "%s",
                                  "timestamp": "%s"
                                }
                            """.formatted(request.getRequestURI(), Instant.now()));
                        })
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            response.getWriter().write("""
                                {
                                  "type": "about:blank",
                                  "title": "Acesso Proibido",
                                  "status": 403,
                                  "detail": "Você não possui permissão para executar esta operação de escrita. Papel requerido: SECRETARIA.",
                                  "instance": "%s",
                                  "timestamp": "%s"
                                }
                            """.formatted(request.getRequestURI(), Instant.now()));
                        })
                );

        return http.build();
    }
}

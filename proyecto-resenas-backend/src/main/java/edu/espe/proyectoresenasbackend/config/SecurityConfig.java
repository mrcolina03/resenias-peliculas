package edu.espe.proyectoresenasbackend.config;

import edu.espe.proyectoresenasbackend.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- ASEGÚRATE DE QUE ESTE IMPORT ESTÉ
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://proyecto-resenas-frontend.onrender.com"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        // Rutas públicas
                        .requestMatchers("/api/resenas/auth/**").permitAll()
                        .requestMatchers("/api/resenas/version").permitAll()

                        // --- INICIO DE LA CORRECCIÓN ---
                        // GET público para ciertas entidades
                        // Las rutas deben coincidir con tus @RequestMapping
                        .requestMatchers(HttpMethod.GET, "/api/resenas/peliculas/**").permitAll() // ANTES: /api/peliculas/**
                        .requestMatchers(HttpMethod.GET, "/api/resenas/cines/**").permitAll()    // ANTES: /api/cines/**
                        .requestMatchers(HttpMethod.GET, "/api/resenas/resena/**").permitAll()  // ANTES: /api/resenas/**
                        // NUEVOS
                        .requestMatchers(HttpMethod.GET, "/api/resenas/chat/stream").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/resenas/chat/messages/recent").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/resenas/chat/simulator/status").permitAll()

                        // Las operaciones de escritura en chat requieren autenticación
                        .requestMatchers("/api/resenas/chat/**").authenticated()

                        // El resto requiere autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
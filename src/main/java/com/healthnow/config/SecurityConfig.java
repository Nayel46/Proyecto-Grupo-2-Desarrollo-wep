
package com.healthnow.config;

import com.healthnow.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UsuarioDetailsService usuarioDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(usuarioDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider)
            throws Exception {

        http
            .authenticationProvider(authenticationProvider)

            .authorizeHttpRequests(auth -> auth

                // Páginas públicas
                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/static.css",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // Funciones exclusivas del administrador
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                // Funciones exclusivas del doctor
                .requestMatchers("/doctor/**")
                .hasRole("DOCTOR")

                // Funciones exclusivas del paciente
                .requestMatchers("/paciente/**")
                .hasRole("PACIENTE")

                // Especialidades para usuarios autenticados
                .requestMatchers("/especialidad/**")
                .hasAnyRole("ADMIN", "DOCTOR", "PACIENTE")

                // Las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")

                // Todos los usuarios van al mismo inicio
                .successHandler((request, response, authentication) -> {
                    response.sendRedirect(
                        request.getContextPath() + "/"
                    );
                })

                .failureUrl("/login?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}

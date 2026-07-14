package com.BackTecnophones;

import java.util.List;

import com.BackTecnophones.config.SessionAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
	@Bean
	@Order(1)
	public SecurityFilterChain afRelayFilterChain(HttpSecurity http) throws Exception {
	    http
	      .securityMatcher("/afrelay/**")
	      .csrf(csrf -> csrf.disable())
	      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	      .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
	    return http.build();
	  }

	@Bean
	@Order(2)
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	      .csrf(csrf -> csrf.disable())
	      .addFilterBefore(new SessionAuthFilter(), UsernamePasswordAuthenticationFilter.class)
	      .authorizeHttpRequests(auth -> auth
	          .requestMatchers("/error", "/usuarios/verificar", "/usuarios/logout", "/auth/login", "/login", "/articulos/**", "/categorias/**", "/rubros/**", "/ventas/mp", "/ventas/aprobadas", "/webhooks/mp/**", "/comprobantes/*/pdf", "/afrelay/**").permitAll() // Publicos
	          .anyRequest().authenticated() // -> Para produccion
	      );
	    return http.build();
	  }
	
	@Bean
	  public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration cfg = new CorsConfiguration();
	    cfg.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://localhost:5174", "https://tecnophones00.web.app", "https://tecnophones.com.ar")); //5174 Por Romero -> Despues Sacarlo y poner el de produccion
	    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
	    cfg.setAllowedHeaders(List.of("*"));
	    cfg.setAllowCredentials(true);
	    cfg.setMaxAge(3600L);
	
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", cfg);
	    return source;
	  }
	
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder(12);
	}
}

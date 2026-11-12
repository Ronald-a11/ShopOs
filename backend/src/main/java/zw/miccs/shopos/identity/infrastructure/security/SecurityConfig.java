package zw.miccs.shopos.identity.infrastructure.security;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import zw.miccs.shopos.identity.application.AuthProperties;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		AuthTokenAuthenticationFilter authTokenAuthenticationFilter,
		AuthenticationEntryPoint authenticationEntryPoint,
		AccessDeniedHandler accessDeniedHandler
	) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/actuator/health/**",
					"/v3/api-docs/**",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/api/v1/auth/login",
					"/api/v1/auth/refresh",
					"/api/v1/auth/logout",
					"/api/v1/auth/forgot-password",
					"/api/v1/auth/reset-password",
					"/api/v1/auth/verification/**"
				).permitAll()
				.anyRequest().authenticated())
			.addFilterBefore(authTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationEntryPoint authenticationEntryPoint() {
		return (HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.AuthenticationException authException) ->
			writeProblem(response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED",
				"Unauthenticated", "Authentication is required");
	}

	@Bean
	AccessDeniedHandler accessDeniedHandler() {
		return (HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.access.AccessDeniedException accessDeniedException) ->
			writeProblem(response, HttpStatus.FORBIDDEN, "ACCESS_DENIED",
				"Access denied", "You do not have permission to perform this action");
	}

	private static void writeProblem(
		HttpServletResponse response,
		HttpStatus status,
		String code,
		String title,
		String detail
	) throws IOException {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setTitle(title);
		pd.setType(URI.create("https://docs.shopos.co.zw/errors/" + code.toLowerCase().replace('_', '-')));
		pd.setProperty("code", code);
		pd.setProperty("traceId", UUID.randomUUID().toString().substring(0, 8));

		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		new ObjectMapper().writeValue(response.getOutputStream(), pd);
	}

}

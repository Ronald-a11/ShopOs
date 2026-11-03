package zw.miccs.shopos.identity.infrastructure;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class IdentitySecurityConfig {

	@Bean
	@Profile({ "local", "test" })
	DevAuthenticationFilter devAuthenticationFilter() {
		return new DevAuthenticationFilter();
	}

	@Bean
	FilterRegistrationBean<JwtAuthenticationFilter> disableJwtServletRegistration(JwtAuthenticationFilter filter) {
		FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtAuthenticationFilter jwtAuthenticationFilter,
		ObjectProvider<DevAuthenticationFilter> devAuthenticationFilter) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.setCharacterEncoding(StandardCharsets.UTF_8.name());
				response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
				response.getWriter().write("""
					{"type":"https://docs.shopos.co.zw/errors/unauthorized","title":"Unauthorized","status":401,"code":"UNAUTHORIZED","detail":"Authentication required","traceId":"security"}
					""");
			}))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/actuator/health/**",
					"/v3/api-docs/**",
					"/swagger-ui/**",
					"/api/v1/auth/login",
					"/api/v1/auth/refresh",
					"/api/v1/auth/logout")
				.permitAll()
				.anyRequest().authenticated())
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		DevAuthenticationFilter dev = devAuthenticationFilter.getIfAvailable();
		if (dev != null) {
			http.addFilterAfter(dev, JwtAuthenticationFilter.class);
		}
		return http.build();
	}
}

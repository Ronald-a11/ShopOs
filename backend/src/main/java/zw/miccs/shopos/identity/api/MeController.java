package zw.miccs.shopos.identity.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import zw.miccs.shopos.common.api.UnauthorizedException;
import zw.miccs.shopos.identity.api.AuthDtos.MeResponse;
import zw.miccs.shopos.identity.application.AuthService;
import zw.miccs.shopos.identity.application.ShoposPrincipal;

@RestController
@RequestMapping("/api/v1")
public class MeController {

	private final AuthService authService;

	public MeController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/me")
	MeResponse me(@AuthenticationPrincipal ShoposPrincipal principal) {
		if (principal == null) {
			throw new UnauthorizedException("UNAUTHORIZED", "Authentication required");
		}
		return MeResponse.from(authService.me(principal.userId()));
	}
}

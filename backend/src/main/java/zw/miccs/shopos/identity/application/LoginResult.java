package zw.miccs.shopos.identity.application;

public record LoginResult(
	AuthenticatedUser user,
	AuthSessionTokens tokens
) {
}

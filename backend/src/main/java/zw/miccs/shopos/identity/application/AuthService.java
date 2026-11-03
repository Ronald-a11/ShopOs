package zw.miccs.shopos.identity.application;

import java.util.UUID;

import zw.miccs.shopos.identity.application.AuthModels.AuthResult;
import zw.miccs.shopos.identity.application.AuthModels.MeView;

public interface AuthService {

	AuthResult login(String email, String password, UUID deviceId, boolean keepSignedIn);

	AuthResult refresh(String rawRefreshToken);

	void logout(String rawRefreshToken);

	MeView me(UUID userId);
}

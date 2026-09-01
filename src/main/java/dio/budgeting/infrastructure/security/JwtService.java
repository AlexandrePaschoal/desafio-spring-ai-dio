package dio.budgeting.infrastructure.security;

import dio.budgeting.domain.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.expiration}")
    private long expirationSeconds;

    public JwtService(
            JwtEncoder jwtEncoder
    ) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(User user) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("smart-budget")
                .issuedAt(now)
                .expiresAt(
                        now.plusSeconds(expirationSeconds)
                )
                .subject(
                        user.getId().toString()
                )
                .claim(
                        "email",
                        user.getEmail()
                )
                .claim(
                        "name",
                        user.getName()
                )
                .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(claims)
                )
                .getTokenValue();
    }
}
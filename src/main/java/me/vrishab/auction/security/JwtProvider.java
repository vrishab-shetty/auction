package me.vrishab.auction.security;

import me.vrishab.auction.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
public class JwtProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtProvider(JwtEncoder jwtEncoder, @Qualifier("jwtDecoder") JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String createToken(UserDTO userDTO, List<String> authorities) {
        Instant now = Instant.now();
        long expiresIn = 2; // 2 hours

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn, ChronoUnit.HOURS))
                .subject(userDTO.name())
                .claim("id", userDTO.id())
                .claim("authorities", authorities)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String getUserData(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        Map<String, Object> claims = jwt.getClaims();

        return (String) claims.get("id");
    }
}

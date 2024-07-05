package me.vrishab.auction.security;

import me.vrishab.auction.user.UserPrincipal;
import me.vrishab.auction.user.converter.UserToUserDTOConverter;
import me.vrishab.auction.user.dto.UserDTO;
import me.vrishab.auction.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final UserToUserDTOConverter userToUserDTOConverter;

    public AuthService(JwtProvider jwtProvider, UserToUserDTOConverter userToUserDTOConverter) {
        this.jwtProvider = jwtProvider;
        this.userToUserDTOConverter = userToUserDTOConverter;
    }

    public Map<String, Object> createLoginInfo(Authentication auth) {

        // Create user info
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = principal.getUser();
        UserDTO userDTO = this.userToUserDTOConverter.convert(user);

        // Generate a Jwt
        String authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        String token = this.jwtProvider.createToken(userDTO, Collections.singletonList(authorities));

        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("userInfo", userDTO);
        loginResult.put("token", token);

        return loginResult;
    }

    public String getUserInfo(Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken)) {
            throw new AuthenticationRequiredException(AuthenticationRequiredException.AuthType.BEARER_TOKEN);
        }

        Jwt jwtToken = ((JwtAuthenticationToken) auth).getToken();

        return this.jwtProvider.getUserData(jwtToken.getTokenValue());
    }
}

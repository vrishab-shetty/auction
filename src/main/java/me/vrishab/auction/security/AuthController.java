package me.vrishab.auction.security;

import lombok.extern.slf4j.Slf4j;
import me.vrishab.auction.security.AuthenticationRequiredException.AuthType;
import me.vrishab.auction.system.Result;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.endpoint.base-url}")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/users/login")
    public Result getLoginInfo(Authentication auth) {

        if (!(auth instanceof UsernamePasswordAuthenticationToken))
            throw new AuthenticationRequiredException(AuthType.BASIC);

        return new Result(true, "User Info and Json Web Token", this.authService.createLoginInfo(auth));
    }
}

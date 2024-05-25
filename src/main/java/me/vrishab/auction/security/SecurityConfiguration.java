package me.vrishab.auction.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class SecurityConfiguration {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    private final CustomBasicAuthenticationEntryPoint basicAuthenticationEntryPoint;
    private final CustomBearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint;
    private final CustomBearerTokenAccessDeniedHandler bearerTokenAccessDeniedHandler;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    public SecurityConfiguration(CustomBasicAuthenticationEntryPoint basicAuthenticationEntryPoint, CustomBearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint, CustomBearerTokenAccessDeniedHandler bearerTokenAccessDeniedHandler) throws NoSuchAlgorithmException {
        this.basicAuthenticationEntryPoint = basicAuthenticationEntryPoint;
        this.bearerTokenAuthenticationEntryPoint = bearerTokenAuthenticationEntryPoint;
        this.bearerTokenAccessDeniedHandler = bearerTokenAccessDeniedHandler;
        KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");

        rsa.initialize(2048);
        KeyPair keyPair = rsa.generateKeyPair();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2ResourceServerProperties oAuth2ResourceServerProperties) throws Exception {
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/auctions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/auctions/**").hasAuthority("ROLE_user")
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/auctions/**").hasAuthority("ROLE_user")
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/auctions/**").hasAuthority("ROLE_user")
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/user/**").hasAuthority("ROLE_user")
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/user/**").hasAuthority("ROLE_user")
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/user/**").hasAuthority("ROLE_user")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headerConfig -> headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(this.basicAuthenticationEntryPoint))
                .oauth2ResourceServer(oauth2ResourceServer -> {
                            oauth2ResourceServer.jwt(Customizer.withDefaults());
                            oauth2ResourceServer.authenticationEntryPoint(this.bearerTokenAuthenticationEntryPoint);
                            oauth2ResourceServer.accessDeniedHandler(this.bearerTokenAccessDeniedHandler);
                        }
                )
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
        JWKSource<SecurityContext> jwtSet = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwtSet);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

}

package com.finisinfinitatis.authservice.security;
import com.finisinfinitatis.authservice.security.optional.CustomTokenEnhancer;
import org.apache.el.parser.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.util.List;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    int expirationTimeMin = 30;

    public AuthorizationServerConfig(AuthenticationManager authenticationManager,
                                     CustomUserDetailsService userDetailsService,
                                     @Lazy PasswordEncoder passwordEncoder){
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
        configurer.inMemory()
                .withClient("testClient")
                // had to encode secret because of "Encoded password does not look like BCrypt"
                .secret(passwordEncoder.encode("mysecret"))
                .authorizedGrantTypes("password", "refresh_token", "access_code", "authorization_code")
                .redirectUris("http://localhost:8083/webjars/springfox-swagger-ui/oauth2-redirect.html")
                .scopes("read", "write", "user")
                .accessTokenValiditySeconds(expirationTimeMin * 60)
                .refreshTokenValiditySeconds(expirationTimeMin * 60);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        // TokenEnhancer is optional but the accessTokenConverter is appended here as well!
        // Without it no jwt is returned!
        tokenEnhancerChain.setTokenEnhancers(List.of(new CustomTokenEnhancer(), accessTokenConverter()));

        endpoints.tokenStore(tokenStore())
                .tokenEnhancer(tokenEnhancerChain)
                .userDetailsService(userDetailsService)
                .authenticationManager(authenticationManager);
    }
    
    @Bean
    public TokenStore tokenStore(){
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    /*used: keytool.exe ( found in jdk /bin dir
    keytool -genkey -v -keystore my-release-key.keystore -alias mykey_alias -keyalg RSA -keysize 2048 -validity 10000
     */
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyStoreKeyFactory kskf = new KeyStoreKeyFactory(new ClassPathResource("my-release-key.keystore"),
                "testpass".toCharArray());
        converter.setKeyPair(kskf.getKeyPair("mykey_alias"));
        return converter;
    }
    
    @Bean
    @Primary
    public DefaultTokenServices tokenServices(){
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setSupportRefreshToken(true);
        return tokenServices;
    }
}

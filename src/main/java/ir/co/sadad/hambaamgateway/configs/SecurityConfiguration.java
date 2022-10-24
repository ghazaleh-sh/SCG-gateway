package ir.co.sadad.hambaamgateway.configs;

import ir.co.sadad.hambaamgateway.filters.JwtFilter;
import ir.co.sadad.hambaamgateway.services.JwtValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ir.co.sadad.hambaamgateway.Constant.REMOTE_CONFIG_PATH;

@Slf4j
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    //    @Autowired
//    private BmiSsoTokenProvider bmiSsoTokenProvider;
    @Autowired
    private JwtValidatorService jwtValidatorService;

    @Value("${scopes.push-notification-secure}")
    private String scope;

    @Value("${permit.routes}")
    private String permittedRoute;

    @Bean
    public WebClient client() {
        return WebClient.builder().build();
    }

    /**
     * this method does not need any scope or limitation for routing
     * just check if current path is in permitted list(set in config file) and matches with the method
     *
     * using for check-version api
     *
     * @param http
     * @return
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityWebFilterChain ssoBasedSecurityConfig(ServerHttpSecurity http) {

        final List<String> paths = Arrays.asList(permittedRoute.split(";"));

        return http
                .logout().disable()
//                .addFilterAt((WebFilter) new SsoAuthenticationWebFilter(bmiSsoTokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) ->
                        Mono.fromRunnable(() ->
                                {
                                    outputResponseBody(swe, "{\"error\": \"NOT-FOUND-AS-PERMITTED-PATH\"}", 404);
                                }
                        )
                ).accessDeniedHandler((swe, e) ->
                        Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN))
                ).and()
                .formLogin().disable()
                .cors().disable()
                .httpBasic().disable()
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.POST).permitAll()
                .pathMatchers(HttpMethod.GET).permitAll()
                .pathMatchers("/authentication/**").denyAll()
                .pathMatchers(String.valueOf(paths)).authenticated()
                .anyExchange().authenticated()
                .and().build();
    }

    /**
     * this method checks the path matches with the scope and then authorizes it.
     * otherwise sends "UNAUTHORIZED_OR_EXPIRED" error as a response
     *
     * @param http
     * @return
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public SecurityWebFilterChain authorizedSecurityConfig(ServerHttpSecurity http) {

        String remoteConfigPath = String.valueOf(String.valueOf(Arrays.stream(permittedRoute.split(";"))
                .filter(p -> p.contains(REMOTE_CONFIG_PATH)).collect(Collectors.toList()).get(0)));

        return http
                .csrf().disable()
                .addFilterAt(new JwtFilter(jwtValidatorService), SecurityWebFiltersOrder.HTTP_BASIC)
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) ->
                        Mono.fromRunnable(() ->
                                {
                                    outputResponseBody(swe, "{\"error\": \"UNAUTHORIZED_OR_EXPIRED\"}", 401);
                                }
                        )
                ).accessDeniedHandler((swe, e) ->
                        Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN))
                ).and()
                .formLogin().disable()
                .cors().disable()
                .httpBasic().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.POST).permitAll()
                .pathMatchers(HttpMethod.GET, remoteConfigPath).permitAll()
                .pathMatchers("/api/**").hasAuthority(scope)
                .anyExchange().authenticated()
                .and().build();
    }

    private void outputResponseBody(ServerWebExchange exchange, String text, int statusCode) {
        log.error("HambaamGatewayError: {}, {}", statusCode, text);
        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().bufferFactory().wrap(text.getBytes(StandardCharsets.UTF_8));
        exchange.getResponse().setRawStatusCode(statusCode);
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        exchange.getResponse().writeWith(Flux.just(buffer));
    }

}

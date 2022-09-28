package ir.co.sadad.hambaamgateway.filters;

import ir.co.sadad.hambaamgateway.services.JwtValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Due to the nature of the filter chain, a filter with lower precedence (a lower order in the chain)
 * will execute its “pre” logic in an earlier stage, but it's “post” implementation will get invoked later:
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter implements WebFilter, Ordered {

    private final JwtValidatorService validatorService;

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("First Pre Web(gateway) Filter");
        final String bearerToken = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (validatorService.isValid(bearerToken)) {
            ServerHttpRequest mutateRequest = exchange.getRequest().mutate()
                    .headers(h -> h.addAll(validatorService.setHeadersToMSRoute(bearerToken)))
                    .build();
            ServerWebExchange mutateServerWebExchange = exchange.mutate().request(mutateRequest).build();
            return chain.filter(mutateServerWebExchange)
                    .then(Mono.fromRunnable(() -> log.info("Last Post Web(gateway) Filter after validation")));
        }
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    log.info("Last Post Web(gateway) Filter when bearer token is not valid...");
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}

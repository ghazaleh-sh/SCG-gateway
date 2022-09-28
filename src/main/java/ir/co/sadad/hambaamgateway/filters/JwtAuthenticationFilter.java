package ir.co.sadad.hambaamgateway.filters;

import com.nimbusds.jwt.JWTClaimsSet;
import ir.co.sadad.hambaamgateway.services.JwtValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

//@Component
public class JwtAuthenticationFilter implements GatewayFilter {

//    @Autowired
    private JwtValidatorService jwtValidatorService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        final List<String> apiEndpoints = List.of("/push-hamrahbam", "/push-bampay");

        //another services
        Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));

        if (isApiSecured.test(request)) {
            if (!request.getHeaders().containsKey("Authorization")) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);

                return response.setComplete();
            }

            final String token = request.getHeaders().getOrEmpty("Authorization").get(0); //.getFirst("Authorization");

            try {
                jwtValidatorService.isValid(token);
            } catch (Exception e) { //JwtTokenMalformedException | JwtTokenMissingException

                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.BAD_REQUEST);

                return response.setComplete();
            }

//            JWTClaimsSet claims = jwtValidatorService.claims(token);
        }

        return chain.filter(exchange);
    }

}

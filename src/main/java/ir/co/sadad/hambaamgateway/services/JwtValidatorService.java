package ir.co.sadad.hambaamgateway.services;

import com.nimbusds.jwt.JWTClaimsSet;
import ir.co.sadad.hambaamgateway.dtos.AuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;

import static ir.co.sadad.hambaamgateway.Constant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtValidatorService {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Verifies the JWT Token, returns true if it is valid
     *
     * @param bearerJwt
     * @return true/false
     */
    public boolean isValid(String bearerJwt) {
        if (bearerJwt == null || bearerJwt.isEmpty()) {
            return false;
        }
        if (!bearerJwt.matches(AUTHORIZATION_REGEX)) {
            return false;
        }

        try {
            JWTClaimsSet jws = jwtUtil.getJws(bearerJwt);
            return !JwtUtil.jatNotExpired(jws);
        } catch (RuntimeException | ParseException e) {
            log.error("invalid bearer Jwt: {}", bearerJwt);
        }
        return false;
    }

    public MultiValueMap<String, String> setHeadersToMSRoute(String bearerToken){
        AuthenticationToken authenticationToken = getAuthenticationToken(bearerToken);
        MultiValueMap<String, String> headers = new HttpHeaders();
//        headers.add(HttpHeaders.AUTHORIZATION, authenticationToken.getJwtToken());
        headers.add("ssn",authenticationToken.getClaims().getClaim("ssn").toString());
        headers.add("serialId",authenticationToken.getClaims().getClaim("serial").toString());
        return headers;

    }

    private AuthenticationToken getAuthenticationToken(String bearerToken) {
        return new AuthenticationToken(bearerToken, jwtUtil.getClaims(bearerToken));
    }

}

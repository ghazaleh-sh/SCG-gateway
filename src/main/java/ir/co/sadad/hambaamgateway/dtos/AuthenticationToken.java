package ir.co.sadad.hambaamgateway.dtos;

import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

@Getter
public class AuthenticationToken extends AbstractAuthenticationToken {


    private final String jwtToken;

    private final JWTClaimsSet claims;

    public AuthenticationToken(String jwtToken, JWTClaimsSet claims) { //Jws<Claims>
        super(Collections.emptySet());
        this.jwtToken = jwtToken;
        this.claims = claims;
        setAuthenticated(claims != null);
        setDetails(claims);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;//(String) claims.getBody().get("cellPhoneNo");
    }
}

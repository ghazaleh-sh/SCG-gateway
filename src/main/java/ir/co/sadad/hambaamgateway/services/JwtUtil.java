package ir.co.sadad.hambaamgateway.services;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

import static ir.co.sadad.hambaamgateway.Constant.*;

@Slf4j
@Component
public class JwtUtil {


    public JWTClaimsSet getJws(String bearerJwt) throws ParseException {

        String jwtToken = bearerJwt.replaceFirst(BEARER__PREFIX, EMPTY_STRING);
        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();

    }


    public JWTClaimsSet getClaims(String bearerJwt) {
        if (bearerJwt == null || bearerJwt.isEmpty()) {
            return null;
        }
        if (!bearerJwt.matches(AUTHORIZATION_REGEX)) {
            return null;
        }

        try {
            JWTClaimsSet jws = getJws(bearerJwt);
            return jatNotExpired(jws) ? null : jws;
        } catch (RuntimeException | ParseException e) {
            log.error("invalid bearerJwt: {}", bearerJwt);
        }
        return null;
    }

    public static boolean jatNotExpired(JWTClaimsSet jws) {
        Date exp = jws.getExpirationTime();
//        Date iat = jws.getNotBeforeTime();
        Date now = new Date();
        return now.after(exp);
    }

}

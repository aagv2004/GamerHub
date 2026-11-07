package cl.duoc.gamerhub.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokens {
    private static final String ISSUER = "gamerhub";
    private static final int LIFETIME_SECONDS = 900;
    private final byte[] key = new byte[32];

    JwtTokens() {
        new SecureRandom().nextBytes(key);
    }

    public String issue(String email) {
        Instant now = Instant.now();
        var claims = new JWTClaimsSet.Builder()
            .issuer(ISSUER)
            .subject(email)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(LIFETIME_SECONDS)))
            .build();
        var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), claims);
        try {
            token.sign(new MACSigner(key));
            return token.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("No se pudo firmar el token.", exception);
        }
    }

    Optional<String> subject(String serialized) {
        try {
            SignedJWT token = SignedJWT.parse(serialized);
            if (!JWSAlgorithm.HS256.equals(token.getHeader().getAlgorithm()) || !token.verify(new MACVerifier(key))) {
                return Optional.empty();
            }

            JWTClaimsSet claims = token.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (!ISSUER.equals(claims.getIssuer()) || expiration == null || !expiration.toInstant().isAfter(Instant.now())) {
                return Optional.empty();
            }
            return Optional.ofNullable(claims.getSubject());
        } catch (JOSEException | ParseException exception) {
            return Optional.empty();
        }
    }
}

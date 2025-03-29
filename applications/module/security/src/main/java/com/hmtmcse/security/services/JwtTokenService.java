package com.hmtmcse.security.services;

import com.hmtmcse.security.model.entites.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenService {
    private static final String SIGNATURE_KEY = "YWlvayBsZW8gPSBBbWlyLVVMIElzbGFtIE9wdSBLaGFsaWZhIGFuZCBMRU8=";

    public String createToken(Users userDetails) {
        return createToken(new HashMap<>(), userDetails);
    }

    public String createToken(Map<String, Object> claims, Users userDetails) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 100 * 1000 * 60))
                .signWith(SignatureAlgorithm.HS256, getSecretKey())
                .compact();
    }

    public String getUsername(String token) {
        return extractClaim(token, Claims::getSubject); // Subject is the username
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(getClaims(token));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);

    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public Claims getClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

    // I can do all kind of stuff here for the sake of Business Logic and Security
    private Key getSecretKey() {
        byte[] secretBytes = Base64.getDecoder().decode(SIGNATURE_KEY);
        return new SecretKeySpec(secretBytes, 0, secretBytes.length, SignatureAlgorithm.HS256.name());
    }
}

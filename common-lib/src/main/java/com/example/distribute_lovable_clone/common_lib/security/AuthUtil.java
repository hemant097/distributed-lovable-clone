package com.example.distribute_lovable_clone.common_lib.security;

import com.example.distribute_lovable_clone.common_lib.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class AuthUtil {
    @Value("${jwt.secret-key}")
    private String jwtSecretKey;

    final long oneMinute = 60000L;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDto user) {
        return Jwts.builder()
                .subject(user.username())
                .claim("userId", user.id().toString())
//                .claim() will be added for roles later
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (oneMinute*100)))
                .signWith(getSecretKey())
                .compact();
    }

    public JwtUserPrincipal verifyAccessToken(String token){
        //this takes the encoded header and payload , and passes header, payload, secret key through the algorithm,
        // and produces an encoded control signature, if it doesn't match with the token provided, authentication is failed

        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.parseLong(claims.get("userId",String.class));
        String username = claims.getSubject();

        return new JwtUserPrincipal(userId, username,null, List.of());
    }

    public Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication!=null && authentication.getPrincipal() instanceof JwtUserPrincipal userPrincipal){
            log.info("current user logged in is : {} ",userPrincipal.userName());
            return userPrincipal.userId();
        }
        else
            throw new AuthenticationCredentialsNotFoundException("No JWT found");

    }
}

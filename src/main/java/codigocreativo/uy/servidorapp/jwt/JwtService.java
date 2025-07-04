package codigocreativo.uy.servidorapp.jwt;

import jakarta.ejb.Stateless;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Optional;

import java.security.Key;
import java.util.Date;

import codigocreativo.uy.servidorapp.jwt.SecretKeyUtil;

@Stateless
public class JwtService {


   

    // Decodificar la clave secreta en Base64
    private static final String DEFAULT_SECRET_KEY = "VGhpc0lzQTMyQnl0ZUxvbmdTZWNyZXRLZXlGb3JKV1Q=";

    private final Key secretKey = Keys.hmacShaKeyFor(
            DatatypeConverter.parseBase64Binary(
                    Optional.ofNullable(System.getenv("SECRET_KEY"))
                            .orElse(DEFAULT_SECRET_KEY)));


    public String generateToken(String email, String nombrePerfil) {
        try {
            if (email == null || nombrePerfil == null) {
                return null;  // Retornar null si email o perfil son nulos
            }
            return Jwts.builder()
                    .setSubject(email)
                    .claim("perfil", nombrePerfil)
                    .claim("email", email)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000)) // 5 min de expiración
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            return null;  // Retornar null si ocurre algún error
        }
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

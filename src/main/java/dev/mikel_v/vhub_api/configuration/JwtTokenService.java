package dev.mikel_v.vhub_api.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token Service.
 *
 * This class provides functionality for generating and validating JSON Web Tokens (JWTs).
 * It is responsible for handling JWT-related operations, such as creating a token, extracting
 * information from a token, and verifying the token's authenticity and integrity.
 *
 * Note: This class is annotated with @Service to indicate that it is a service component.
 *
 * JSON Web Tokens (JWTs) are an industry-standard for representing claims securely between two parties.
 * They are commonly used for authentication and authorization purposes in web applications.
 *
 * @see JwtTokenService
 * @author Mikel Villota
 * @version 1.0
 * @since 2023-06-14
 */
@Service
public class JwtTokenService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private int jwtExpiration;

    /**
     * Extracts the subject (user phone) from a JWT token.
     * @param token the JWT token from which to extract the subject
     * @return the user phone as a string if it exists in the token, or null otherwise
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT token using the provided claims resolver function.
     * @param token the JWT token from which to extract the claim
     * @param claimsResolver a function that takes a Claims object and returns the desired claim value
     * @return the extracted claim value
     * @param <T> the type of the claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for the provided user details.
     * @param userDetails the user details to generate the token for
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token with the provided extra claims and user details, with an expiration time based on the
     * configured JWT expiration time.
     * @param extraClaims a map of extra claims to include in the JWT payload
     * @param userDetails the UserDetails object representing the authenticated user
     * @return a JWT token string
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Builds a JWT token with the given parameters.
     * @param extraClaims a Map containing extra claims to be added to the token
     * @param userDetails the user details to be added to the token
     * @param expiration the expiration time of the token, in milliseconds
     * @return the JWT token as a String
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            int expiration
    ) {
        Calendar calendar = Calendar.getInstance();
        Date issuedDate = Calendar.getInstance().getTime();
        calendar.add(Calendar.DATE, expiration);
        Date expirationDate = calendar.getTime();
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(issuedDate)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Checks whether the given JWT token is valid for the specified user details.
     * @param token The JWT token to check
     * @param userDetails The user details to validate against
     * @return True if the token is valid for the user details, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Check if the given JWT token has expired.
     * @param token the JWT token to check for expiration
     * @return true if the token has expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a given JWT token.
     * @param token the JWT token to extract the expiration date from
     * @return the expiration date of the JWT token as a {@code Date} object
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses the JWT token and extracts all the claims from it.
     * @param token The JWT token to be parsed.
     * @return The {@link Claims} object containing all the claims extracted from the JWT token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Retrieves the signing key to be used for generating and verifying JWTs.
     * @return the signing key as a Key object.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

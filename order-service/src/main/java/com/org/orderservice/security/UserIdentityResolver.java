package com.org.orderservice.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class UserIdentityResolver {
    private static final Logger log = LoggerFactory.getLogger(UserIdentityResolver.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String resolveUserIdentity() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }

        String authHeader = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "unknown";
        }

        String token = authHeader.substring(7);
        String subject = extractClaim(token, "sub");
        if (subject != null && !subject.isBlank()) {
            return subject;
        }

        String email = extractClaim(token, "email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        String name = extractClaim(token, "name");
        if (name != null && !name.isBlank()) {
            return name;
        }

        return "unknown";
    }

    private String extractClaim(String token, String claimName) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = objectMapper.readTree(new String(decodedPayload, StandardCharsets.UTF_8));
            JsonNode claim = payload.get(claimName);
            return claim != null && !claim.isNull() ? claim.asText() : null;
        } catch (Exception ex) {
            log.debug("Unable to parse JWT claim '{}'", claimName, ex);
            return null;
        }
    }
}

package com.example.transferstylerebuildmaven.services;

import com.example.transferstylerebuildmaven.models.token.Token;
import com.example.transferstylerebuildmaven.repositories.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private final TokenRepository tokenRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        final String keywordTokenName = "Bearer ";

        if (authHeader == null || !authHeader.startsWith(keywordTokenName)){
            return;
        }
        final String jwt = authHeader.substring(keywordTokenName.length());
        Token storedToken = tokenRepository.findByToken(jwt).orElse(null);
        if (storedToken != null){
            storedToken.setRevoked(true);
            storedToken.setExpired(true);
            tokenRepository.save(storedToken);

            SecurityContextHolder.clearContext();
        }

    }
}

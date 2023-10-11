package com.example.transferstylerebuildmaven.services;

import com.example.transferstylerebuildmaven.respones.AuthenticationResponse;
import com.example.transferstylerebuildmaven.requests.AuthenticationRequest;
import com.example.transferstylerebuildmaven.requests.RegisterRequest;
import com.example.transferstylerebuildmaven.models.token.Token;
import com.example.transferstylerebuildmaven.models.token.TokenType;
import com.example.transferstylerebuildmaven.models.user.User;
import com.example.transferstylerebuildmaven.repositories.TokenRepository;
import com.example.transferstylerebuildmaven.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

    public AuthenticationResponse register(RegisterRequest request) {
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, accessToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword())
        ); // user authenticated
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow( ()
                        -> new UsernameNotFoundException("User with name " + request.getUsername() + " not found") );

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String keywordTokenName = "Bearer ";
        if (authHeader == null ||!authHeader.startsWith(keywordTokenName)) {
            return;
        }
        final String refreshToken = authHeader.substring(keywordTokenName.length());
        final String username =  jwtService.extractUsername(refreshToken);
        if (username != null ){
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User with name " + username + " not found\nCannot refresh token"
                    ));

            if (jwtService.isTokenValid(refreshToken, user)){
                String accessToken =  jwtService.generateAccessToken(user);
                revokeAllUserTokens(user); // revoke old tokens
                saveUserToken(user, accessToken);
                AuthenticationResponse authenticationResponse =
                        AuthenticationResponse.builder()
                                .accessToken(accessToken) // new access token
                                .refreshToken(refreshToken) // old refresh token
                                .build();
                try {
                    // writes the AuthenticationResponse object as JSON to the response's output stream using the ObjectMapper class.
                    new ObjectMapper().writeValue(response.getOutputStream(), authenticationResponse);
                } catch (IOException e) {
                    System.err.println("Cannot write AuthenticationResponse object as JSON");
                    e.printStackTrace();

                }
            }
        }
    }

    private void revokeAllUserTokens(User user){
        List<Token>  validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()){
            return;
        }
            validUserTokens.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
        tokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(User user, String accessToken){
        Token token = Token.builder()
                .user(user)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .token(accessToken)
                .build();
        tokenRepository.save(token);
    }
}

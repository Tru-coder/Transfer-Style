package com.example.transferstylerebuildmaven.configs.security;

import com.example.transferstylerebuildmaven.repositories.TokenRepository;
import com.example.transferstylerebuildmaven.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getServletPath().contains("/api/v1/auth")) {
            /*
                the request is allowed to continue without any further processing,
                and the filterChain.doFilter() method is called to pass
                the request to the next filter in the chain.
             */
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String keywordTokenName = "Bearer ";

        if (authHeader == null || !authHeader.startsWith(keywordTokenName)) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwtToken = authHeader.substring(keywordTokenName.length());
        final String username = jwtService.extractUsername(jwtToken);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            /*
             user email is not null
             and there is no authentication present
             in the SecurityContextHolder.getContext(),
             proceeds to perform additional checks on the JWT and the user token.
             */
            //user not authenticated
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            boolean isTokenFunctioning =  tokenRepository.findByToken(jwtToken)
                    .map(t-> !t.isExpired() && !t.isRevoked())
                    .orElse(false);


            if (jwtService.isTokenValid(jwtToken, userDetails) && isTokenFunctioning){
                // if user valid
                // creates an authentication token
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // update auth token
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }


}

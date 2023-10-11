package com.example.transferstylerebuildmaven.auditing;

import com.example.transferstylerebuildmaven.models.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class ApplicationAuditAware  implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
        ){ return Optional.empty(); }

        User userPrincipal = (User) authentication.getPrincipal();
        // Optional containing the id of the user obtained from the User object, or null if the user's id is null.
        return Optional.ofNullable(userPrincipal.getId());
    }
}

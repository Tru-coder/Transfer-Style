package com.example.transferstylerebuildmaven.models.user;



import com.example.transferstylerebuildmaven.models.token.Token;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder()
@Table(name = "app_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    private Long id;

    @Setter(AccessLevel.NONE)
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String firstname;

    @Column(name = "last_name", nullable = false)
    private String lastname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;


    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;


    @PrePersist
    public void initialize() {
        uuid = UUID.randomUUID();
        isDeleted = false;
        registrationDate = LocalDateTime.now();
        lastModifiedDate = registrationDate;
    }

    @Override
    public String toString(){
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isDeleted='" + isDeleted + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}


package com.example.transferstylerebuildmaven.models.token;

import com.example.transferstylerebuildmaven.models.user.User;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private final TokenType tokenType = TokenType.BEARER;

    private boolean revoked;

    private boolean expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;
}

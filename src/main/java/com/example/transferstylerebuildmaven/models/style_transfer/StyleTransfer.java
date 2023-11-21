package com.example.transferstylerebuildmaven.models.style_transfer;

import com.example.transferstylerebuildmaven.models.Image.Image;
import com.example.transferstylerebuildmaven.models.Image.ImageType;
import com.example.transferstylerebuildmaven.models.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name="style_transfer")
@Getter
@Setter
public class StyleTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(nullable = false, name = "id")
    private Long id;

    @Column(nullable = false, name = "uuid_request")
    private UUID uuidRequest;

    @Column(nullable = false, name = "optimizer_type")
    private String optimizer;

    @Column(nullable = false,name = "created_date")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "execution_time")
    private LocalDateTime executionTime;

    @Column(nullable = false, name = "algorithm_style_transfer_type")
    @Enumerated(EnumType.STRING)
    private StyleTransferType algorithmStyleTransferType;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "styleTransfer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @MapKey(name = "imageType")
    private Map<ImageType, Image> images;

    @Override
    public String toString(){
        return "StyleTransfer{" +
                "uuid='" + uuidRequest + '\'' +
                ", optimizer='" + optimizer + '\'' +
                ", execution Time='" + executionTime + '\'' +
                ", isDeleted='" + isDeleted + '\'' +
                '}';
    }


    @PrePersist
    public void initialize() {
        isDeleted = false;
    }
}

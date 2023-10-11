package com.example.transferstylerebuildmaven.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name="style_transfer")
@Getter
@Setter
@ToString
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
    private String algorithmStyleTransferType;

    @Column(nullable = false, name = "style_image_absolute_path")
    private String styleImageAbsolutePath;

    @Column(nullable = false, name = "original_image_absolute_path")
    private String originalImageAbsolutePath;

    @Column(nullable = false, name = "created_image_absolute_path")
    private String createdImageAbsolutePath;

    @Lob
    @Column(nullable = false, name = "style_image_in_bytes" )
    private byte[] styleImageInBytes;
    @Lob
    @Column(nullable = false, name = "original_image_in_bytes")
    private byte[] originalImageInBytes;
    @Lob
    @Column(nullable = false, name = "created_image_in_bytes")
    private byte[] createdImageInBytes;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted;

    @PrePersist
    public void initialize() {
        isDeleted = false;
    }
}

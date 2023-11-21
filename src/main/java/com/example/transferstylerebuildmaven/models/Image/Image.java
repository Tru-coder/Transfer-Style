package com.example.transferstylerebuildmaven.models.Image;

import com.example.transferstylerebuildmaven.models.style_transfer.StyleTransfer;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(nullable = false, name = "id")
    private Long id;

    @Column(name = "image_extension", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageExtension imageExtension;

    @Column(name = "image_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    @Column(name = "image_name", nullable = false)
    private String imageName;

    @Column(name = "absolute_path", nullable = false)
    private String absolutePath;

    @Lob
    @Column(name = "image_in_bytes", nullable = false)
    private byte[] imageInBytes;

    @ManyToOne
    @JoinColumn(name = "style_transfer_id")
    private StyleTransfer styleTransfer;
}

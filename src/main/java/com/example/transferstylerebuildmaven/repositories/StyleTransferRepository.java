package com.example.transferstylerebuildmaven.repositories;

import com.example.transferstylerebuildmaven.models.style_transfer.StyleTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
public interface StyleTransferRepository extends JpaRepository<StyleTransfer, Long> {
     void deleteByUuidRequest(UUID uuidRequest);
     Optional <StyleTransfer> findByUuidRequest(UUID uuidRequest);
}

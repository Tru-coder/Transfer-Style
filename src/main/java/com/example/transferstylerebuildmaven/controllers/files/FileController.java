package com.example.transferstylerebuildmaven.controllers.files;


import com.example.transferstylerebuildmaven.services.FileSystemStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FileController implements FileControllerInterface{
    private final FileSystemStorageService fileSystemStorageService;

    @Override
    public ResponseEntity<?> downloadAllResultFilesFromDisk(@PathVariable("uuidRequest") UUID uuidRequest) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth.getName());
        System.out.println(auth.getDetails());


        File zip = fileSystemStorageService.createZipResultFiles(uuidRequest);

        Path zipPath = zip.toPath();
        byte[] zipContent = Files.readAllBytes(zipPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDispositionFormData("attachment", "Result_files.zip");
        headers.setContentLength(zipContent.length);

        return new ResponseEntity<>(zipContent, headers, HttpStatus.OK);

    }
}

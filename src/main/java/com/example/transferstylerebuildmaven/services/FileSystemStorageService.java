package com.example.transferstylerebuildmaven.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Getter
@RequiredArgsConstructor
public class FileSystemStorageService {
    // Properties
    @Value("${upload.image.path}")// path from properties for file
    private String uploadImagePath;

    @Value("${output.image.path}") // path from properties for file
    private String outputImagePath;


    /**
     * Method to create a unique file name and transfer the file.
     *
     * @param original     The original file to be transferred
     * @param description  The description of the file
     * @param requestUUID  The UUID of the request
     * @return             The path of the transferred file
     * @throws IOException If an I/O error occurs
     */
    public String makeUniqueFileNameAndTransfer(MultipartFile original, String description, String requestUUID) throws IOException {
        if (original != null && !Objects.requireNonNull(original.getOriginalFilename()).isEmpty()){
            String uploadDirectory = uploadImagePath + File.separator + requestUUID;
            File uploadDir = new File(uploadDirectory);

            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String resultFilename = description + "_" + original.getOriginalFilename();
            original.transferTo(new File(uploadDir + "/" + resultFilename));

            return uploadDirectory + File.separator + resultFilename;
        }
        throw new RuntimeException("File " + original.getOriginalFilename() + "is empty or malicious");
    }

    private void zipFiles(List<File> files, ZipOutputStream zipOutputStream) throws IOException {
        Set<String> fileNameAdded = new HashSet<>();

        for (File file : files) {
            if (!fileNameAdded.contains(file.getName())) {
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    IOUtils.copy(fileInputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
                fileNameAdded.add(file.getName());
            }
        }
    }

    public List<File> getFilesInImageDirectory(UUID uuidRequest) {
        File directory = new File(outputImagePath + File.separator + uuidRequest);
        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));
    }

    public File createZipResultFiles(UUID uuidRequest) throws IOException {
        List<File> files = getFilesInImageDirectory(uuidRequest);
        File tempFile = File.createTempFile("resultFiles", ".zip");

        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            zipFiles(files, zipOutputStream);
        }

        return tempFile;
    }


}

package com.example.transferstylerebuildmaven.services;

import com.example.transferstylerebuildmaven.repositories.ImageRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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

    private final ImageRepository imageRepository;

    HashMap<MultipartFile, Boolean> transferedFiles = new HashMap<MultipartFile, Boolean>();

    public boolean isTransferedBefore(MultipartFile file) {
        return transferedFiles.containsKey(file);
    }

    /**
     * Method to create a unique file name and transfer the file.
     *
     * @param original    The original file to be transferred
     * @param description The description of the file
     * @param requestUUID The UUID of the request
     * @return The path of the transferred file
     * @throws IOException If an I/O error occurs
     */
    public String makeUniqueFileNameAndTransfer(MultipartFile original, String description, String requestUUID) throws IOException {

        if (original == null || original.isEmpty()) {
            // Throw an exception if the original file is empty or malicious
            throw new IllegalArgumentException("File is empty or malicious: " + Objects.requireNonNull(original).getOriginalFilename());
        }
        String uploadDirectory = uploadImagePath + File.separator + requestUUID;
        File uploadDir = new File(uploadDirectory);

        // Create upload directory if it does not exist
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + uploadDir);
            }
        }

        // Generate the result filename by combining description and the original file's name
        String resultFilename = description + "_" + original.getOriginalFilename();
        // Transfer the file to the upload directory
        File newFile = new File(uploadDir, resultFilename);
        original.transferTo(newFile);

        // Verify if the file transfer was successful
        if (!newFile.exists() || newFile.length() != original.getSize()) {
            throw new IOException("Failed to transfer the file to: " + newFile);
        }
        transferedFiles.put(original, true);
        // Return the absolute path of the transferred file
        return newFile.getAbsolutePath();
    }

    public static String extractFileNameFromPath(String path) {
        File file = new File(path);
        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");

        return pos > 0 ? fileName.substring(0, pos) : fileName;
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


    public String getTransferedImagePath(String originalFilename) throws FileNotFoundException {
        return imageRepository.getImageByImageName(originalFilename).orElseThrow(() -> new FileNotFoundException("File " + originalFilename + " not found in database")).getAbsolutePath();
    }

}

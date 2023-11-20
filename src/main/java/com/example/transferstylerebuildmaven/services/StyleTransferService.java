package com.example.transferstylerebuildmaven.services;

import com.example.transferstylerebuildmaven.models.style_transfer.StyleTransfer;
import com.example.transferstylerebuildmaven.models.style_transfer.StyleTransferType;
import com.example.transferstylerebuildmaven.models.user.User;
import com.example.transferstylerebuildmaven.repositories.StyleTransferRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Service class for conducting style transfer operations.
 */

@Service
@Getter
@RequiredArgsConstructor
public class StyleTransferService {
    // Dependencies
    private final UserService userService;
    private final EmailSerivce emailSerivce;
    private final StyleTransferRepository styleTransferRepository;
    private final ResourceLoader resourceLoader;
    private final PythonService pythonService;
    private final FileSystemStorageService fileService;





    /**
     * Method to conduct style transfer using Gatys algorithm.
     *
     * @param uuidRequest     The UUID of the request
     * @param originalImage   The original image file
     * @param styleImage      The style image file
     * @param optimizer       The optimizer for style transfer
     * @return                True if style transfer is successful, false otherwise
     */
    public boolean doStyleTransferGatys(User creatorOfRequest,UUID uuidRequest, MultipartFile originalImage, MultipartFile styleImage, String optimizer) {
        StyleTransfer createdStyleTransfer = new StyleTransfer();
        createdStyleTransfer.setUuidRequest(uuidRequest);
        createdStyleTransfer.setCreatedAt(LocalDateTime.now());

        // make unique names
        String  pathToOriginalImage, pathToStyleImage;
        try {
            pathToOriginalImage = fileService.makeUniqueFileNameAndTransfer(originalImage, "original", uuidRequest.toString());
            pathToStyleImage = fileService.makeUniqueFileNameAndTransfer(styleImage, "style", uuidRequest.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Files are empty or malicious");
        }

        createdStyleTransfer.setOriginalImageAbsolutePath(pathToOriginalImage);
        createdStyleTransfer.setStyleImageAbsolutePath(pathToStyleImage);
        createdStyleTransfer.setOptimizer(optimizer);

        // do style transfer
       if (pythonService.executePythonScript(createdStyleTransfer, fileService.getOutputImagePath()) != 0)
       {
           throw new RuntimeException("Python script execution error");
       }

       // save to database
        new Thread(() -> {
            createdStyleTransfer.setOptimizer(optimizer);
            createdStyleTransfer.setFinishedAt(LocalDateTime.now());
            createdStyleTransfer.setAlgorithmStyleTransferType(StyleTransferType.GATYS);

            String pathToOutputImage = fileService.getOutputImagePath() + File.separator +  uuidRequest + File.separator + optimizer +"-result.jpg";
            createdStyleTransfer.setCreatedImageAbsolutePath(pathToOutputImage);
            try {
                createdStyleTransfer.setCreatedImageInBytes(IOUtils.toByteArray(new FileInputStream(resourceLoader.getResource("file:" + pathToOutputImage).getFile())));
                createdStyleTransfer.setStyleImageInBytes(IOUtils.toByteArray(new FileInputStream(resourceLoader.getResource("file:" + pathToStyleImage).getFile())));
                createdStyleTransfer.setOriginalImageInBytes(IOUtils.toByteArray(new FileInputStream(resourceLoader.getResource("file:" + pathToOriginalImage).getFile())));


                createdStyleTransfer.setUser(creatorOfRequest);
            } catch (IOException e) {
                e.printStackTrace();
//            throw new IOException("Output file with name " + pathToOutputImage + " not found");
            }



            styleTransferRepository.save(createdStyleTransfer);
        }).start();

        return true;
    }

    public boolean hardDeleteStyleTransfer(UUID uuidRequest){
        styleTransferRepository.deleteByUuidRequest(uuidRequest);
        return true;
    }

    @Transactional
    public boolean softDeleteStyleTransfer(UUID uuidRequest){
        StyleTransfer styleTransfer = getStyleTransferByUuidRequest(uuidRequest);
        if (styleTransfer != null){
            styleTransfer.setIsDeleted(true);
            styleTransferRepository.save(styleTransfer);
            return true;
        }
        // not exist
        return false;
    }

    @Transactional
    public StyleTransfer getStyleTransferByUuidRequest(UUID uuidRequest){
        return styleTransferRepository.findByUuidRequest(uuidRequest).orElse(null);
    }

    @Transactional
    public List<StyleTransfer> getAllUserStyleTransfers (User user){
        if (user == null) throw new NullPointerException("User cannot be null");
        return styleTransferRepository.findAllByUserAndIsDeletedIsFalse(user);
    }


    public void sendResultInEmail(UUID uuidRequest, String userEmail) throws IOException {
        emailSerivce.sendEmailWithAttachment(userEmail, "Style Transfer Result", "Your Result", fileService.createZipResultFiles(uuidRequest));
    }



}

package com.example.transferstylerebuildmaven.services;

import com.example.transferstylerebuildmaven.models.Image.Image;
import com.example.transferstylerebuildmaven.models.Image.ImageExtension;
import com.example.transferstylerebuildmaven.models.Image.ImageType;
import com.example.transferstylerebuildmaven.models.style_transfer.StyleTransfer;
import com.example.transferstylerebuildmaven.models.style_transfer.StyleTransferType;
import com.example.transferstylerebuildmaven.models.user.User;
import com.example.transferstylerebuildmaven.repositories.ImageRepository;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final ImageRepository imageRepository;




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
        String pathToOriginalImage,  pathToStyleImage;
        try {
            pathToOriginalImage = fileService.isTransferedBefore(originalImage) ?
                    fileService.getTransferedImagePath(originalImage.getOriginalFilename()) :
                    fileService.makeUniqueFileNameAndTransfer(originalImage, "original", uuidRequest.toString());


            pathToStyleImage = fileService.isTransferedBefore(styleImage) ?
                    fileService.getTransferedImagePath(styleImage.getOriginalFilename()) :
                    fileService.makeUniqueFileNameAndTransfer(styleImage, "style", uuidRequest.toString());
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
            throw  new RuntimeException("Cannot find transfered multipart file");
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Files are empty or malicious");
        }

        HashMap<ImageType, String> pathsToImages = new HashMap<>();
        pathsToImages.put(ImageType.ORIGINAL, pathToOriginalImage);
        pathsToImages.put(ImageType.STYLE, pathToStyleImage);
        pathsToImages.put(ImageType.GENERATED, fileService.getOutputImagePath());


        // do style transfer
       if (pythonService.executePythonScript(pathsToImages, optimizer, uuidRequest) != 0)
       {
           throw new RuntimeException("Python script execution error");
       }

       // save to database
        new Thread(() -> {
            createdStyleTransfer.setOptimizer(optimizer);
            createdStyleTransfer.setFinishedAt(LocalDateTime.now());
            createdStyleTransfer.setAlgorithmStyleTransferType(StyleTransferType.GATYS);
            String pathToOutputImage = fileService.getOutputImagePath() + File.separator +  uuidRequest + File.separator + optimizer +"-result.jpg";
            createdStyleTransfer.setUser(creatorOfRequest);
            try {
                HashMap<ImageType, Image> images = new HashMap<>();
                initializeHashMapImages(images, ImageType.ORIGINAL, createdStyleTransfer, pathToOriginalImage);
                initializeHashMapImages(images, ImageType.STYLE, createdStyleTransfer, pathToStyleImage);
                initializeHashMapImages(images, ImageType.GENERATED, createdStyleTransfer, pathToOutputImage);
                createdStyleTransfer.setImages(images);
            } catch (IOException e) {
                e.printStackTrace();
//            throw new IOException("Output file with name " + pathToOutputImage + " not found");
            }
            styleTransferRepository.save(createdStyleTransfer);
        }).start();

        return true;
    }

    private void initializeHashMapImages( HashMap<ImageType, Image> images, ImageType imageType, StyleTransfer createdStyleTransfer, String pathToImage) throws IOException {
        images.put(imageType, Image.builder()
                .styleTransfer(createdStyleTransfer)
                .absolutePath(pathToImage)
                .imageName(FileSystemStorageService.extractFileNameFromPath(pathToImage))
                .imageExtension(ImageExtension.JPG)
                .imageInBytes(IOUtils.toByteArray(new FileInputStream(resourceLoader.getResource("file:" + pathToImage).getFile())))
                .imageType(imageType)
                .build());

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

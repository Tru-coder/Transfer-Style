package com.example.transferstylerebuildmaven.controllers.style_transfer;


import com.example.transferstylerebuildmaven.commons.RequestState;
import com.example.transferstylerebuildmaven.commons.StyleTransferProcessingState;
import com.example.transferstylerebuildmaven.models.Image.ImageType;
import com.example.transferstylerebuildmaven.models.user.User;
import com.example.transferstylerebuildmaven.respones.style_transfers.RequestStyleTransferResponse;
import com.example.transferstylerebuildmaven.services.StyleTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class StyleTransferController implements StyleTransferInterface {
    private final StyleTransferService styleTransferService;

    private final HashMap<UUID, StyleTransferProcessingState> requestsState
            = new HashMap<>();


    // todo: seperate on URL lock level methods
    // todo: add File Entity, Scroll Entity (Lenta)

    @RequestMapping(value = "auth/result/style/transfer/{uuidRequest}", method = RequestMethod.GET)
    public ResponseEntity<?> sendResultStyleTransferInEmail (@PathVariable("uuidRequest") UUID uuidRequest) throws IOException {
        styleTransferService.sendResultInEmail(uuidRequest, Objects.requireNonNull(getCurrentUser()).getEmail());
        return ResponseEntity.ok().body("Sent");
    }

    @RequestMapping(value = "auth/results/style/transfer", method = RequestMethod.GET)
    public ResponseEntity<?> getAllUserStyleTransfers ()  {
        return ResponseEntity.ok().body(styleTransferService.getAllUserStyleTransfers(getCurrentUser()).toString());
    }

    @Override
    public ResponseEntity<?> styleTransferGatys(
            @RequestParam(required = true, name = "original_image") MultipartFile originalImage,
            @RequestParam(required = true, name = "style_image") MultipartFile styleImage,
            @RequestParam(required = true, name = "optimizer", defaultValue = "lbfgs") String optimizer) {
        if  (!(optimizer.equals("lbfgs") || optimizer.equals("adam"))){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Unacceptable value of optimizer");
        }

        System.out.println(originalImage.getOriginalFilename());
        System.out.println(originalImage.getName());

        UUID uuidRequest = UUID.randomUUID();
        User currentUser = getCurrentUser();
        new Thread(() -> {
            requestsState.put(uuidRequest,
                    new StyleTransferProcessingState(RequestState.Processing,
                            "Image processing initialized"));

            if ( styleTransferService.doStyleTransferGatys(currentUser, uuidRequest, originalImage, styleImage, optimizer)){
                requestsState.put(uuidRequest,
                        new StyleTransferProcessingState(RequestState.Done,
                                "Image processing finished"));
            }
            else{
                requestsState.put(uuidRequest,
                        new StyleTransferProcessingState(RequestState.Error,
                                "Error has happened during image processing"));
            }
        }).start();


        return  ResponseEntity.status(HttpStatus.ACCEPTED).
                body(new RequestStyleTransferResponse(
                        "Image processing initialized. UUID request is "
                                + uuidRequest +
                                "\n" + "To know image generation state look here ...", uuidRequest));
    }

    @Override
    public StyleTransferProcessingState getRequestState(@PathVariable("uuidRequest") UUID uuidRequest){
        requestsState.forEach((key, value) -> System.out.println(key + " " + value));
        return  requestsState.getOrDefault(uuidRequest,
                new StyleTransferProcessingState(RequestState.Error, "This request is not exists"));
    }


    @RequestMapping(value = "/style/transfer/{uuidRequest}")
    public ResponseEntity<?> getStyleTransfer(@PathVariable("uuidRequest") UUID uuidRequest){


        return ResponseEntity.status(HttpStatus.OK).body( styleTransferService.getStyleTransferByUuidRequest(uuidRequest).toString());
    }



    @Override
    public ResponseEntity<?> getResultImageByUuid(@PathVariable("uuidRequest") UUID uuidRequest){

        byte[] createdImage = styleTransferService.getStyleTransferByUuidRequest(uuidRequest).getImages().get(ImageType.GENERATED).getImageInBytes();
        return ResponseEntity.status(HttpStatus.OK).body(createdImage);
    }
    @Override
    public ResponseEntity<?> deleteStyleTransferByUuid(@PathVariable("uuidRequest") UUID uuidRequest){

        return ResponseEntity.status(HttpStatus.OK).body(styleTransferService.softDeleteStyleTransfer(uuidRequest));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails){
            return (User) authentication.getPrincipal();
        }
        else {
            // Handle the case where the principal is not available or is not of type UserDetails
            return null;
        }
    }
}

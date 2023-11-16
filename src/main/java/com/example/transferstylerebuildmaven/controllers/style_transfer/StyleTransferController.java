package com.example.transferstylerebuildmaven.controllers.style_transfer;


import com.example.transferstylerebuildmaven.commons.RequestState;
import com.example.transferstylerebuildmaven.commons.StyleTransferProcessingState;
import com.example.transferstylerebuildmaven.respones.style_transfers.RequestStyleTransferResponse;
import com.example.transferstylerebuildmaven.services.StyleTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class StyleTransferController implements StyleTransferInterface {
    private final StyleTransferService styleTransferService;

    private final HashMap<UUID, StyleTransferProcessingState> requestsState
            = new HashMap<UUID, StyleTransferProcessingState>();


    @RequestMapping(value = "auth/result/style/transfer/{uuidRequest}", method = RequestMethod.GET)
    public ResponseEntity<?> sendResultStyleTransferInEmail (@PathVariable("uuidRequest") UUID uuidRequest, Principal principal) throws IOException {
        styleTransferService.sendResultInEmail(uuidRequest, principal.getName());
        return ResponseEntity.ok().body("Sent");
    }


    @Override
    public ResponseEntity<?> styleTransferGatys(
            @RequestParam(required = true, name = "original_image") MultipartFile originalImage,
            @RequestParam(required = true, name = "style_image") MultipartFile styleImage,
            @RequestParam(required = true, name = "optimizer", defaultValue = "lbfgs") String optimizer) {
        if  (!(optimizer.equals("lbfgs") || optimizer.equals("adam"))){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Unacceptable value of optimizer");
        }

        UUID uuidRequest = UUID.randomUUID();
        new Thread(() -> {
            requestsState.put(uuidRequest,
                    new StyleTransferProcessingState(RequestState.Processing,
                            "Image processing initialized"));

            if ( styleTransferService.doStyleTransferGatys(uuidRequest, originalImage, styleImage, optimizer)){
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



    @Override
    public ResponseEntity<?> downloadAllResultFilesFromDisk(@PathVariable("uuidRequest") UUID uuidRequest) throws IOException {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename= Result_files.zip")
                .body(styleTransferService.getResultFilesInByteArrayStream(uuidRequest).toByteArray());
    }


    @Override
    public ResponseEntity<?> getResultImageByUuid(@PathVariable("uuidRequest") UUID uuidRequest){

        byte[] createdImage = styleTransferService.getStyleTransferByUuidRequest(uuidRequest).getCreatedImageInBytes();
        return ResponseEntity.status(HttpStatus.OK).body(createdImage);
    }
    @Override
    public ResponseEntity<?> deleteStyleTransferByUuid(@PathVariable("uuidRequest") UUID uuidRequest){

        return ResponseEntity.status(HttpStatus.OK).body(styleTransferService.softDeleteStyleTransfer(uuidRequest));
    }
}

package com.example.transferstylerebuildmaven.controllers.style_transfer;


import com.example.transferstylerebuildmaven.respones.style_transfers.RequestStyleTransferResponse;
import com.example.transferstylerebuildmaven.commons.RequestState;
import com.example.transferstylerebuildmaven.commons.StyleTransferProcessingState;
import com.example.transferstylerebuildmaven.services.StyleTransferService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class StyleTransferController implements StyleTransferInterface {
    private final StyleTransferService styleTransferService;

    private final HashMap<UUID, StyleTransferProcessingState> requestsState
            = new HashMap<UUID, StyleTransferProcessingState>();



    // todo: rewrite error handling
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


    public StyleTransferProcessingState getRequestState(@PathVariable("uuidRequest") UUID uuidRequest){
        requestsState.forEach((key, value) -> System.out.println(key + " " + value));
        return  requestsState.getOrDefault(uuidRequest,
                new StyleTransferProcessingState(RequestState.Error, "This request is not exists"));
    }



    // todo: check on UUID type matching (not malicious)
    // todo: add folder scanning and sending in email
    public ResponseEntity<?> downloadAllResultFilesFromDisk(@PathVariable("uuidRequest") UUID uuidRequest) throws IOException {
        File f = new File(styleTransferService.getOutputImagePath() + "\\"+uuidRequest);
        List<File> files = new ArrayList<File>(Arrays.asList(Objects.requireNonNull(f.listFiles())));

        // Create an output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Create a ZipOutputStream and pass the output stream to it
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        // Package files
        Set<String> fileNameAdded = new HashSet<>();

        for (File file : files) {
            // New zip entry and copying input stream with file, after that close stream
            if (!fileNameAdded.contains(file.getName())) {
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream fileInputStream = new FileInputStream(file);
                IOUtils.copy(fileInputStream, zipOutputStream);

                fileInputStream.close();
                zipOutputStream.closeEntry();
                fileNameAdded.add(file.getName());
            }
        }
        zipOutputStream.close();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename= Result_files.zip")
                .body(outputStream.toByteArray());
    }



    public ResponseEntity<?> getResultImageByUuid(@PathVariable("uuidRequest") UUID uuidRequest){

        byte[] createdImage = styleTransferService.getStyleTransferByUuidRequest(uuidRequest).getCreatedImageInBytes();
        return ResponseEntity.status(HttpStatus.OK).body(createdImage);
    }

    public ResponseEntity<?> deleteStyleTransferByUuid(@PathVariable("uuidRequest") UUID uuidRequest){

        return ResponseEntity.status(HttpStatus.OK).body(styleTransferService.softDeleteStyleTransfer(uuidRequest));
    }
}

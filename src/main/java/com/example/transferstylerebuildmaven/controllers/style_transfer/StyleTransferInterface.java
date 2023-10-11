package com.example.transferstylerebuildmaven.controllers.style_transfer;

import com.example.transferstylerebuildmaven.commons.StyleTransferProcessingState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@Tag(name = "Style Transfer Controller")
public interface StyleTransferInterface {


    @Operation(
            description = "Transfer style from style image to original image",
            summary = "Generate original image with specific style",
            responses = {
                    @ApiResponse(
                            description = "Image processing initialized",
                            responseCode = "202"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error" ,
                            responseCode = "500"
                    )
            },
            parameters = {
                    @Parameter(name = "original image", required = true,  description = "image to process"),
                    @Parameter(name = "style image", required = true,  description = "take style from this image"),
                    @Parameter(name = "optimizer", required = true,  description = "change way of processing image")
            }

    )
    @RequestMapping(value = "/style/transfer/image/gatys", method = RequestMethod.POST)
    ResponseEntity<?> styleTransferGatys(
            @RequestParam(required = true, name = "original_image") MultipartFile originalImage,
            @RequestParam(required = true, name = "style_image") MultipartFile styleImage,
            @RequestParam(required = true, name = "optimizer", defaultValue = "lbfgs") String optimizer);





    @Operation(
            description = "Get current state of request",
            summary = "Current state of request",
            responses = {
                    @ApiResponse(
                            description = "State description",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error" ,
                            responseCode = "500"
                    )
            }
    )
    @RequestMapping(value = "/style/transfer/image/gatys/{uuidRequest}", method = RequestMethod.GET)
    StyleTransferProcessingState getRequestState(@PathVariable("uuidRequest") UUID uuidRequest);


    @Operation(
            description = "Download all result files from disk in zip-file",
            summary = "zip-file",
            responses = {
                    @ApiResponse(
                            description = "Download result",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error" ,
                            responseCode = "500"
                    )
            }
    )

    @RequestMapping(value = "/style/transfer/image/gatys/download/disk/{uuidRequest}", method = RequestMethod.GET)
    ResponseEntity<?> downloadAllResultFilesFromDisk(@PathVariable("uuidRequest") UUID uuidRequest) throws IOException;


    @Operation(
            description = "Returns a result image from db in jpg",
            summary = "Image in jpg",
            responses = {
                    @ApiResponse(
                            description = "Image in jpg",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error" ,
                            responseCode = "500"
                    )
            }
    )
    @RequestMapping(value = "/style/transfer/image/gatys/download/database/{uuidRequest}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    ResponseEntity<?> getResultImageByUuid(@PathVariable("uuidRequest") UUID uuidRequest);


    @Operation(
            description = "delete request of style transfer and its result from db",
            summary = "deleted style transfer",
            responses = {
                    @ApiResponse(
                            description = "deleted result",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error" ,
                            responseCode = "500"
                    )
            }
    )
    @RequestMapping(value = "/style/transfer/image/gatys/delete/database/{uuidRequest}", method = RequestMethod.DELETE)
    ResponseEntity<?> deleteStyleTransferByUuid(@PathVariable("uuidRequest") UUID uuidRequest);

}

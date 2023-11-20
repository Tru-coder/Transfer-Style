package com.example.transferstylerebuildmaven.controllers.files;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.UUID;

@Tag(name = "File controller", description = "Files management. Controller works with disk directories")
@RequestMapping("/api/file/")
public interface FileControllerInterface {

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
}

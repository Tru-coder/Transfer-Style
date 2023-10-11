package com.example.transferstylerebuildmaven.services;

import com.example.transferstylerebuildmaven.models.StyleTransfer;
import com.example.transferstylerebuildmaven.repositories.StyleTransferRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;


@Service
@Getter
@RequiredArgsConstructor
public class StyleTransferService {
    private final StyleTransferRepository styleTransferRepository;
    private final ResourceLoader resourceLoader;

    @Value("${upload.image.path}")// path from properties for file
    private String uploadImagePath;

    @Value("${output.image.path}") // path from properties for file
    private String outputImagePath;

    @Value("${python.script.style.transfer.gatys.path}")
    private String pythonScriptStyleTransferGatysPath;

    @Value("${python.script.style.transfer.venv.path}")
    private String pythonVenvStyleTransferPath;

    private String makeUniqueFileNameAndTransfer(MultipartFile original, String description, String requestUUID) throws IOException {
        if (original != null && !Objects.requireNonNull(original.getOriginalFilename()).isEmpty()){
            String uploadDirectory = uploadImagePath + "\\" + requestUUID;
            File uploadDir = new File(uploadDirectory);

            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String resultFilename = description + "_" + original.getOriginalFilename();
            original.transferTo(new File(uploadDir + "/" + resultFilename));

            return uploadDirectory + "\\" + resultFilename;
        }
        throw new RuntimeException("File " + original.getOriginalFilename() + "is empty or malicious");
    }


    private int executePythonScript(UUID uuidRequest, String uniqueOriginalImage, String uniqueStyleImage, String optimizer){
        String parametersString =
                "--content_img_name " + uniqueOriginalImage + " "
                        + "--style_img_name " + uniqueStyleImage + " "
                        + "--optimizer " + optimizer + " "
                        + "--location_input_folder " + " " + uploadImagePath + "\\" + uuidRequest + " "
                        + "--location_output_folder" + " " + outputImagePath + "\\" +  uuidRequest;

        String executionLine = pythonVenvStyleTransferPath + " "  + pythonScriptStyleTransferGatysPath +
                " " + parametersString;

        CommandLine cmdLine = CommandLine.parse(executionLine);

        System.out.println(parametersString);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        int exitCode = 0;
        try {
            exitCode = executor.execute(cmdLine);
        } catch (IOException e) {
            String output = outputStream.toString();
            System.out.println(output);
            e.printStackTrace();
            throw new RuntimeException("Style transfer cannot be perform due to python error", e);
        }

        String output = outputStream.toString();

        System.out.println(output);
        System.out.println(exitCode);

        return exitCode;

    }

    public boolean doStyleTransferGatys(UUID uuidRequest, MultipartFile originalImage, MultipartFile styleImage, String optimizer) {
        String  uniqueOriginalImage, uniqueStyleImage;
        StyleTransfer createdStyleTransfer = new StyleTransfer();
        createdStyleTransfer.setUuidRequest(uuidRequest);
        createdStyleTransfer.setCreatedAt(LocalDateTime.now());

        // make unique names
        try {
            uniqueOriginalImage = makeUniqueFileNameAndTransfer(originalImage, "original", uuidRequest.toString());
            uniqueStyleImage = makeUniqueFileNameAndTransfer(styleImage, "style", uuidRequest.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Files are empty or malicious");
        }

        // do style transfer
       if ( executePythonScript(uuidRequest, uniqueOriginalImage, uniqueStyleImage, optimizer) != 0) throw new RuntimeException("Python script execution error");

       // save to database
        new Thread(() -> {
            createdStyleTransfer.setOptimizer(optimizer);
            createdStyleTransfer.setFinishedAt(LocalDateTime.now());
            createdStyleTransfer.setAlgorithmStyleTransferType("Gatys");

            String pathToOutputImage = outputImagePath + "\\" +  uuidRequest + "\\" + optimizer +"-result.jpg";
            String pathToOriginalImage =  uniqueOriginalImage;
            String pathToStyleImage = uniqueStyleImage;

            createdStyleTransfer.setCreatedImageAbsolutePath(pathToOutputImage);
            createdStyleTransfer.setOriginalImageAbsolutePath(pathToOriginalImage);
            createdStyleTransfer.setStyleImageAbsolutePath(pathToStyleImage);

            try {
                createdStyleTransfer.setCreatedImageInBytes(IOUtils.toByteArray(new FileInputStream(resourceLoader.getResource("file:" + pathToOutputImage).getFile())));
                createdStyleTransfer.setStyleImageInBytes(IOUtils.toByteArray(new FileInputStream(resourceLoader.getResource("file:" + pathToStyleImage).getFile())));
                createdStyleTransfer.setOriginalImageInBytes(IOUtils.toByteArray(new FileInputStream(resourceLoader.getResource("file:" + pathToOriginalImage).getFile())));
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

}

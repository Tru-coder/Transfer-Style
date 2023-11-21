package com.example.transferstylerebuildmaven.services;

import com.example.transferstylerebuildmaven.models.Image.ImageType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PythonService {
    @Value("${python.script.style.transfer.gatys.path}")
    private String pythonScriptStyleTransferGatysPath;

    @Value("${python.script.style.transfer.venv.path}")
    private String pythonVenvStyleTransferPath;

    public int executePythonScript(HashMap<ImageType, String> pathToImages, String optimizer, UUID uuidRequest){
        String parametersString =
                "--content_img_name " + pathToImages.get(ImageType.ORIGINAL) + " "
                        + "--style_img_name " + pathToImages.get(ImageType.STYLE) + " "
                        + "--optimizer " + optimizer + " "
                        + "--location_output_folder "  + pathToImages.get(ImageType.GENERATED) + File.separator + uuidRequest;

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
}

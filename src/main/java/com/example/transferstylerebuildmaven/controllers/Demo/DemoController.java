package com.example.transferstylerebuildmaven.controllers.Demo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/")
public class DemoController {

    @GetMapping("/auth/demo-controller")
    public ResponseEntity<?> sayNonSecuredHello(){
        return ResponseEntity.ok("Hello from non-secured end-point");
    }

    @GetMapping("/non-auth/demo-controller")
    public ResponseEntity<?> saySecuredHello(){
        return ResponseEntity.ok("Hello from secured end-point");
    }

    @GetMapping("/demo-controller/info")
    public String userData(Principal principal) {


        return
                principal.getName();
    }


}

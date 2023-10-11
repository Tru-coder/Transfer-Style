package com.example.transferstylerebuildmaven.controllers.auth;


import com.example.transferstylerebuildmaven.requests.AuthenticationRequest;
import com.example.transferstylerebuildmaven.respones.AuthenticationResponse;
import com.example.transferstylerebuildmaven.requests.RegisterRequest;
import com.example.transferstylerebuildmaven.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ){

        return ResponseEntity.ok(authenticationService.register(request));
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<AuthenticationResponse>  authenticate(
            @RequestBody
            AuthenticationRequest request
    ){

        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @RequestMapping(value = "/refresh-token", method = RequestMethod.POST)
    public void refreshToken(HttpServletRequest request, HttpServletResponse response){
        authenticationService.refreshToken(request, response);
    }

}

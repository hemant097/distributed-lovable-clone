package com.example.distribute_lovable_clone.account_service.controller;

import com.example.distribute_lovable_clone.account_service.dto.auth.AuthResponse;
import com.example.distribute_lovable_clone.account_service.dto.auth.LoginRequest;
import com.example.distribute_lovable_clone.account_service.dto.auth.SignUpRequest;
import com.example.distribute_lovable_clone.account_service.dto.auth.UserProfileResponse;
import com.example.distribute_lovable_clone.account_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
//    private final UserService userService;
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUpRequest(@RequestBody SignUpRequest signUpRequest){

        return ResponseEntity.ok(authService.signUp(signUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginRequest(@RequestBody LoginRequest loginRequest){

        return ResponseEntity.ok(authService.login(loginRequest));
    }

//    @GetMapping("/me")
//    public ResponseEntity<UserProfileResponse> getProfile(){
//        Long userId = 1L;
//        return ResponseEntity.ok(userService.getProfile(userId));
//    } TODO
}

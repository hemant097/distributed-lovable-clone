package com.example.distributed_lovable_clone.account_service.service;


import com.example.distributed_lovable_clone.account_service.dto.auth.AuthResponse;
import com.example.distributed_lovable_clone.account_service.dto.auth.LoginRequest;
import com.example.distributed_lovable_clone.account_service.dto.auth.SignUpRequest;

public interface AuthService {
    AuthResponse signUp(SignUpRequest signUpRequest);

    AuthResponse login(LoginRequest loginRequest);
}

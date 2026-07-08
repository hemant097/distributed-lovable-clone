package com.example.distributed_lovable_clone.account_service.service.impl;

import com.example.distributed_lovable_clone.account_service.dto.auth.AuthResponse;
import com.example.distributed_lovable_clone.account_service.dto.auth.LoginRequest;
import com.example.distributed_lovable_clone.account_service.dto.auth.SignUpRequest;
import com.example.distributed_lovable_clone.account_service.entity.User;
import com.example.distributed_lovable_clone.account_service.mapper.UserMapper;
import com.example.distributed_lovable_clone.account_service.repository.UserRepository;
import com.example.distributed_lovable_clone.account_service.service.AuthService;
import com.example.distributed_lovable_clone.commonlib.errors.BadRequestException;
import com.example.distributed_lovable_clone.commonlib.security.AuthUtil;
import com.example.distributed_lovable_clone.commonlib.security.JwtUserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepo;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    AuthUtil authUtil;
    AuthenticationManager authenticationManager;

    @Override
    public AuthResponse signUp(SignUpRequest request) {
        userRepo.findByUsername(request.username())
                .ifPresent(user ->{
            throw new BadRequestException("user already exists with username: "+request.username());
        });

        User user = userMapper.toUserEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepo.save(user);

        JwtUserPrincipal jwtUserPrincipal = new JwtUserPrincipal(user.getId(),user.getName(),
                user.getUsername(),null, List.of());
        String token = authUtil.generateAccessToken(jwtUserPrincipal);

        return new AuthResponse(token, userMapper.toUserProfileResponse(jwtUserPrincipal));
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        JwtUserPrincipal user = (JwtUserPrincipal) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(user);

        return new AuthResponse(token, userMapper.toUserProfileResponse(user));


    }
}

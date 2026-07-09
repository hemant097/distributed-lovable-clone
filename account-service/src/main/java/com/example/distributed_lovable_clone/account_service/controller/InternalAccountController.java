package com.example.distributed_lovable_clone.account_service.controller;

import com.example.distributed_lovable_clone.account_service.mapper.UserMapper;
import com.example.distributed_lovable_clone.account_service.repository.UserRepository;
import com.example.distributed_lovable_clone.account_service.service.SubscriptionService;
import com.example.distributed_lovable_clone.common_lib.dto.PlanDto;
import com.example.distributed_lovable_clone.common_lib.dto.UserDto;
import com.example.distributed_lovable_clone.common_lib.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class InternalAccountController {
    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final SubscriptionService subscriptionService;

    @GetMapping("/users/id")
    public UserDto getUserById(@PathVariable Long id){
        return userRepo.findById(id)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("user",id.toString()));
    }

    @GetMapping("/users/by-email")
    public Optional<UserDto> getUserByEmail(@RequestParam String email){
        return userRepo.findByUsernameIgnoreCase(email)
                .map(userMapper::toUserDto);
    }

    @GetMapping("/billing/current-plan")
    public PlanDto getCurrentSubscription(){
        return subscriptionService.getCurrentSubscribedPlanByUser();
    }
}

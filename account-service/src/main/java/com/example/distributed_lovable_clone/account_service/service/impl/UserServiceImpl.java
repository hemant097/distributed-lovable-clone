package com.example.distributed_lovable_clone.account_service.service.impl;

import com.example.distributed_lovable_clone.account_service.entity.User;
import com.example.distributed_lovable_clone.account_service.repository.UserRepository;
import com.example.distributed_lovable_clone.common_lib.errors.ResourceNotFoundException;
import com.example.distributed_lovable_clone.common_lib.security.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =  userRepo.findByUsername(username)
                .orElseThrow( () -> new ResourceNotFoundException("user",username));

        return new JwtUserPrincipal(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getPassword(),
                List.of());
    }


}
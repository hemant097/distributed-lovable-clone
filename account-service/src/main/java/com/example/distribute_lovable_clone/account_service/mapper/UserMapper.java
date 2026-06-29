package com.example.distribute_lovable_clone.account_service.mapper;

import com.example.distribute_lovable_clone.account_service.dto.auth.SignUpRequest;
import com.example.distribute_lovable_clone.account_service.dto.auth.UserProfileResponse;
import com.example.distribute_lovable_clone.account_service.entity.User;
import com.example.distributelovableclone.commonlib.dto.UserDto;
import com.example.distributelovableclone.commonlib.security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUserEntity(SignUpRequest signUpRequest);

    @Mapping(source = "userId",target = "id")
    UserProfileResponse toUserProfileResponse(JwtUserPrincipal jwtUserPrincipal);

    UserDto toUserDto(User user);
}

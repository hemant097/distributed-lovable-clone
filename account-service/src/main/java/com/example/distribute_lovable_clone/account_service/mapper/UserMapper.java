package com.example.distribute_lovable_clone.account_service.mapper;

import com.example.distribute_lovable_clone.account_service.dto.auth.SignUpRequest;
import com.example.distribute_lovable_clone.account_service.dto.auth.UserProfileResponse;
import com.example.distribute_lovable_clone.account_service.entity.User;
import com.example.distribute_lovable_clone.common_lib.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUserEntity(SignUpRequest signUpRequest);

    UserProfileResponse toUserProfileResponse(User user);

    UserDto toUserDto(User user);
}

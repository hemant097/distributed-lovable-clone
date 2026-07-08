package com.example.distributed_lovable_clone.account_service.mapper;

import com.example.distributed_lovable_clone.account_service.dto.auth.SignUpRequest;
import com.example.distributed_lovable_clone.account_service.dto.auth.UserProfileResponse;
import com.example.distributed_lovable_clone.account_service.entity.User;
import com.example.distributed_lovable_clone.commonlib.dto.UserDto;
import com.example.distributed_lovable_clone.commonlib.security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUserEntity(SignUpRequest signUpRequest);

    @Mapping(source = "userId",target = "id")
    UserProfileResponse toUserProfileResponse(JwtUserPrincipal jwtUserPrincipal);

    UserDto toUserDto(User user);
}

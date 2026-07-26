package com.sss.app.mapper.signup;

import com.sss.app.dto.signup.SignupCreateRequestDTO;
import com.sss.app.dto.signup.SignupResponseDTO;
import com.sss.app.entity.users.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SignupMapper {

    @Mapping(target = "contact_number", source = "mobileNumber")
    @Mapping(target = "name", source = "email")
    User toEntity(SignupCreateRequestDTO dto);

    @Mapping(target = "mobileNumber", source = "contact_number")
    SignupResponseDTO toResponse(User entity);
}

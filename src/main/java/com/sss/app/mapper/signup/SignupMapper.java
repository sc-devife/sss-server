package com.sss.app.mapper.signup;

import com.sss.app.dto.signup.SignupCreateRequestDTO;
import com.sss.app.dto.signup.SignupResponseDTO;
import com.sss.app.entity.signup.Signup;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SignupMapper {
    Signup toEntity(SignupCreateRequestDTO dto);

    SignupResponseDTO toResponse(Signup entity);
}

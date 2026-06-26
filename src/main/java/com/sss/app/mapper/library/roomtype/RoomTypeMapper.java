package com.sss.app.mapper.library.roomtype;

import com.sss.app.dto.library.roomtype.RoomTypeCreateRequestDTO;
import com.sss.app.dto.library.roomtype.RoomTypeResponseDTO;
import com.sss.app.dto.library.roomtype.RoomTypeUpdateRequestDTO;
import com.sss.app.entity.library.roomtype.RoomType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RoomTypeMapper {

    RoomType toEntityCreate(RoomTypeCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(RoomTypeUpdateRequestDTO dto, @MappingTarget RoomType entity);

    RoomTypeResponseDTO toResponse(RoomType entity);
}

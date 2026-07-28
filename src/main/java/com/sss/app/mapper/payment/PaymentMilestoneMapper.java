package com.sss.app.mapper.payment;

import com.sss.app.dto.payment.PaymentMilestoneResponseDTO;
import com.sss.app.entity.payment.PaymentMilestone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMilestoneMapper {

    @Mapping(target = "dealUid", source = "deal.uid")
    PaymentMilestoneResponseDTO toResponse(PaymentMilestone entity);
}

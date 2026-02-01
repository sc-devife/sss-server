package com.sss.app.dto.traveller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TravellerResponseDTO extends TravellerDTO{
    private Long seqp;
}

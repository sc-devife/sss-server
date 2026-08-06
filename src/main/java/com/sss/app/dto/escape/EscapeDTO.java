package com.sss.app.dto.escape;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.dto.traveller.TravellerResponseDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EscapeDTO {

    // ✅ Full objects
    private LeadResponseDTO lead;

    private List<TravellerResponseDTO> travellers;

    private List<EscapePointResponseDto> escapePoints;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    private Integer numberOfDays;
    private String status;
}

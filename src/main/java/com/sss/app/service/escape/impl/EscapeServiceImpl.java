package com.sss.app.service.escape.impl;

import com.sss.app.dto.escape.EscapeCreateRequestDTO;
import com.sss.app.dto.escape.EscapeResponseDTO;
import com.sss.app.dto.escape.EscapeUpdateRequestDTO;
import com.sss.app.dto.lead.LeadResponseDTO;
import com.sss.app.entity.escape.Escape;
import com.sss.app.entity.lead.Lead;
import com.sss.app.exception.NotFoundException;
import com.sss.app.helper.escape.EscapeHelper;
import com.sss.app.helper.traveller.TravellerHelper;
import com.sss.app.mapper.escape.EscapeMapper;
import com.sss.app.repository.escape.EscapeRepository;
import com.sss.app.repository.lead.LeadRepository;
import com.sss.app.repository.library.escapepoint.EscapePointRepository;
import com.sss.app.repository.traveller.TravellerRepository;
import com.sss.app.service.escape.EscapeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EscapeServiceImpl implements EscapeService {

   // private final EscapeRepository tripRepository;
    private final EscapeMapper tripMapper;
   // private final LeadRepository leadRepository;
   // private final TravellerRepository travellerRepository;
    //private final EscapePointRepository destinationRepository;
    private final EscapeHelper escapeHelper;

    @Override
    public EscapeResponseDTO createEscape(EscapeCreateRequestDTO request) {

        return tripMapper.toResponse(escapeHelper.createEscape(request));
    }

    @Override
    public EscapeResponseDTO updateEscape(Long seqp, EscapeUpdateRequestDTO request)
    {
        return tripMapper.toResponse(escapeHelper.updateEscape(seqp, request));
    }


    @Override
    public EscapeResponseDTO getTripById(Long id) {
        return tripMapper.toResponse(escapeHelper.getEscapeById(id));
    }

    @Override
    public void deleteEscape(Long seqp) {
        escapeHelper.deleteEscape(seqp);
    }

}

package com.sss.app.service.taxprofile.impl;

import com.sss.app.dto.taxprofile.TaxProfileCreateRequestDTO;
import com.sss.app.dto.taxprofile.TaxProfileResponseDTO;
import com.sss.app.dto.taxprofile.TaxProfileUpdateRequestDTO;
import com.sss.app.helper.taxprofile.TaxProfileHelper;
import com.sss.app.mapper.taxprofile.TaxProfileMapper;
import com.sss.app.service.taxprofile.TaxProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxProfileServiceImpl implements TaxProfileService {

    private final TaxProfileHelper taxProfileHelper;
    private final TaxProfileMapper taxProfileMapper;

    @Override
    public TaxProfileResponseDTO create(TaxProfileCreateRequestDTO request) {
        return taxProfileMapper.toResponse(taxProfileHelper.create(request));
    }

    @Override
    public TaxProfileResponseDTO getByUid(UUID uid) {
        return taxProfileMapper.toResponse(taxProfileHelper.getByUid(uid));
    }

    @Override
    public List<TaxProfileResponseDTO> getAllForOrg() {
        return taxProfileHelper.getAllForOrg().stream().map(taxProfileMapper::toResponse).toList();
    }

    @Override
    public TaxProfileResponseDTO update(UUID uid, TaxProfileUpdateRequestDTO request) {
        return taxProfileMapper.toResponse(taxProfileHelper.update(uid, request));
    }

    @Override
    public void deactivate(UUID uid) {
        taxProfileHelper.deactivate(uid);
    }
}

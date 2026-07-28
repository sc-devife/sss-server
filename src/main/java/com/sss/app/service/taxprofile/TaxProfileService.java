package com.sss.app.service.taxprofile;

import com.sss.app.dto.taxprofile.TaxProfileCreateRequestDTO;
import com.sss.app.dto.taxprofile.TaxProfileResponseDTO;
import com.sss.app.dto.taxprofile.TaxProfileUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface TaxProfileService {
    TaxProfileResponseDTO create(TaxProfileCreateRequestDTO request);
    TaxProfileResponseDTO getByUid(UUID uid);
    List<TaxProfileResponseDTO> getAllForOrg();
    TaxProfileResponseDTO update(UUID uid, TaxProfileUpdateRequestDTO request);
    void deactivate(UUID uid);
}

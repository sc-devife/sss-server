package com.sss.app.helper.taxprofile;

import com.sss.app.dto.taxprofile.TaxProfileCreateRequestDTO;
import com.sss.app.dto.taxprofile.TaxProfileUpdateRequestDTO;
import com.sss.app.entity.taxprofile.TaxProfile;
import com.sss.app.entity.users.User;
import com.sss.app.exception.NotFoundException;
import com.sss.app.mapper.taxprofile.TaxProfileMapper;
import com.sss.app.repository.taxprofile.TaxProfileRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaxProfileHelper {

    private final TaxProfileRepository taxProfileRepository;
    private final TaxProfileMapper taxProfileMapper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public TaxProfile create(TaxProfileCreateRequestDTO request) {
        TaxProfile taxProfile = taxProfileMapper.toEntityCreate(request);
        taxProfile.setOrgId(currentUser().getOrgId());
        return taxProfileRepository.save(taxProfile);
    }

    public TaxProfile getByUid(UUID uid) {
        TaxProfile taxProfile = taxProfileRepository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Tax profile not found"));
        orgAccessGuard.requireAccessToOrg(taxProfile.getOrgId());
        return taxProfile;
    }

    public List<TaxProfile> getAllForOrg() {
        return taxProfileRepository.findAllByOrgId(currentUser().getOrgId());
    }

    public TaxProfile update(UUID uid, TaxProfileUpdateRequestDTO request) {
        TaxProfile taxProfile = getByUid(uid);
        taxProfileMapper.updateEntityFromDto(request, taxProfile);
        return taxProfileRepository.save(taxProfile);
    }

    public void deactivate(UUID uid) {
        TaxProfile taxProfile = getByUid(uid);
        taxProfile.setStatus("inactive");
        taxProfileRepository.save(taxProfile);
    }
}

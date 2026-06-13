package com.sss.app.service.library.escapesource;

import com.sss.app.dto.library.escapesource.EscapeSourceRequestDTO;
import com.sss.app.dto.library.escapesource.EscapeSourceResponseDTO;
//import org.springframework.data.domain.Page;

//import java.awt.print.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.List;

public interface EscapeSourceService {

    EscapeSourceResponseDTO createEscapeSource(EscapeSourceRequestDTO dto);
    EscapeSourceResponseDTO updateEscapeSource(Long id, EscapeSourceRequestDTO dto);
    EscapeSourceResponseDTO getSourceById(Long id);
    List<EscapeSourceResponseDTO> getAllSources();
    Page<EscapeSourceResponseDTO> getSources(Pageable pageable);

    // EscapeSourceResponseDTO getSourceById(Long id);

  //  List<EscapeSourceResponseDTO> getAllSources();

    //EscapeSourceResponseDTO disableSource(Long id);
}

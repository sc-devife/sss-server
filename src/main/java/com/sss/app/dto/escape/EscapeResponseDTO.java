package com.sss.app.dto.escape;

import com.sss.app.dto.library.escapesource.EscapeSourceResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EscapeResponseDTO extends EscapeDTO {
    private Long seqp;
    private EscapeSourceResponseDTO source;
}

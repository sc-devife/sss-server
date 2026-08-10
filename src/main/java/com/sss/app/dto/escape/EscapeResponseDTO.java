package com.sss.app.dto.escape;

import com.sss.app.dto.library.escapesource.EscapeSourceResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EscapeResponseDTO extends EscapeDTO {
    private UUID uid;
    private EscapeSourceResponseDTO source;
}

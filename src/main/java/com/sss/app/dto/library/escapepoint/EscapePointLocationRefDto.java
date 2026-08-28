package com.sss.app.dto.library.escapepoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscapePointLocationRefDto {
    private UUID uid;
    private String city;
    private String state;
    private String country;
    private String displayName;

    // Lombok's boolean getter for a field already prefixed "is" is isPrimary()
    // (not isIsPrimary()) — Jackson then strips that "is" itself when deriving
    // the JSON property name, silently emitting "primary" instead of
    // "isPrimary" unless pinned explicitly here.
    @JsonProperty("isPrimary")
    private boolean isPrimary;
}

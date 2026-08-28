package com.sss.app.service.library.escapepoint;

import com.sss.app.dto.library.escapepoint.EscapePointLocationRefDto;
import com.sss.app.dto.library.escapepoint.EscapePointResponseDto;
import com.sss.app.entity.library.escapepoint.EscapePointLocation;
import com.sss.app.repository.library.escapepoint.EscapePointLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Shared by EscapePointsServiceImpl (resolving an EscapePoint's own response)
// and EscapeServiceImpl (resolving EscapePoints nested inside an Escape
// response) — both need the same locations/locationLabel resolution, batched
// across however many EscapePoints are in play in one call.
@Component
@RequiredArgsConstructor
public class EscapePointLocationResolver {

    private final EscapePointLocationRepository escapePointLocationRepository;

    /** Populates locations + locationLabel on every dto, matched to entities by list position. */
    public void resolve(List<Long> escapePointSeqps, List<EscapePointResponseDto> dtos) {
        Map<Long, List<EscapePointLocationRefDto>> byEscapePointSeqp = escapePointLocationRepository
                .findAllByEscapePoint_SeqpIn(escapePointSeqps).stream()
                .collect(Collectors.groupingBy(
                        link -> link.getEscapePoint().getSeqp(),
                        Collectors.mapping(this::toRefDto, Collectors.toList())));

        for (int i = 0; i < escapePointSeqps.size(); i++) {
            List<EscapePointLocationRefDto> locationDtos = byEscapePointSeqp.getOrDefault(escapePointSeqps.get(i), List.of());
            EscapePointResponseDto dto = dtos.get(i);
            dto.setLocations(locationDtos);
            dto.setLocationLabel(locationDtos.stream()
                    .filter(EscapePointLocationRefDto::isPrimary)
                    .findFirst()
                    .or(() -> locationDtos.stream().findFirst())
                    .map(EscapePointLocationRefDto::getDisplayName)
                    .orElse(""));
        }
    }

    private EscapePointLocationRefDto toRefDto(EscapePointLocation link) {
        return new EscapePointLocationRefDto(link.getLocation().getUid(), link.getLocation().getCity(),
                link.getLocation().getState(), link.getLocation().getCountry(), link.getLocation().getDisplayName(),
                Boolean.TRUE.equals(link.getIsPrimary()));
    }
}

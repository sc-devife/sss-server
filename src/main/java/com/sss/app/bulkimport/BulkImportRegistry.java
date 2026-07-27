package com.sss.app.bulkimport;

import com.sss.app.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Looks up the right BulkImportSchema by entityType. New entities register
 * themselves automatically just by existing as a @Component implementing
 * BulkImportSchema — nothing here needs to change when one is added.
 */
@Component
public class BulkImportRegistry {

    private final Map<String, BulkImportSchema> schemasByType;

    public BulkImportRegistry(List<BulkImportSchema> schemas) {
        this.schemasByType = schemas.stream()
                .collect(Collectors.toMap(BulkImportSchema::entityType, s -> s));
    }

    public BulkImportSchema get(String entityType) {
        BulkImportSchema schema = schemasByType.get(entityType);
        if (schema == null) {
            throw new NotFoundException("No bulk-import schema registered for entity type: " + entityType);
        }
        return schema;
    }
}

package com.sss.app.repository.library.escapesource;

import com.sss.app.entity.library.escapesource.EscapeSource;
import com.sss.app.util.escapeSource.EscapeSourceType;
//import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

//import java.awt.print.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface EscapeSourceRepository extends JpaRepository<EscapeSource, Long> {
    boolean existsByFullNameIgnoreCaseAndSourceType(
            String fullName,
            EscapeSourceType sourceType
    );

    boolean existsByShortNameIgnoreCase(String shortName);
    Page<EscapeSource> findAll(Pageable pageable);

    Optional<EscapeSource> findByUid(UUID uid);
}

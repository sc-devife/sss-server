package com.sss.app.repository.lead;

import com.sss.app.entity.lead.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    // Example: find leads by status
    List<Lead> findByStatus(String status);

    // Example: find leads by destination (for travel use case)
    List<Lead> findByDestinationIgnoreCase(String destination);

    // Example: find leads by source (INSTAGRAM, WEBSITE, etc.)
   // List<Lead> findBySource(com.example.crm.entity.LeadSource source);
}

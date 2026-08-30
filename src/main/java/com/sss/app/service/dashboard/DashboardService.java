package com.sss.app.service.dashboard;

import com.sss.app.dto.dashboard.DashboardResponseDTO;
import com.sss.app.dto.dashboard.LeadsTrendPointDTO;

import java.util.List;

public interface DashboardService {
    DashboardResponseDTO getDashboard();

    /** period: one of "7d", "30d", "90d", "12m". Returns [] if the caller lacks organizations.read. */
    List<LeadsTrendPointDTO> getLeadsTrend(String period);
}

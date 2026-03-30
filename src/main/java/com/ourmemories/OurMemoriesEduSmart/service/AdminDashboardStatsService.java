package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.model.Role;
import org.springframework.stereotype.Service;

@Service
public interface AdminDashboardStatsService {
    int countAllActiveUsers();
    int countAllUsersWithRole(Role role);
    int countAllApplications();
    int countAllApplicationsWithStatus(String applicationStatus);
}

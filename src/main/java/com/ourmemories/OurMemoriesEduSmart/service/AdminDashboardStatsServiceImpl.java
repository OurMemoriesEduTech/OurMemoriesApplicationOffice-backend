package com.ourmemories.OurMemoriesEduSmart.service;

import com.ourmemories.OurMemoriesEduSmart.model.Role;
import com.ourmemories.OurMemoriesEduSmart.repository.ApplicationRepository;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardStatsServiceImpl implements AdminDashboardStatsService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    public AdminDashboardStatsServiceImpl(UserRepository userRepository, ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public int countAllActiveUsers() {
        return (int) userRepository.count();
    }

    @Override
    public int countAllUsersWithRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    public int countAllApplications() {
        return (int) applicationRepository.count();
    }

    @Override
    public int countAllApplicationsWithStatus(String applicationStatus) {
        return applicationRepository.countApplicationByStatus(applicationStatus);
    }
}

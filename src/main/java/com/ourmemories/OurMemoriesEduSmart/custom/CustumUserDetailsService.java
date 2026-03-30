package com.ourmemories.OurMemoriesEduSmart.custom;

import com.ourmemories.OurMemoriesEduSmart.model.User;
import com.ourmemories.OurMemoriesEduSmart.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustumUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustumUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(user);
    }
}

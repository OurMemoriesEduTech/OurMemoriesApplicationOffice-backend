package com.ourmemories.OurMemoriesEduSmart.config;

import com.ourmemories.OurMemoriesEduSmart.custom.CustumUserDetailsService;
import com.ourmemories.OurMemoriesEduSmart.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustumUserDetailsService custumUserDetailsService;

    public JwtCookieFilter(JwtUtil jwtUtil, CustumUserDetailsService custumUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.custumUserDetailsService = custumUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String jwt = null;
        // lana on this if statement we are checking if the token is available in the cookie
        if (request.getCookies() != null) {
            // lana we are getting the token from the cookie
            jwt = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("jwt"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        // lana we validate and set the user to the Security Context
        if (jwt != null && jwtUtil.validateToken(jwt)) {
            String username = jwtUtil.getEmailFromToken(jwt);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = custumUserDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

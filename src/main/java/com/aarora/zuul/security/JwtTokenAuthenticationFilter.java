package com.aarora.zuul.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    public JwtTokenAuthenticationFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

//        1. Get the authentication Header.
        String header = httpServletRequest.getHeader(jwtConfig.getHeader());
//        2. validate the header.
        if (header == null || !header.startsWith(jwtConfig.getPrefix())) {
            System.out.println("insdie the do filter");
//            request is not valid. Go to the next filter
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        /*  If there is no token provided and hence the user won't be authenticated.
            We will then check if the user is accessing a public path
            All secured paths that need a token are already defined and secured in config class
            If the user tries to access without access token, then won't be authenticated and an exception
            will be thrown
         */
//          Extract the token
        String token = header.replace(jwtConfig.getPrefix(), "");
        System.out.println("Checking token"+token);
        try {
//            5. Validate the token
            Claims claims = Jwts.parser().setSigningKey(jwtConfig.getSecret().getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            if (username != null) {
                List<String> authorities = (List<String>) claims.get("authorities");
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                );
//                6. Set the context that the user is authenticated.
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // In case of failure. Make sure it's clear; so guarantee user won't be authenticated
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}

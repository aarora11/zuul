package com.aarora.zuul.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class SecurityTokenConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtConfig jwtConfig;


    protected void configure(HttpSecurity http) throws  Exception{
        http.csrf().disable()
//                Make sure we use the stateless protocol. Do not want to maintain session.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
//                handle an un-authorized event.
                .exceptionHandling().authenticationEntryPoint((req, rsp, e)-> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .and()
//                Add a filter to validate the token with every request.
                .addFilterAfter(new JwtTokenAuthenticationFilter(jwtConfig), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
//                Allow those who are acessing the auth route with POST
                .antMatchers(jwtConfig.getUri()).permitAll()
//                Must be an admin to access the admin routes
                .antMatchers("/gallery"+"/admin/**").hasRole("ADMIN")
//                Any other request has to be authenticated.
                .anyRequest().authenticated();
    }


    @Bean
    public JwtConfig jwtConfig(){
        return new JwtConfig();
    }




}


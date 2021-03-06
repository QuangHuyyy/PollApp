package com.example.pollappapi.config;

import com.example.pollappapi.security.CustomUserDetailsService;
import com.example.pollappapi.security.JwtAuthenticationEntryPoint;
import com.example.pollappapi.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // This is the primary spring security annotation that is used to enable web security in a project.
@EnableGlobalMethodSecurity( // This is used to enable method level security based on annotations.
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(customUserDetailsService) // cung c???p user service cho spring security
                .passwordEncoder(passwordEncoder()); // cung c???p password encoder
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // Get AuthenticationManager bean
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors() // ch???n request t??? domain kh??c
                .and()
                .csrf().disable() //v?? hi???u h??a csrf
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .sessionManagement() // cho ph??p ch??ng t??i ki???m so??t Phi??n HTTP c???a m??nh
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //Kh??ng c?? phi??n n??o ???????c t???o ho???c s??? d???ng b???i Spring Security
                .and()
                .authorizeRequests()
                .antMatchers("/",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js").permitAll() // cho ph??p t???t c??? m???i ng?????i truy c???p v??o c??c t??i nguy??n n??y
                .antMatchers("/api/auth/**").permitAll() // cho ph??p t???t c??? m???i ng?????i truy c???p v??o ?????a ch??? n??y
                .antMatchers("/api/user/checkUsernameAvailability",
                        "/api/user/checkEmailAvailability").permitAll() // cho ph??p t???t c??? m???i ng?????i truy c???p v??o ?????a ch??? n??y
                .antMatchers(HttpMethod.GET, "/api/polls/**",
                        "/api/users/**").permitAll() // cho ph??p t???t c??? m???i ng?????i truy c???p v??o ?????a ch??? n??y
                .anyRequest().authenticated(); // t???t c??? c??c request kh??c ?????u ph???i x??c th???c m???i ???????c truy c???p

        // th??m 1 l???p filter ????? ki???m tra jwt
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}

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
                .userDetailsService(customUserDetailsService) // cung cấp user service cho spring security
                .passwordEncoder(passwordEncoder()); // cung cấp password encoder
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // Get AuthenticationManager bean
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors() // chặn request từ domain khác
                .and()
                .csrf().disable() //vô hiệu hóa csrf
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .sessionManagement() // cho phép chúng tôi kiểm soát Phiên HTTP của mình
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //Không có phiên nào được tạo hoặc sử dụng bởi Spring Security
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
                        "/**/*.js").permitAll() // cho phép tất cả mọi người truy cập vào các tài nguyên này
                .antMatchers("/api/auth/**").permitAll() // cho phép tất cả mọi người truy cập vào địa chỉ này
                .antMatchers("/api/user/checkUsernameAvailability",
                        "/api/user/checkEmailAvailability").permitAll() // cho phép tất cả mọi người truy cập vào địa chỉ này
                .antMatchers(HttpMethod.GET, "/api/polls/**",
                        "/api/users/**").permitAll() // cho phép tất cả mọi người truy cập vào địa chỉ này
                .anyRequest().authenticated(); // tất cả các request khác đều phải xác thực mới được truy cập

        // thêm 1 lớp filter để kiểm tra jwt
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}

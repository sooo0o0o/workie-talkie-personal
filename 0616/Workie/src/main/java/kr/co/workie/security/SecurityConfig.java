package kr.co.workie.security;

import kr.co.workie.security.filter.JWTAuthenticationFilter;
import kr.co.workie.util.JWTProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JWTProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        // 토큰기반 인증 시큐리티 설정
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))      //******확인*********
                .csrf(CsrfConfigurer::disable)              // 사이트 위변조 방지
                .httpBasic(HttpBasicConfigurer::disable)    // 기본 HTTP 인증 방식 비활성
                .formLogin(FormLoginConfigurer::disable)    // form 이 아닌 토큰 기반이므로 폼인증 비활성
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성
                // 토큰 검사 필터 등록
                .addFilterBefore(new JWTAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        //.requestMatchers("/article/**").hasAnyRole("ADMIN", "USER") <- 프론트에서 테스트할 때 인가 처리 아래처럼 수정
                        .requestMatchers(HttpMethod.GET,"/article/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/article/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET,"/product/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/product/**").hasRole("ADMIN")
                        .anyRequest().permitAll());


        return httpSecurity.build();
    }

    // Security 인증 암호화 인코더 설정
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // CORS 설정 Bean 추가
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173", // 프론트엔드 개발 서버
                "https://workie-talkie.site",
                "https://lotte2-community-app-project-team3-lac.vercel.app", // Vercel 배포 주소
                "https://workie-talkie-personal-kappa.vercel.app" // Vercel 배포 주소
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 자격 증명(쿠키, 인증 헤더 등)을 허용 (JWT 사용 시 필요)
        configuration.setMaxAge(3600L); // Pre-flight 요청 결과 캐시 시간 (초)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 설정 적용
        return source;
    }
    
}

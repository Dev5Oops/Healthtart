package com.dev5ops.healthtart.security;

import com.dev5ops.healthtart.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
/* 설명. OncePerRequestFilter를 상속받아 doFilterInternal을 오버라이딩 한다.(한번만 실행되는 필터) */
public class JwtFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public JwtFilter(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /* 설명. 들고 온(Request Header) 토큰이 유효한지 판별 및 인증(Authentication 객체로 관리) */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("UsernamePasswordAuthenticationFilter보다 먼저 동작하는 필터");

        // oauth 로그인의 경우 filter가 아닌 oauth2handler에 가기 때문에 따로 처리할 필요가 없음. 이후 요청은 jwt token을 가지고 접근할테니 따로 처리 필요없음.
//        // SecurityContext에 이미 OAuth2 인증 정보가 있는지 확인 (OAuth2 로그인 처리)
//        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
//
//        // OAuth2 로그인의 경우 JWT 필터 통과 없이 필터 체인 계속 진행
//        if (currentAuthentication != null && currentAuthentication instanceof OAuth2AuthenticationToken) {
//            log.info("OAuth2 authentication detected, skipping JWT processing.");
//            filterChain.doFilter(request, response);
//            return;
//        }

        // Authorization 헤더에서 JWT 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        log.info("jwtFilter의 getHeader('Authorization'): {}", authorizationHeader);

        String token = null;

        // Authorization 헤더에 토큰이 있는지 확인하고, 없으면 쿼리 파라미터에서 token 값을 찾음
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // "Bearer " 이후의 토큰 값만 추출
            log.info("Bearer 토큰 추출 완료: {}", token);
        } else {
            // 헤더에 토큰이 없으면 쿼리 파라미터에서 token 값을 찾음
            token = request.getParameter("token");
            log.info("OAuth 로그인: 쿼리 파라미터에서 토큰 추출. 토큰 : {}", token);
        }

        /* 설명. 토큰이 있을 경우에만 유효성 검사 및 인증 처리 -> 아니면 다음 필터로 넘어가게함. (oauth 유저의 경우 바로 로그인이 되기 때문에)*/
        if (token != null && jwtUtil.validateToken(token)) {
            Authentication authentication = jwtUtil.getAuthentication(token);
            log.info("JwtFilter를 통과한 유효한 토큰을 통해 security가 관리할 principal 객체: {}", authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);   // 인증 완료
        } else {
            log.warn("유효하지 않은 토큰이거나 토큰이 없습니다.");
        }


        /* 설명. 위의 if문으로 인증된 Authentication 객체가 principal 객체로 관리되지 않는다면 다음 필터 실행 */
        filterChain.doFilter(request, response);    // 실행 될 다음 필터는 UsernamePasswordAuthenticationFilter
    }

}
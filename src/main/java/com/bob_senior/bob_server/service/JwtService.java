package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.base.BaseException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

import static com.bob_senior.bob_server.domain.base.BaseResponseStatus.*;

@Service
@Log4j2
public class JwtService {


    private final RedisService redisService;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 60;  // 60일 (개발 단계) -> 최종 2일 변경 예정
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 60;  // 60일 (개발 단계) -> 최종 2일 변경 예정
    private final Key key;

    @Autowired
    public JwtService(RedisService redisService, @Value("${jwt.secret}") String secretKey) {
        this.redisService = redisService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 헤더에서 access 토큰 가져오기
    public String getAccessToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("jwtAccessToken");
    }

    // 헤더에서 refresh 토큰 가져오기
    public String getRefreshToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("jwtRefreshToken");
    }

    // access 토큰 생성하기
    public String createAccessToken(int userIdx) {
        Date now = new Date();

        // access Token 생성
        Date accessTokenExpiresIn = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("userIdx", userIdx)
                .setIssuedAt(now)
                .setExpiration(accessTokenExpiresIn)        // payload "exp": 1516239022 (예시)
                .signWith(key, SignatureAlgorithm.HS512)    // header "alg": "HS512"
                .compact();

        return accessToken;
    }


    // refresh 토큰 생성하기
    public String createRefreshToken(int userIdx) {
        Date now = new Date();

        // Refresh Token 생성
        Date refreshTokenExpiresIn = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(now)
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // redis에 저장!!!!
        redisService.setRedisRefreshToken(userIdx, refreshToken, REFRESH_TOKEN_EXPIRE_TIME);

        return refreshToken;
    }


    // refresh 토큰 유효성 검증하기
    // refresh 토큰이 유효하지 않으면 throw BaseException
    // 입력으로 들어온 refresh 토큰이 redis에 저장된 refresh 토큰과 다르면 return false
    public void validateRefreshToken(int userIdx) throws BaseException {
        String refreshToken = getRefreshToken();

        if (refreshToken == null || refreshToken.length() == 0) {
            log.error("은");
            throw new BaseException(EMPTY_REFRESH_JWT);
        }

        try {
            Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("만료된 Refresh 토큰입니다. 다시 로그인해주세요.");
            throw new BaseException(EXPIRED_REFRESH_JWT);
        }catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.error("지원되지 않거나 잘못된 Refresh 토큰 입니다.");
            throw new BaseException(INVALID_REFRESH_JWT);
        }

        // redis에서 refresh 토큰을 가져와서 입력으로 들어온 refresh 토큰과 비교
        String refreshTokenInRedis = redisService.getRedisRefreshToken(userIdx);
        if ( (refreshTokenInRedis == null) || (!refreshTokenInRedis.equals(refreshToken)) ){
            log.error("존재하지 않거나 만료된 Refresh 토큰입니다. 다시 로그인해주세요.");
            throw new BaseException(NOT_EXIST_REFRESH_JWT);
        }

    }

    // access 토큰 유효성 검증하기
    // access 토큰이 유효하지 않으면 throw BaseException
    // access 토큰의 userIdx와 입력으로 들어온 userIdx가 일치하지 않으면 return false
    // access 토큰의 userIdx와 입력으로 들어온 userIdx가 일치하면 return true
    public void validateAccessToken(int userIdx) throws BaseException {

        String accessToken = getAccessToken();

        if (accessToken == null || accessToken.length() == 0) {
            log.error("Access 토큰을 입력해주세요.");
            throw new BaseException(EMPTY_ACCESS_JWT);
        }

        try {
            int userIdxInJwt = Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody()
                    .get("userIdx", Integer.class);

            if (userIdxInJwt != userIdx){
                log.error("Access 토큰의 userIdx와 Request의 userIdx가 일치하지 않습니다.");
                throw new BaseException(INVALID_USER_ACCESS_JWT);
            }
        } catch (ExpiredJwtException e) {
            log.error("만료된 Access 토큰입니다. Refresh 토큰을 이용해서 새로운 Access 토큰을 발급 받으세요.");
            throw new BaseException(EXPIRED_ACCESS_JWT);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.error("지원되지 않거나 잘못된 Access 토큰 입니다.");
            throw new BaseException(INVALID_ACCESS_JWT);
        }
    }
}

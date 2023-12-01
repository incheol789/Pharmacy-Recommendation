package com.example.project.api.service;

import com.example.project.api.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAddressSearchService {

    private final RestTemplate restTemplate;
    private final KakaoUriBuilderService kakaoUriBuilderService;

    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 2000)
    )
    public KakaoApiResponseDto requestAddressSearch(String address) {
        if (ObjectUtils.isEmpty(address)) {
            return null;
        }

        URI uri = kakaoUriBuilderService.buildUriByAddressSearch(address);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);

        // HttpEntity를 사용하여 헤더 설정
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        try {
            // kakao api 호출
            ResponseEntity<KakaoApiResponseDto> responseEntity =
                    restTemplate.exchange(uri, HttpMethod.GET, httpEntity, KakaoApiResponseDto.class);

            // 응답을 반환
            return responseEntity.getBody();
        } catch (HttpClientErrorException.Unauthorized unauthorizedException) {
            // 401 Unauthorized 예외를 적절히 처리
            // 예: 로깅, 예외 전파, 기본 응답 등
            return handleUnauthorizedException(unauthorizedException);
        }
    }

    private KakaoApiResponseDto handleUnauthorizedException(HttpClientErrorException.Unauthorized unauthorizedException) {
        // 여기서 적절한 처리를 수행
        // 예: 로깅, 예외 전파, 기본 응답 등
        return null;
    }

    @Recover
    public KakaoApiResponseDto recover(RuntimeException e, String address) {
        log.error("All the retries failed. address: {}, error: {}", address, e.getMessage());
        return null;
    }

}

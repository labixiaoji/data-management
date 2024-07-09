package cn.ist.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class ProxyService {

    private final RestTemplate restTemplate;

    @Autowired
    public ProxyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> T proxyGetRequest(String targetUrl, Map<String, String> queryParams, Class<T> responseType) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(targetUrl);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        return restTemplate.getForObject(builder.toUriString(), responseType);
    }

    public <T> T proxyPostRequest(String targetUrl, Map<String, Object> requestBody, Class<T> responseType) {
        return restTemplate.postForObject(targetUrl, requestBody, responseType);
    }
}

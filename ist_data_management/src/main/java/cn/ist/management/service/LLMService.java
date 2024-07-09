package cn.ist.management.service;

import cn.ist.management.po.LLMResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LLMService {

    @Value("${python.service.test-url}")
    private String llmServiceUrl;
    private final ProxyService proxyService;

    @Autowired
    public LLMService(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    public LLMResponse.responseData queryLLM(String demand, String domain) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("demand", demand);
        queryParams.put("domain", domain);
        LLMResponse result = proxyService.proxyPostRequest(llmServiceUrl + "llm", queryParams, LLMResponse.class);
        return result.getData();
    }
}


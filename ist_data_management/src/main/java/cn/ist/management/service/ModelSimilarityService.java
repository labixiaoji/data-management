package cn.ist.management.service;

import cn.ist.management.vo.modelsimilarity.InsertModelResponse;
import cn.ist.management.vo.modelsimilarity.QueryModelsResponse;
import cn.ist.management.vo.modelsimilarity.QueryTagsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModelSimilarityService {

    @Value("${python.service.url}")
    private String modelSimilarityServiceUrl;
    private final ProxyService proxyService;

    @Autowired
    public ModelSimilarityService(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    public InsertModelResponse insertModel(String id, List<String> fields, List<String> tags) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", id);
        requestBody.put("fields", fields);
        requestBody.put("tags", tags);
        return proxyService.proxyPostRequest(modelSimilarityServiceUrl + "models", requestBody, InsertModelResponse.class);
    }


    public QueryModelsResponse queryModels(List<String> fields, Integer modelNumber) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("fields", String.join(",", fields));
        queryParams.put("model_num", modelNumber.toString());
        return proxyService.proxyGetRequest(modelSimilarityServiceUrl + "models", queryParams, QueryModelsResponse.class);
    }

    public QueryTagsResponse queryTags(List<String> fields, Integer modelNumber, Integer tagNumber) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("fields", String.join(",", fields));
        queryParams.put("model_num", modelNumber.toString());
        queryParams.put("tag_num", tagNumber.toString());
        return proxyService.proxyGetRequest(modelSimilarityServiceUrl + "models/tags", queryParams, QueryTagsResponse.class);
    }

}

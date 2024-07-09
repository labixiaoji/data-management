package cn.ist.management.vo.modelsimilarity;

import lombok.Data;

import java.util.List;

@Data
public class QueryTagsResponse {
    private int code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<String> tags;
    }
}

package cn.ist.management.vo.modelsimilarity;

import lombok.Data;

import java.util.List;

@Data
public class QueryModelsResponse {
    private int code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<Model> models;
    }

    @lombok.Data
    public static class Model {
        private String id;
        private List<String> fields;
        private List<String> tags;
    }
}

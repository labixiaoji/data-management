package cn.ist.management.po;

import lombok.Data;

@Data
public class LLMResponse {
    private int code;
    private responseData data;
    private String message;

    @Data
    public class responseData {
        private String llmAnswer;
        private String llmCode;
        private String queryLog;
        private String executeResult;
    }
}

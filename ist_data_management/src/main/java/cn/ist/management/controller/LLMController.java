package cn.ist.management.controller;

import cn.ist.management.common.CommonResult;
import cn.ist.management.po.LLMResponse;
import cn.ist.management.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/llmProxyService")
public class LLMController {
    private final LLMService llmService;

    @Autowired
    public LLMController(LLMService llmService) {
        this.llmService = llmService;
    }

    /**
     *
     */
    @PostMapping("/query")
    public CommonResult<LLMResponse.responseData> queryLLM(@RequestBody Map<String, Object> modelMap) {
        try {
            String demand = (String) modelMap.get("demand");
            String domain = (String) modelMap.get("domain");
            LLMResponse.responseData result;
            result = llmService.queryLLM(demand, domain);
            return new CommonResult<>(200, "ok", result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

}
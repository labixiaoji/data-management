package cn.ist.management.controller;

import cn.ist.management.common.CommonResult;
import cn.ist.management.dao.impl.DataModelDao;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.vo.modelsimilarity.InsertModelResponse;
import cn.ist.management.vo.modelsimilarity.QueryModelsResponse;
import cn.ist.management.vo.modelsimilarity.QueryTagsResponse;
import cn.ist.management.service.ModelSimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/modelSimilarityService")
public class ModelSimilarityController {
    private final ModelSimilarityService modelSimilarityService;
    @Resource
    private DataModelDao dataModelDao;

    @Autowired
    public ModelSimilarityController(ModelSimilarityService modelSimilarityService) {
        this.modelSimilarityService = modelSimilarityService;
    }

    /**
     * 查询相似模型
     */
    @GetMapping("/models")
    public CommonResult<List<DataModel>> queryModels(@RequestParam String fields,
                                                     @RequestParam(required = false) Integer modelNum) {
        try {
            QueryModelsResponse result;
            modelNum = (modelNum == null) ? 100 : modelNum;
            result = modelSimilarityService.queryModels(Arrays.asList(fields.split(",")), modelNum);

            List<String> idList = new ArrayList<>();
            for (QueryModelsResponse.Model model : result.getData().getModels()) {
                idList.add(model.getId());
            }

            List<DataModel> models = dataModelDao.findByModelId(idList);

            return new CommonResult<>(200, "ok", models);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * 查询相似标签
     */
    @GetMapping("/models/tags")
    public CommonResult<QueryTagsResponse> queryTags(@RequestParam String fields,
                                                     @RequestParam(name = "model_num", required = false) Integer modelNum,
                                                     @RequestParam(name = "tag_num", required = false) Integer tagNum) {
        try {
            modelNum = (modelNum == null) ? 10 : modelNum;
            tagNum = (tagNum == null) ? 5 : tagNum;
            QueryTagsResponse result = modelSimilarityService.queryTags(Arrays.asList(fields.split(",")), modelNum, tagNum);
            return new CommonResult<>(200, "ok", result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * 向量数据库新增模型
     */
    @PostMapping("/models")
    public CommonResult<InsertModelResponse> insertModel(@RequestBody Map<String, Object> modelMap) {
        try {
            InsertModelResponse result = modelSimilarityService.insertModel((String) modelMap.get("id"),
                    (List<String>) modelMap.get("fields"), (List<String>) modelMap.get("tags"));
            return new CommonResult<>(200, "ok", result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

}

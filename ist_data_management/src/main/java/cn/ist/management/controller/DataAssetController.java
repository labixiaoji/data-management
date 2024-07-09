package cn.ist.management.controller;

import cn.ist.management.common.CommonResult;
import cn.ist.management.dao.impl.DataModelDao;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.vo.fromFront.QueryDataAssetFromVo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/dataAssetService")
public class DataAssetController {

    @Resource
    private DataModelDao dataModelDao;

    /**
     * findByModelAll
     *
     * @description 通过模型所有字段查找
     */
    @GetMapping ("/findByModelAll")
    public CommonResult findByQueryDataAsset(QueryDataAssetFromVo queryDataAssetFromVo) {

        try {
            List<DataModel> modelAll = dataModelDao.findByModelAll(queryDataAssetFromVo);
            return new CommonResult<>(200, "ok",modelAll);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }

    }

    /**
     * dataAssetCatalogDisplay
     *
     * @description 通过模型的字段展示相应的资产目录
     */
    @PostMapping("/dataAssetCatalogDisplay")
    public CommonResult dataAssetCatalogDisplay(@RequestBody QueryDataAssetFromVo queryDataAssetFromVo) {
        try {
            List<DataModel> modelAll = dataModelDao.findByModelAll(queryDataAssetFromVo);
            System.out.println(modelAll);
            return new CommonResult<>(200, "ok",modelAll);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }

    }


    /**
     * dataAssetToExcel
     *
     * @description 将资产目录导出成Excel
     */

}

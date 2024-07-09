package cn.ist.management.controller;

import cn.ist.management.common.CommonResult;
import cn.ist.management.dao.IDataModelDao;
import cn.ist.management.dao.impl.DataModelDao;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.po.model.StructuredDataModel;
import cn.ist.management.po.quality.StructuredDataQuality;
import org.springframework.beans.factory.annotation.Value;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.vo.fromFront.AddModelFromVo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.GET;

import javax.annotation.Resource;
import java.sql.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dataQualityService")
public class DataQualityController {
    private String url = "jdbc:postgresql://124.222.140.214:5666/data_management?user=postgres&password=123qweasd";
    @GetMapping("/query")
    public CommonResult<String> queryQuality() {
        try {
            StructuredDataQuality structuredDataQuality = new StructuredDataQuality(url, "public", "ship_engine_status");
            double result = structuredDataQuality.dataQualityScore();
            return new CommonResult<>(200, "ok", Double.toString(result));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

}

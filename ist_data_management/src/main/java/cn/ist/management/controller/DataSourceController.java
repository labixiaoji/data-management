package cn.ist.management.controller;

import cn.ist.management.common.CommonResult;
import cn.ist.management.dao.impl.DataSourceDao;
import cn.ist.management.po.Field;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.vo.fromFront.AddDataSourceFromVo;
import cn.ist.management.vo.fromFront.GetMetaFieldsFromVo;
import cn.ist.management.vo.fromFront.GetTablesFromVO;
import cn.ist.management.vo.fromFront.QueryDataAssetFromVo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import cn.ist.management.dao.impl.DataSourceDao;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/dataSourceService")
public class DataSourceController {

    @Resource
    private DataSourceDao dataSourceDao;

    /**
     * addDataSource
     *
     * @param addDataSourceFromVo
     */
    @PostMapping("/addDataSource")
    public CommonResult<Void> addDataSource(@RequestBody AddDataSourceFromVo addDataSourceFromVo) {
        try {
            DataSource source = addDataSourceFromVo.toPo();
            dataSourceDao.saveSource(source);
            return new CommonResult<>(200, "ok");
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * deleteDataSource
     *
     * @param sourceId
     */
    @GetMapping("/deleteDataSource/{sourceId}")
    public CommonResult<Void> deleteDataSource(@PathVariable("sourceId") String sourceId) {
        try {
            // mongodb中删除
            dataSourceDao.deleteSource(sourceId);
            return new CommonResult<>(200, "ok");
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * getAllDataSource
     */
    @GetMapping("/getAllDataSource")
    public CommonResult<List<DataSource>> getAllDataSource() {
        try {
            List<DataSource> sources = dataSourceDao.findAll();
            return new CommonResult<>(200, "ok", sources);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * getMetaFields
     *
     * @param getMetaFieldsFromVo
     */
    @PostMapping("/getMetaFields")
    public CommonResult<List<Field>> getMetaFields(@RequestBody GetMetaFieldsFromVo getMetaFieldsFromVo) {
        try {
            DataSource source = getMetaFieldsFromVo.toDataSource();
            List<Field> metaFields = source.metaFields();
            return new CommonResult<>(200, "ok", metaFields);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * getTables
     *
     * @param getTablesFromVO
     */
    @PostMapping("/getTables")
    public CommonResult<List<String>> getTables(@RequestBody GetTablesFromVO getTablesFromVO) {
        try {
            DataSource source = getTablesFromVO.toDataSource();
            List<String> tables = source.allTables();
            return new CommonResult<>(200, "ok", tables);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

}

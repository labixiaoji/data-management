package cn.ist.management.controller;

import cn.ist.management.common.CommonResult;
import cn.ist.management.common.Constant;
import cn.ist.management.dao.impl.DataModelDao;
import cn.ist.management.dao.impl.DataSourceDao;
import cn.ist.management.flink.FlinkEnvironment;
import cn.ist.management.po.Field;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.po.model.UnstructuredDataModel;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.po.source.DirectoryDataSource;
import cn.ist.management.po.source.MongodbDataSource;
import cn.ist.management.po.source.PostgresqlDataSource;
import cn.ist.management.service.ModelSimilarityService;
import cn.ist.management.util.*;
import cn.ist.management.util.thread.ListenerFileChangeThreadRunnable;
import cn.ist.management.vo.fromFront.AddModelFromVo;
import cn.ist.management.vo.fromFront.BindDataSourceFromVO;
import cn.ist.management.vo.fromFront.SaveDataFromVo;
import cn.ist.management.vo.toFront.GetModelDataToVo;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.Connection;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dataModelService")
public class DataModelController {

    @Resource
    private DataModelDao dataModelDao;

    @Resource
    private DataSourceDao dataSourceDao;

    @Autowired
    private ModelSimilarityService modelSimilarityService;

    private ZipUtil zipUtil = ApplicationContextGetBeanHelper.getBean(ZipUtil.class);

    private FileUtil fileUtil = ApplicationContextGetBeanHelper.getBean(FileUtil.class);


    /**
     * addDataModel
     *
     * @param addModelFromVo
     * @description 新增一个未添加过的DataModel，此处不进行数据源的绑定
     */
    @PostMapping("/addDataModel")
    public CommonResult<Void> addDataModel(@RequestBody AddModelFromVo addModelFromVo) {
        try {
            DataModel dataModel = addModelFromVo.toPo();

            // psql模型可能需要接入mongodb数据源，得把documentId留下来便于进行update和delete
            dataModel.getFields().add(0, new Field(Constant.MODEL_DOCUMENT_ID_COL, Field.TYPE_STRING, "", false, false, 0));
            dataModel.getFields().add(1, new Field(Constant.MODEL_SOURCE_ID_COL, Field.TYPE_STRING, "", false, false, 0));
            dataModelDao.saveModel(dataModel);

            List<String> fieldNames = new ArrayList<>();
            for (Field field : dataModel.getFields()) {
                if (field.getName().equals(Constant.MODEL_DOCUMENT_ID_COL) || field.getName().equals(Constant.MODEL_SOURCE_ID_COL)) {
                    continue;
                }
                fieldNames.add(field.getName());
            }
            modelSimilarityService.insertModel(dataModel.getId(), fieldNames, dataModel.getTag());

            return new CommonResult<>(200, "ok");
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * deleteDataModel
     *
     * @param modelId
     */
    @GetMapping("/deleteDataModel/{modelId}")
    public CommonResult<Void> deleteDataModel(@PathVariable("modelId") String modelId) {
        try {

            DataModel model = dataModelDao.findByModelId(modelId);

            // mongodb中删除
            dataModelDao.deleteModel(modelId);

            // 删除对应的数据表
            model.deleteModel();

            // todo 如果正在实时采集怎么办

            return new CommonResult<>(200, "ok");

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * bindDataSource
     *
     * @param bindDataSourceFromVO
     * @description 对已有的DataModel的数据源进行绑定
     */
    @PostMapping("/bindDataSource")
    public CommonResult<Void> bindDataSource(@RequestBody BindDataSourceFromVO bindDataSourceFromVO) {

        try {

            DataModel model = dataModelDao.findByModelId(bindDataSourceFromVO.getModelId());

            List<DataSource> sources = new ArrayList<>();

            int size = bindDataSourceFromVO.getSourceIds().size();

            for (int i = 0; i < size; i++) {
                DataSource source = dataSourceDao.findBySourceId(bindDataSourceFromVO.getSourceIds().get(i));
                sources.add(source);
                // documentId的映射由后端指定
                if (source.getType().equals(DataSource.SOURCE_TYPE_MONGODB)) {
                    Map<String, String> fieldMap = bindDataSourceFromVO.getFieldMaps().get(i);
                    fieldMap.put("_id", Constant.MODEL_DOCUMENT_ID_COL);
                }
            }

            model.setDataSources(sources);

            model.setFieldMaps(bindDataSourceFromVO.getFieldMaps());

            dataModelDao.updateModel(model);

            // 如果是实时模型，那么一绑定就开始采集
            if (model.getRealtime()) {
                new Thread(() -> {
                    for (DataSource source : sources) {
                        if (source instanceof PostgresqlDataSource) {
                            new Thread(() -> {
                                try {
                                    FlinkEnvironment.addPostgresqlSource((PostgresqlDataSource) source, model.getId());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }).start();
                        } else if (source instanceof MongodbDataSource) {
                            new Thread(() -> {
                                try {
                                    FlinkEnvironment.addMongodbSource((MongodbDataSource) source, model.getId());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }).start();
                        } else if (source instanceof DirectoryDataSource) {
                            ((DirectoryDataSource) source).getFtpUtil().addListenerFileChange(source.getUrl(), (UnstructuredDataModel) model);
                        } else {
                            System.out.println("unhandled source type");
                        }
                    }
                }).start();
            }

            return new CommonResult<>(200, "ok");

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }

    }

    /**
     * getAllDataModel
     */
    @GetMapping("/getAllDataModel")
    public CommonResult<List<DataModel>> getAllDataModel() {
        try {
            List<DataModel> models = dataModelDao.findAll();
            Converter.filterSystemField(models);
            return new CommonResult<>(200, "ok", models);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * getModelData
     *
     * @param modelId
     * @param sourceId  填all则不区分数据源
     * @param forExport
     * @description 目前实现为简单的全部获取
     */
    @GetMapping("/getModelData/{modelId}/{sourceId}/{forExport}")
    public CommonResult<GetModelDataToVo> getModelData(@PathVariable("modelId") String modelId, @PathVariable("sourceId") String sourceId, @PathVariable("forExport") Integer forExport) {

        try {

            DataModel model = dataModelDao.findByModelId(modelId);

            Converter.filterSystemField(model);

            List<Field> fields = model.getFields();

            HashMap<String, Object> conditions = new HashMap<>();
            if (!sourceId.equals("all")) {
                conditions.put(Constant.MODEL_SOURCE_ID_COL, sourceId);
            }

            List<Map<String, Object>> rows;

            if (forExport.equals(1)) {
                rows = model.fetchData(true, conditions);
            } else {
                rows = model.fetchData(false, conditions);
                // 限制最多返回1000条
                if (rows.size() > 1000) {
                    rows = rows.subList(0, 1000);
                }
            }

            return new CommonResult<>(200, "ok", new GetModelDataToVo(modelId, fields, rows, model));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }

    }

    @GetMapping("/getZip/{modelId}")
    public Object getZip(@PathVariable("modelId") String modelId, HttpServletResponse response) throws IOException {
        DataModel model = dataModelDao.findByModelId(modelId);
        List<DataSource> dataSourceList = model.getDataSources();
        String directoryPath = "";
        for (DataSource dataSource : dataSourceList) {
            directoryPath = "./ftpfiles/" + dataSource.getDataSourceId();
            break;
        }
        //设置response的header
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=" + modelId + ".zip");
        //方式1,使用java原生方式
        //调用工具类,将test文件夹压缩成zip包并写入到http响应流,保留目录结构,
        zipUtil.toZip(directoryPath, response.getOutputStream(), true);
        return null;
    }

    /**
     * saveData
     *
     * @param saveDataFromVo
     * @description 目前实现为简单的全量采集
     */
    @PostMapping("/saveData")
    public CommonResult<Void> saveData(@RequestBody SaveDataFromVo saveDataFromVo) {
        try {
            DataModel model = dataModelDao.findByModelId(saveDataFromVo.getModelId());
            model.saveData();
            model.setLastUpdateTime(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dataModelDao.updateModel(model);
            return new CommonResult<>(200, "ok");
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

}

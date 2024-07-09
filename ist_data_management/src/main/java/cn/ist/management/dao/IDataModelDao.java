package cn.ist.management.dao;

import cn.ist.management.po.model.DataModel;
import cn.ist.management.vo.fromFront.QueryDataAssetFromVo;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface IDataModelDao {

    void saveModel(DataModel model);

    void updateModel(DataModel model);

    DataModel findByModelName(String modelName);

    DataModel findByModelId(String modelId);

    List<DataModel> findByModelId(List<String> modelIds);

    List<DataModel> findAll();

    //通过模型所有字段查询
    List<DataModel> findByModelAll(QueryDataAssetFromVo queryDataAssetFromVo);

    void deleteModel(String modelId);

}

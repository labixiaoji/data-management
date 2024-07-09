package cn.ist.management.dao;

import cn.ist.management.po.model.DataModel;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.vo.fromFront.QueryDataAssetFromVo;

import java.util.List;

public interface IDataSourceDao {

    void saveSource(DataSource dataSource);

    List<DataSource> findAll();

    DataSource findBySourceId(String sourceId);

    void deleteSource(String sourceId);

}

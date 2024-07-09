package cn.ist.management.dao.impl;

import cn.ist.management.dao.IDataModelDao;
import cn.ist.management.dao.IDataSourceDao;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.vo.fromFront.QueryDataAssetFromVo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class DataSourceDao implements IDataSourceDao {

    @Resource
    MongoTemplate mongoTemplate;

    @Override
    public void saveSource(DataSource dataSource) {
        mongoTemplate.save(dataSource);
    }

    @Override
    public List<DataSource> findAll() {
        return mongoTemplate.findAll(DataSource.class);
    }

    @Override
    public DataSource findBySourceId(String sourceId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("dataSourceId").is(sourceId));
        return mongoTemplate.findOne(query, DataSource.class);
    }

    @Override
    public void deleteSource(String sourceId) {
        Query query = new Query(Criteria.where("dataSourceId").is(sourceId));
        mongoTemplate.remove(query, DataSource.class);
    }
}

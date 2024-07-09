package cn.ist.management.dao.impl;

import cn.ist.management.dao.IDataModelDao;
import cn.ist.management.po.model.DataModel;
import cn.ist.management.vo.fromFront.QueryDataAssetFromVo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class DataModelDao implements IDataModelDao {

    @Resource
    MongoTemplate mongoTemplate;

    @Override
    public void saveModel(DataModel model) {
        DataModel temp = findByModelName(model.getModelName());
        if (temp != null) {
            throw new RuntimeException("已存在同名的DataModel");
        }
        mongoTemplate.save(model);
    }

    @Override
    public void updateModel(DataModel model) {
        DataModel temp = findByModelName(model.getModelName());
        if (temp == null) {
            throw new RuntimeException("未查询到此DataModel");
        } else {
            model.setId(temp.getId());
            mongoTemplate.save(model);
        }
    }

    @Override
    public DataModel findByModelName(String modelName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("modelName").is(modelName));
        return mongoTemplate.findOne(query, DataModel.class);
    }


    //根据字段进行查询
    @Override
    public List<DataModel> findByModelAll(QueryDataAssetFromVo queryDataAssetFromVo) {
        Query query = new Query();
        // 如果description有值就进行查询
        if (!queryDataAssetFromVo.getDescription().isEmpty()) {
            query.addCriteria(Criteria.where("description").is(queryDataAssetFromVo.getDescription()));
        }
        // 如果tag有值就进行查询
        if (!queryDataAssetFromVo.getTag().isEmpty()) {
            String[] tags = queryDataAssetFromVo.getTag().split(",");
            query.addCriteria(Criteria.where("tag").all(tags));
        }
        // 如果type有值就进行查询
        if (queryDataAssetFromVo.getType() != null) {
            query.addCriteria(Criteria.where("type").is(queryDataAssetFromVo.getType()));
        }
        // 如果modal有值就进行查询
        if (queryDataAssetFromVo.getModal() != null) {
            System.out.println(queryDataAssetFromVo.getModal());
            query.addCriteria(Criteria.where("modal").is(queryDataAssetFromVo.getModal()));
        }
        // 如果domain有值就进行查询
        if (!queryDataAssetFromVo.getDomain().isEmpty()) {
            query.addCriteria(Criteria.where("domain").is(queryDataAssetFromVo.getDomain()));
        }
        // 如果modelName有值就进行查询
        if (!queryDataAssetFromVo.getModelName().isEmpty()) {
            query.addCriteria(Criteria.where("modelName").is(queryDataAssetFromVo.getModelName()));
        }
        return mongoTemplate.find(query, DataModel.class);
    }

    @Override
    public DataModel findByModelId(String modelId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(modelId));
        return mongoTemplate.findOne(query, DataModel.class);
    }

    @Override
    public List<DataModel> findByModelId(List<String> modelIds) {
        Query query = new Query(Criteria.where("id").in(modelIds));
        return mongoTemplate.find(query, DataModel.class);
    }

    @Override
    public List<DataModel> findAll() {
        return mongoTemplate.findAll(DataModel.class);
    }

    @Override
    public void deleteModel(String modelId) {
        Query query = new Query(Criteria.where("id").is(modelId));
        mongoTemplate.remove(query, DataModel.class);
    }

}

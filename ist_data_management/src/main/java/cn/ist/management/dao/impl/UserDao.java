package cn.ist.management.dao.impl;

import cn.ist.management.dao.IDataModelDao;
import cn.ist.management.dao.IUserDao;
import cn.ist.management.po.User;
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
public class UserDao implements IUserDao {

    @Resource
    MongoTemplate mongoTemplate;

    @Override
    public void saveUser(User user) {
        User tmp = findByUsername(user.getUsername());
        if (tmp != null) {
            throw new RuntimeException("已存在同名的User");
        }
        mongoTemplate.save(user);
    }

    @Override
    public User findByUsername(String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        return mongoTemplate.findOne(query, User.class);
    }

}

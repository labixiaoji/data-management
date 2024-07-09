package cn.ist.management.dao;

import cn.ist.management.po.User;
import cn.ist.management.po.source.DataSource;

import java.util.List;

public interface IUserDao {

    void saveUser(User user);

    User findByUsername(String username);

}

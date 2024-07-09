package cn.ist.management.controller;

import cn.ist.management.common.CommonResult;
import cn.ist.management.dao.impl.DataSourceDao;
import cn.ist.management.dao.impl.UserDao;
import cn.ist.management.po.Field;
import cn.ist.management.po.User;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.vo.fromFront.AddDataSourceFromVo;
import cn.ist.management.vo.fromFront.GetMetaFieldsFromVo;
import cn.ist.management.vo.fromFront.GetTablesFromVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/userService")
public class UserController {

    @Resource
    private UserDao userDao;

    /**
     * register
     *
     * @param user
     * @description 不需要填写userId；201（用户已存在）
     */
    @PostMapping("/register")
    public CommonResult<Void> register(@RequestBody User user) {
        try {
            User u = userDao.findByUsername(user.getUsername());
            if (u != null) {
                return new CommonResult<>(201, "用户已存在");
            }
            userDao.saveUser(user);
            return new CommonResult<>(200, "ok");
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

    /**
     * login
     *
     * @param user
     * @description 只需要填写username和password；201（用户不存在）；202（密码不正确）
     */
    @GetMapping("/login")
    public CommonResult<User> login(@RequestBody User user) {

        try {

            User u = userDao.findByUsername(user.getUsername());

            if (u == null) {
                return new CommonResult<>(201, "用户不存在");
            }

            if (!u.getPassword().equals(user.getPassword())) {
                return new CommonResult<>(202, "密码不正确");
            }

            return new CommonResult<>(200, "ok", u);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, e.toString());
        }
    }

}

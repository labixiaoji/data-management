package cn.ist.management.po.source;

import cn.ist.management.po.Field;
import cn.ist.management.util.ApplicationContextGetBeanHelper;
import cn.ist.management.util.FtpUtil;
import com.ververica.cdc.connectors.shaded.com.google.common.collect.Lists;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@Slf4j
public class DirectoryDataSource extends DataSource {

    public FtpUtil ftpUtil = ApplicationContextGetBeanHelper.getBean(FtpUtil.class);

    //ftp获取文件
    @Override
    public List<Map<String, Object>> connectAndGetData() {
        String url = getUrl();
        String ROOT_DIR = ".";
        String localPath = "./ftpfiles/" + getDataSourceId();
        Map<String, Object> res = new HashMap<>();
        try {
            res = ftpUtil.getDirectory(url, ROOT_DIR, localPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(res.toString());
        return Lists.newArrayList(res);
    }

    @Override
    public List<Field> metaFields() {
        List<Field> res = new ArrayList<>();
        res.add(new Field("size", Field.TYPE_STRING));
        return null;
    }

    @Override
    public List<String> allTables() {
        return null;
    }

}

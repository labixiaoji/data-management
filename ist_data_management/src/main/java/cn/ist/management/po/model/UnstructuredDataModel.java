package cn.ist.management.po.model;

import cn.ist.management.po.Field;
import cn.ist.management.po.rule.unstructured.UnstructuredRule;
import cn.ist.management.po.source.DataSource;
import cn.ist.management.po.source.DirectoryDataSource;
import cn.ist.management.util.ApplicationContextGetBeanHelper;
import cn.ist.management.util.FileUtil;
import cn.ist.management.util.FtpUtil;
import cn.ist.management.util.ZipUtil;
import lombok.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.ist.management.util.FtpUtil.DIR_SPLIT;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class UnstructuredDataModel extends DataModel {

    private FtpUtil ftpUtil = ApplicationContextGetBeanHelper.getBean(FtpUtil.class);

    private List<UnstructuredRule> rules;

    private FileUtil fileUtil = ApplicationContextGetBeanHelper.getBean(FileUtil.class);


    public UnstructuredDataModel(String id, String modelName, Integer modal, Integer type, List<String> tag, String domain, String description, Boolean realtime, List<Field> fields, List<DataSource> dataSources, List<UnstructuredRule> rules, List<Map<String, String>> fieldMaps) {
        super(id, modelName, modal, type, tag, domain, description, realtime, fields, dataSources, fieldMaps, 100.0, null);
        this.rules = rules;
    }

    @Override
    public void saveData() throws SQLException {
        for (DataSource source : getDataSources()) {
            String url = source.getUrl();
            List<Map<String, Object>> files = source.connectAndGetData();
            // 执行文件搬运操作
            for (Map<String, Object> file : files) {
                for (Map.Entry<String, Object> entry : file.entrySet()) {
                    Object value = entry.getValue();
                    assert ftpUtil != null;
                    Map<String, String> tmp = (Map<String, String>) value;
                    ftpUtil.download(url, tmp.get("ftpDirPath"), tmp.get("fileName"), tmp.get("localDirPath"));
                }
            }
        }
        System.out.println("文件存储结束！");
    }

    @Override
    public List<Map<String, Object>> fetchData(Boolean forExport, Map<String, Object> conditions) {
        List<Map<String, Object>> files = new ArrayList<>();
        if (forExport) {
            return null;
        }
        else {
            List<DataSource> dataSourceList = getDataSources();
            for(DataSource dataSource : dataSourceList) {
                String directoryPath = "./ftpfiles/" + dataSource.getDataSourceId();
                List<Map<String, Object>> fileList = fileUtil.getAllFiles(directoryPath);
                files.addAll(fileList);
                return files;
            }
        }
        return files;
    }

    @Override
    public void insertOne(Map<String, Object> row) throws SQLException {
        DirectoryDataSource source = (DirectoryDataSource) row.get("source");
        assert(source != null);
        ftpUtil.download(source.getUrl(), (String) row.get("ftpDirPath"), (String) row.get("fileName"), (String) row.get("localDirPath"));
    }

    @Override
    public void deleteOne(Map<String, Object> row) throws SQLException {
        DirectoryDataSource source = (DirectoryDataSource) row.get("source");
        assert(source != null);

        ftpUtil.deleteLocal(source.getUrl(), (String) row.get("fileName"), (String) row.get("localDirPath"));
    }

    @Override
    public void updateOne(Map<String, Object> filterBy, Map<String, Object> newRow) throws SQLException {
        deleteOne(filterBy);
        insertOne(newRow);
    }

    @Override
    public void deleteModel() throws SQLException {
        List<DataSource> dataSourceList = getDataSources();
        String directoryPath = "";
        for(DataSource dataSource : dataSourceList) {
            directoryPath = "./ftpfiles/" + dataSource.getDataSourceId();
            break;
        }
        fileUtil.deleteDir(directoryPath);
    }
}

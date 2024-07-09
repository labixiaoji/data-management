package cn.ist.management.util.thread;

import java.sql.SQLException;

@FunctionalInterface
public interface FileChangeEvent {

    /**
     * 文件发生改变时触发此方法
     * @param fileChangeData 文件发生了改变
     * */
    void change(FileChangeData fileChangeData) throws SQLException;

}



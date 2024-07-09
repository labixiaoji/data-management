package cn.ist.management.util.thread;

import lombok.Getter;

public enum FileChangeType {
    FILE_UPDATE(0, "文件更新"),
    FILE_ADD(1, "文件添加"),
    FILE_DELETED(2, "文件删除");

    @Getter
    private Integer type;

    @Getter
    private String desc;

    FileChangeType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}

package com.leantech.admin.common;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class TableDataInfo<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private List<T> rows;
    private int code;
    private String msg;

    public TableDataInfo() {
    }

    public TableDataInfo(List<T> rows, long total) {
        this.rows = rows;
        this.total = total;
        this.code = R.SUCCESS;
        this.msg = "查询成功";
    }

    public static <T> TableDataInfo<T> build(List<T> rows, long total) {
        return new TableDataInfo<>(rows, total);
    }
}

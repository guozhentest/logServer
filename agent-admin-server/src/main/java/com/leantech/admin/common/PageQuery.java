package com.leantech.admin.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public PageQuery() {
    }

    public PageQuery(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum != null && pageNum > 0 ? pageNum : 1;
        this.pageSize = pageSize != null && pageSize > 0 ? pageSize : 10;
    }
}

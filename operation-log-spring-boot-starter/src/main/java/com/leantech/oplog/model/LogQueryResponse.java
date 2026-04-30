package com.leantech.oplog.model;

import lombok.Data;

import java.util.List;

@Data
public class LogQueryResponse<T> {
    private int code;
    private String message;
    private PageData<T> data;

    @Data
    public static class PageData<T> {
        private long total;
        private int page;
        private int size;
        private List<T> records;
    }
}

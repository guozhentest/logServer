package com.leantech.agent.model;

import java.util.List;

public class LogQueryResponse<T> {

    private Integer code;
    private String message;
    private PageData<T> data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PageData<T> getData() {
        return data;
    }

    public void setData(PageData<T> data) {
        this.data = data;
    }

    public static class PageData<T> {

        private Long total;
        private Integer page;
        private Integer size;
        private List<T> records;

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public List<T> getRecords() {
            return records;
        }

        public void setRecords(List<T> records) {
            this.records = records;
        }
    }
}

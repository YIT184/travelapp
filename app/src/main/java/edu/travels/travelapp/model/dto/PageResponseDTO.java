package edu.travels.travelapp.model.dto;

import java.util.List;

public class PageResponseDTO<T> {
    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private Integer pages;
    private List<T> list;
    // 兼容后端返回字段 records
    private List<T> records;

    public PageResponseDTO() {}

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        // 优先使用 list，如果为 null 则尝试使用 records
        if (list != null) {
            return list;
        }
        return records;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
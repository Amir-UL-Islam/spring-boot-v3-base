package com.problemfighter.pfspring.restapi.rr.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class PageableResponse<T> extends ErrorAndBaseData {
    public List<T> data;
    public PaginationData pagination;

    public PaginationData addPagination(Integer page, Integer itemPerPage) {
        if (this.pagination == null) {
            this.pagination = new PaginationData();
        }

        this.pagination.page = page;
        this.pagination.itemPerPage = itemPerPage;
        return this.pagination;
    }
}

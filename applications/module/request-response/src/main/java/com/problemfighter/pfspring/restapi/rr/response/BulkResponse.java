package com.problemfighter.pfspring.restapi.rr.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class BulkResponse<D> extends BaseData {
    public List<D> success = null;
    public List<BulkErrorData<D>> failed = null;

    public BulkResponse<D> addFailed(BulkErrorData<D> data) {
        if (this.failed == null) {
            this.failed = new ArrayList();
        }

        this.failed.add(data);
        return this;
    }

    public BulkResponse<D> addSuccessDataList(List<D> dataList) {
        if (this.success == null) {
            this.success = new ArrayList();
        }

        this.success = dataList;
        return this;
    }
}

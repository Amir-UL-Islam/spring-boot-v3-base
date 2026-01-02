package com.problemfighter.pfspring.restapi.rr.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class DetailsListResponse<T> extends ErrorAndBaseData {
    public List<T> data;
}

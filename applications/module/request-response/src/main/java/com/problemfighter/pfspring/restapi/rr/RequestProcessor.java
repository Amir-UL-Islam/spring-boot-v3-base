package com.problemfighter.pfspring.restapi.rr;

import com.problemfighter.java.oc.common.InitCustomProcessor;
import com.problemfighter.java.oc.common.ObjectCopierException;
import com.problemfighter.java.oc.common.ProcessCustomCopy;
import com.problemfighter.java.oc.copier.ObjectCopier;
import com.problemfighter.java.oc.reflection.ReflectionProcessor;
import com.problemfighter.pfspring.restapi.common.ApiRestException;
import com.problemfighter.pfspring.restapi.common.RestSpringContext;
import com.problemfighter.pfspring.restapi.rr.request.RequestBulkData;
import com.problemfighter.pfspring.restapi.rr.request.RequestData;
import com.problemfighter.pfspring.restapi.rr.response.BulkErrorData;
import com.problemfighter.pfspring.restapi.rr.response.BulkErrorValidEntities;
import com.problemfighter.pfspring.restapi.rr.response.MessageResponse;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class RequestProcessor {
    private ObjectCopier objectCopier = new ObjectCopier();
    private ReflectionProcessor reflectionProcessor;
    public static Integer itemPerPage = 15;
    public static String sortField = "id";
    public static Sort.Direction sortOrder;

    public RequestProcessor() {
        this.objectCopier.initCustomProcessor = new InitCustomProcessor() {
            public <S, D> ProcessCustomCopy<S, D> init(Class<?> klass, S source, D destination) {
                return (ProcessCustomCopy)RestSpringContext.getBean(klass);
            }
        };
        this.reflectionProcessor = new ReflectionProcessor();
    }

    private <D> D copySrcToDst(Object source, D destination) {
        try {
            return (D)this.objectCopier.copy(source, destination);
        } catch (ObjectCopierException e) {
            ApiRestException.otherError(e.getMessage());
            return null;
        }
    }

    private <D> D copySrcToDst(Object source, Class<D> destination) {
        try {
            return (D)this.objectCopier.copy(source, destination);
        } catch (ObjectCopierException e) {
            ApiRestException.otherError(e.getMessage());
            return null;
        }
    }

    private <T, O> T getFieldValue(O object, String name, Class<T> type) {
        try {
            Field field = this.reflectionProcessor.getFieldFromObject(object, name);
            return (T)(field != null && field.getType() == type ? field.get(object) : null);
        } catch (IllegalAccessException var5) {
            return null;
        }
    }

    public <D> D copySrcToDstValidate(Object source, Class<D> destination) {
        this.dataValidate(source);
        return (D)this.copySrcToDst(source, destination);
    }

    public <D> D copySrcToDstValidate(Object source, D destination) {
        return (D)(this.dataValidate(source) ? this.copySrcToDst(source, destination) : null);
    }

    public Boolean dataValidate(Object source) {
        LinkedHashMap<String, String> errors = this.objectCopier.validateObject(source);
        if (errors.size() != 0) {
            ApiRestException.throwException(ResponseProcessor.validationError().reason(errors));
            return false;
        } else {
            return true;
        }
    }

    public <D> Boolean dataValidate(RequestData<D> requestData) {
        return this.dataValidate(requestData.getData());
    }

    public <D> D process(Object source, Class<D> destination) {
        return (D)this.copySrcToDstValidate(source, destination);
    }

    public <D> D process(Object source, D destination) {
        return (D)this.copySrcToDstValidate(source, destination);
    }

    public <D> D process(RequestData<?> requestData, D destination) {
        return (D)this.copySrcToDstValidate(requestData.getData(), destination);
    }

    public <D> D process(RequestData<?> requestData, Class<D> destination) {
        return (D)this.copySrcToDstValidate(requestData.getData(), destination);
    }

    public <D, E> BulkErrorValidEntities<D, E> process(RequestBulkData<D> requestData, Class<E> destination) {
        BulkErrorValidEntities<D, E> errorDst = new BulkErrorValidEntities();

        for(D object : requestData.getData()) {
            try {
                errorDst.addToList(this.copySrcToDstValidate(object, destination));
            } catch (ApiRestException e) {
                MessageResponse messageResponse = (MessageResponse)e.getError();
                errorDst.addFailed((new BulkErrorData()).addError(messageResponse.error).addObject(object));
            }
        }

        return errorDst;
    }

    public <O> Long validateId(RequestData<O> data, String message) {
        return this.validateId(this.getId(data), message);
    }

    public <O> Long validateId(Long id, String message) {
        if (id == null && message != null) {
            ApiRestException.error(message);
        }

        return id;
    }

    public <O> Long getId(RequestData<O> data) {
        return this.getIdFieldValue(data.getData());
    }

    public <O> Long validateNGetId(RequestData<O> data, String message) {
        Long id = this.getIdFieldValue(data.getData());
        return this.validateId(id, message);
    }

    public <O> Long getIdFieldValue(O object) {
        return (Long)this.getFieldValue(object, "id", Long.class);
    }

    public PageRequest paginationOnly(Integer page, Integer size) {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = itemPerPage;
        }

        return PageRequest.of(page, size);
    }

    public PageRequest paginationNSort(Integer page, Integer size, String sort, String field) {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = itemPerPage;
        }

        Sort.Direction order = sortOrder;
        if (sort != null && sort.equals("asc")) {
            order = Direction.ASC;
        }

        if (field == null || field.equals("")) {
            field = sortField;
        }

        return PageRequest.of(page, size, order, new String[]{field});
    }

    public static RequestProcessor instance() {
        return new RequestProcessor();
    }

    static {
        sortOrder = Direction.DESC;
    }
}

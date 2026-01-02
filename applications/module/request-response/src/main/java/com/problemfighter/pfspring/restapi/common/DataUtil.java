package com.problemfighter.pfspring.restapi.common;

import com.problemfighter.java.oc.reflection.ReflectionProcessor;
import com.problemfighter.pfspring.restapi.rr.RequestProcessor;
import com.problemfighter.pfspring.restapi.rr.request.RequestBulkData;
import com.problemfighter.pfspring.restapi.rr.response.BulkErrorData;
import com.problemfighter.pfspring.restapi.rr.response.BulkErrorValidEntities;
import com.problemfighter.pfspring.restapi.rr.response.ErrorData;
import com.problemfighter.pfspring.restapi.rr.response.I18nMessage;
import com.problemfighter.pfspring.restapi.rr.response.MessageResponse;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DataUtil {
    private ReflectionProcessor reflectionProcessor = new ReflectionProcessor();
    private RequestProcessor requestProcessor = new RequestProcessor();

    private <D> D markAsDeletedFlag(D data, Boolean isDeleted) {
        return (D)this.updateProperty(data, Map.of("isDeleted", isDeleted));
    }

    private <D> Iterable<D> markAsDeletedFlag(Iterable<D> dataList, Boolean isDeleted) {
        for(D data : dataList) {
            this.markAsDeletedFlag(data, isDeleted);
        }

        return dataList;
    }

    public <D> List<Long> getAllId(RequestBulkData<D> data) {
        return this.getAllId(data.getData());
    }

    public <D> List<Long> getAllId(List<D> list) {
        return this.getFieldValues(list, "id", Long.class);
    }

    public <D, L> List<L> getFieldValues(List<D> list, String name, Class<L> klass) {
        List<L> response = new ArrayList();

        for(D data : list) {
            try {
                Field field = this.reflectionProcessor.getAnyFieldFromObject(data, name);
                if (field != null && field.getType() == klass) {
                    field.setAccessible(true);
                    response.add((L) field.get(data));
                }
            } catch (IllegalAccessException var9) {
            }
        }

        return response;
    }

    public <D> List<D> markAsDeletedFlag(List<D> dataList, Boolean isDeleted) {
        for(D data : dataList) {
            this.markAsDeletedFlag(data, isDeleted);
        }

        return dataList;
    }

    public <D> Iterable<D> markAsDeleted(Iterable<D> dataList) {
        return this.markAsDeletedFlag(dataList, true);
    }

    public <D> Iterable<D> markAsUndeleted(Iterable<D> dataList) {
        return this.markAsDeletedFlag(dataList, false);
    }

    public <D> Boolean isEmpty(Iterable<D> iterable) {
        return this.size(iterable) == 0;
    }

    public <D> Integer size(Iterable<D> iterable) {
        if (iterable instanceof Collection) {
            return ((Collection)iterable).size();
        } else {
            Integer counter = 0;

            for(Object i : iterable) {
                counter = counter + 1;
            }

            return counter;
        }
    }

    public <D> D getObjectFromListIfFieldValueMatch(List<D> dataList, String fieldName, Object dataObject) {
        for(D data : dataList) {
            try {
                Field listField = this.reflectionProcessor.getAnyFieldFromObject(data, fieldName);
                Field dataField = this.reflectionProcessor.getAnyFieldFromObject(dataObject, fieldName);
                if (listField != null && dataField != null && listField.getType() == dataField.getType()) {
                    listField.setAccessible(true);
                    dataField.setAccessible(true);
                    if (listField.get(data).equals(dataField.get(dataObject))) {
                        return data;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public <E, D> BulkErrorValidEntities<D, E> merge(Iterable<E> mergeTo, RequestBulkData<D> data) {
        BulkErrorValidEntities<D, E> bulkErrorValidEntities = new BulkErrorValidEntities();
        List<D> sourceList = data.getData();

        for(E entity : mergeTo) {
            D source = (D)this.getObjectFromListIfFieldValueMatch(sourceList, "id", entity);
            if (source != null) {
                try {
                    bulkErrorValidEntities.addToList(this.requestProcessor.copySrcToDstValidate(source, entity));
                } catch (ApiRestException e) {
                    MessageResponse messageResponse = (MessageResponse)e.getError();
                    bulkErrorValidEntities.addFailed((new BulkErrorData()).addError(messageResponse.error).addObject(source));
                }

                sourceList.remove(source);
            }
        }

        ErrorData error = new ErrorData();
        error.message = I18nMessage.message("Unable to process update");

        for(D errorSource : sourceList) {
            bulkErrorValidEntities.addFailed((new BulkErrorData()).addError(error).addObject(errorSource));
        }

        return bulkErrorValidEntities;
    }

    public <D> D markAsUndeleted(D data) {
        return (D)this.markAsDeletedFlag(data, false);
    }

    public <D> D markAsDeleted(D data) {
        return (D)this.markAsDeletedFlag(data, true);
    }

    public <D> List<D> updateProperty(List<D> dataList, Map<String, Object> fieldValue) {
        for(D data : dataList) {
            this.updateProperty(data, fieldValue);
        }

        return dataList;
    }

    public <D> D updateProperty(D data, Map<String, Object> fieldValue) {
        for(Map.Entry<String, Object> entry : fieldValue.entrySet()) {
            try {
                Field field = this.reflectionProcessor.getAnyFieldFromObject(data, (String)entry.getKey());
                if (field != null) {
                    field.setAccessible(true);
                    field.set(data, entry.getValue());
                }
            } catch (IllegalAccessException var7) {
            }
        }

        return data;
    }

    public <E> E validateAndOptionToEntity(Optional<E> optional, String message) {
        if (optional.isPresent()) {
            return (E)optional.get();
        } else {
            if (message != null) {
                ApiRestException.error(message);
            }

            return null;
        }
    }

    public static DataUtil instance() {
        return new DataUtil();
    }
}

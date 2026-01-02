package com.problemfighter.pfspring.restapi.rr;

import com.problemfighter.java.oc.common.InitCustomProcessor;
import com.problemfighter.java.oc.common.ObjectCopierException;
import com.problemfighter.java.oc.common.ProcessCustomCopy;
import com.problemfighter.java.oc.copier.ObjectCopier;
import com.problemfighter.pfspring.restapi.common.RestSpringContext;
import com.problemfighter.pfspring.restapi.exception.ExceptionMessage;
import com.problemfighter.pfspring.restapi.rr.response.BulkErrorValidEntities;
import com.problemfighter.pfspring.restapi.rr.response.BulkResponse;
import com.problemfighter.pfspring.restapi.rr.response.DetailsResponse;
import com.problemfighter.pfspring.restapi.rr.response.ErrorData;
import com.problemfighter.pfspring.restapi.rr.response.I18nMessage;
import com.problemfighter.pfspring.restapi.rr.response.MessageResponse;
import com.problemfighter.pfspring.restapi.rr.response.PageableResponse;
import com.problemfighter.pfspring.restapi.rr.response.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ResponseProcessor {
    private ObjectCopier objectCopier = new ObjectCopier();

    public ResponseProcessor() {
        this.objectCopier.initCustomProcessor = new InitCustomProcessor() {
            public <S, D> ProcessCustomCopy<S, D> init(Class<?> klass, S source, D destination) {
                return (ProcessCustomCopy)RestSpringContext.getBean(klass);
            }
        };
    }

    private MessageResponse responseMessage(String message, String errorCode, ErrorData error) {
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.message = I18nMessage.message(message);
        messageResponse.code = errorCode;
        messageResponse.error = error;
        return messageResponse;
    }

    private <E, D> D convertEntityToDTO(E entity, Class<D> dto) throws ObjectCopierException {
        return (D)this.objectCopier.copy(entity, dto);
    }

    public <E, D> List<D> entityToDTO(List<E> entities, Class<D> dto) {
        List<D> dtoList = new ArrayList();
        if (entities != null) {
            for(E entity : entities) {
                try {
                    D dtoObject = (D)this.objectCopier.copy(entity, dto);
                    if (dtoObject != null) {
                        dtoList.add(dtoObject);
                    }
                } catch (ObjectCopierException ignore) {
                    ignore.printStackTrace();
                }
            }
        }

        return dtoList;
    }

    private <E> Boolean isEmptyEntity(E entity) {
        if (entity == null) {
            return true;
        } else {
            return entity instanceof Optional ? ((Optional)entity).isEmpty() : false;
        }
    }

    private <E> E getEntityValue(E entity) {
        if (entity == null) {
            return null;
        } else if (entity instanceof Optional) {
            Optional<E> optional = (Optional)entity;
            return (E)(optional.isEmpty() ? null : optional.get());
        } else {
            return entity;
        }
    }

    public MessageResponse response(String message, String errorCode) {
        return this.responseMessage(message, errorCode, (ErrorData)null).status(Status.success);
    }

    public MessageResponse response(String message) {
        return this.response(message, "1200");
    }

    public <E, D> PageableResponse<D> response(Page<E> page, List<D> data) {
        PageableResponse<D> pageableResponse = new PageableResponse();
        pageableResponse.data = data;
        Pageable pageable = page.getPageable();
        pageableResponse.addPagination(pageable.getPageNumber(), pageable.getPageSize()).setTotal(page.getTotalElements()).setTotalPage(page.getTotalPages());
        pageableResponse.success();
        return pageableResponse;
    }

    public <E, D> PageableResponse<D> response(Page<E> page, Class<D> dto) {
        return this.response(page, this.entityToDTO(page.getContent(), dto));
    }

    public <D> DetailsResponse<D> response(D object) {
        DetailsResponse<D> detailsResponse = new DetailsResponse();
        detailsResponse.data = object;
        detailsResponse.success();
        return detailsResponse;
    }

    public <E, D> DetailsResponse<D> response(E source, Class<D> dto, String message) {
        DetailsResponse<D> detailsResponse = new DetailsResponse();

        try {
            source = (E)this.getEntityValue(source);
            if (source != null) {
                detailsResponse.data = this.convertEntityToDTO(source, dto);
                detailsResponse.success();
            }
        } catch (ObjectCopierException e) {
            detailsResponse.addErrorMessage(e.getMessage());
        }

        if (message != null && detailsResponse.data == null) {
            detailsResponse.addErrorMessage(message);
            detailsResponse.error();
        }

        return detailsResponse;
    }

    public <E, D> DetailsResponse<D> response(E source, Class<D> dto) {
        return this.response(source, dto, (String)null);
    }

    public <D> BulkResponse<D> response(BulkErrorValidEntities<D, ?> processed, Class<D> dto) {
        BulkResponse<D> bulkResponse = new BulkResponse();
        processed.addSuccessDataList(this.entityToDTO(processed.entityList, dto));
        bulkResponse.status = Status.partial;
        bulkResponse.code = "1212";
        if (processed.success != null && processed.success.size() != 0) {
            if (processed.failed == null) {
                bulkResponse.status = Status.success;
                bulkResponse.code = "1200";
            }
        } else {
            bulkResponse.status = Status.error;
            bulkResponse.code = "1510";
            processed.success = null;
        }

        bulkResponse.success = processed.success;
        bulkResponse.failed = processed.failed;
        return bulkResponse;
    }

    public static ResponseProcessor instance() {
        return new ResponseProcessor();
    }

    public <E, D> D entityToDTO(E entity, Class<D> dto) {
        try {
            return (D)this.convertEntityToDTO(entity, dto);
        } catch (ObjectCopierException var4) {
            return null;
        }
    }

    public MessageResponse error(String message, String errorCode) {
        return this.response((String)null, errorCode).errorMessage(message).status(Status.error);
    }

    public MessageResponse error(String message) {
        return this.error(message, "1510");
    }

    public static MessageResponse unknownError() {
        return instance().error(ExceptionMessage.unknownError, "1511");
    }

    public static MessageResponse notFound(String message) {
        return errorMessage(message).setCode("1404");
    }

    public static MessageResponse notFound() {
        return notFound(ExceptionMessage.notFound);
    }

    public static MessageResponse badRequest(String message) {
        return errorMessage(message).setCode("1400");
    }

    public static MessageResponse badRequest() {
        return badRequest(ExceptionMessage.badRequest);
    }

    public static MessageResponse unauthorized() {
        return unauthorized(ExceptionMessage.unauthorized);
    }

    public static MessageResponse unauthorized(String message) {
        return errorMessage(message).setCode("1401");
    }

    public static MessageResponse forbidden(String message) {
        return errorMessage(message).setCode("1403");
    }

    public static MessageResponse forbidden() {
        return forbidden(ExceptionMessage.forbidden);
    }

    public static MessageResponse codeError(String message) {
        return errorMessage(message).setCode("1500");
    }

    public static MessageResponse codeError() {
        return codeError(ExceptionMessage.codeError);
    }

    public static MessageResponse validationError(String message) {
        return instance().error(message, "1600");
    }

    public static MessageResponse validationError() {
        return validationError(ExceptionMessage.validationError);
    }

    public static MessageResponse errorMessage(String message) {
        return instance().error(message, "1510");
    }

    public static MessageResponse successMessage(String message) {
        return instance().response(message, "1200");
    }

    public static MessageResponse otherError(String message) {
        return errorMessage(message).setCode("1512");
    }
}

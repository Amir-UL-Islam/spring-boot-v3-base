package com.problemfighter.java.oc.copier;

import com.problemfighter.java.oc.annotation.DataMapping;
import com.problemfighter.java.oc.annotation.DataMappingInfo;
import com.problemfighter.java.oc.common.InitCustomProcessor;
import com.problemfighter.java.oc.common.OCConstant;
import com.problemfighter.java.oc.common.ObjectCopierException;
import com.problemfighter.java.oc.common.ProcessCustomCopy;
import com.problemfighter.java.oc.data.CopyReport;
import com.problemfighter.java.oc.data.CopyReportError;
import com.problemfighter.java.oc.data.CopySourceDstField;
import com.problemfighter.java.oc.data.ObjectCopierInfoDetails;
import com.problemfighter.java.oc.reflection.ReflectionProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class ObjectCopier {
    private ReflectionProcessor reflectionProcessor = new ReflectionProcessor();
    private LinkedHashMap<String, CopyReport> errorReports = new LinkedHashMap();
    public InitCustomProcessor initCustomProcessor = null;
    private final ThreadLocal<Set<String>> copyGuard = ThreadLocal.withInitial(HashSet::new);

    private void addReport(String name, String errorType, String nestedKey) {
        if (name == null) {
            name = "Source or Destination";
        }

        if (nestedKey == null) {
            this.errorReports.put(name, new CopyReport(name, errorType));
        } else if (this.errorReports.get(nestedKey) != null) {
            ((CopyReport) this.errorReports.get(nestedKey)).addNestedReport(new CopyReport(name, errorType));
        }

    }

    public LinkedHashMap<String, CopyReport> getErrorReports() {
        return this.errorReports;
    }

    public LinkedHashMap<String, String> validateObject(Object object) {
        LinkedHashMap<String, String> errors = new LinkedHashMap<>();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        for (ConstraintViolation<Object> violation : validator.validate(object, new Class[0])) {
            for (Path.Node node : violation.getPropertyPath()) {
                errors.put(node.getName(), violation.getMessage());
            }
        }

        return errors;
    }

    private Boolean isValidateTypeOrReport(CopySourceDstField copySourceDstField, String nestedKey) {
        boolean isValid = false;
        if (copySourceDstField.source == null) {
            this.addReport(copySourceDstField.sourceFieldName, CopyReportError.DST_PROPERTY_UNAVAILABLE.label, nestedKey);
        } else if (copySourceDstField.destination == null) {
            this.addReport(copySourceDstField.sourceFieldName, CopyReportError.DST_PROPERTY_UNAVAILABLE.label, nestedKey);
        } else if (!this.isCompatibleForCopy(copySourceDstField.source.getType(), copySourceDstField.destination.getType())) {
            this.addReport(copySourceDstField.source.getName(), CopyReportError.DATA_TYPE_MISMATCH.label, nestedKey);
        } else {
            isValid = true;
        }

        return isValid;
    }

    private boolean isCompatibleForCopy(Class<?> sourceType, Class<?> destinationType) {
        if (sourceType == destinationType || destinationType.isAssignableFrom(sourceType)) {
            return true;
        }

        if (this.reflectionProcessor.isPrimitive(sourceType) || this.reflectionProcessor.isPrimitive(destinationType)) {
            return false;
        }

        if (sourceType.isEnum() || destinationType.isEnum()) {
            return false;
        }

        if (this.reflectionProcessor.isList(sourceType) && this.reflectionProcessor.isList(destinationType)) {
            return true;
        }

        if (this.reflectionProcessor.isSet(sourceType) && this.reflectionProcessor.isSet(destinationType)) {
            return true;
        }

        if (this.reflectionProcessor.isQueue(sourceType) && this.reflectionProcessor.isQueue(destinationType)) {
            return true;
        }

        if (this.reflectionProcessor.isMap(sourceType) && this.reflectionProcessor.isMap(destinationType)) {
            return true;
        }

        boolean sourceIsCollectionLike = this.reflectionProcessor.isList(sourceType) || this.reflectionProcessor.isSet(sourceType) || this.reflectionProcessor.isQueue(sourceType) || this.reflectionProcessor.isMap(sourceType);
        boolean destinationIsCollectionLike = this.reflectionProcessor.isList(destinationType) || this.reflectionProcessor.isSet(destinationType) || this.reflectionProcessor.isQueue(destinationType) || this.reflectionProcessor.isMap(destinationType);
        if (sourceIsCollectionLike || destinationIsCollectionLike) {
            return false;
        }

        // Allow deep object copy for user-domain classes (DTO <-> Entity) but avoid arbitrary java.* cross-type mapping.
        return !sourceType.getName().startsWith("java.") && !destinationType.getName().startsWith("java.");
    }

    private Boolean isDataMapperAnnotationAvailable(Field field) {
        return field.isAnnotationPresent(DataMapping.class);
    }

    private Boolean isFieldCustomCall(Field field) {
        return this.isDataMapperAnnotationAvailable(field) ? ((DataMapping) field.getAnnotation(DataMapping.class)).customProcess() : false;
    }

    private String getSourceFieldName(Field field, Boolean isStrict) {
        if (this.isDataMapperAnnotationAvailable(field)) {
            return ((DataMapping) field.getAnnotation(DataMapping.class)).source();
        } else {
            return !isStrict ? field.getName() : null;
        }
    }

    private Boolean isDataMapperAnnotationAvailable(List<Field> fields) {
        for (Field field : fields) {
            if (this.isDataMapperAnnotationAvailable(field)) {
                return true;
            }
        }

        return false;
    }

    private Boolean isDataMappingInfoAnnotation(Class<?> klass) {
        return klass.isAnnotationPresent(DataMappingInfo.class);
    }

    private Boolean isStrictMapping(Class<?> klass) {
        return this.isDataMappingInfoAnnotation(klass) ? ((DataMappingInfo) klass.getAnnotation(DataMappingInfo.class)).isStrict() : OCConstant.isStrictCopy;
    }

    private String copierDefaultName(Class<?> klass) {
        return this.isDataMappingInfoAnnotation(klass) ? ((DataMappingInfo) klass.getAnnotation(DataMappingInfo.class)).name() : "anonymous";
    }

    private Class<?> customProcessor(Class<?> klass) {
        return this.isDataMappingInfoAnnotation(klass) ? ((DataMappingInfo) klass.getAnnotation(DataMappingInfo.class)).customProcessor() : null;
    }

    private <S, D> ProcessCustomCopy<S, D> initCustomProcessor(Object object, S sourceObject, D destinationObject) {
        Class<?> callbackClass = this.customProcessor(object.getClass());
        if (callbackClass != null && ProcessCustomCopy.class.isAssignableFrom(callbackClass)) {
            ProcessCustomCopy<S, D> customCopy = null;
            if (this.initCustomProcessor != null) {
                customCopy = this.initCustomProcessor.init(callbackClass, sourceObject, destinationObject);
            } else {
                customCopy = (ProcessCustomCopy) this.reflectionProcessor.newInstance(callbackClass);
            }

            return customCopy;
        } else {
            return null;
        }
    }

    private <S, D> ObjectCopierInfoDetails<?, ?> processInfo(Object object, S sourceObject, D destinationObject) {
        ObjectCopierInfoDetails<S, D> objectCopierInfo = new ObjectCopierInfoDetails<>();
        objectCopierInfo.isStrictMapping = this.isStrictMapping(object.getClass());
        objectCopierInfo.mappingClassName = this.copierDefaultName(object.getClass());
        objectCopierInfo.processCustomCopy = this.initCustomProcessor(object, sourceObject, destinationObject);
        return objectCopierInfo;
    }

    private Field getField(Field field, CopySourceDstField copySourceDstField) {
        copySourceDstField.sourceFieldName = this.getSourceFieldName(field, copySourceDstField.isStrictMapping);
        if (copySourceDstField.sourceFieldName != null && copySourceDstField.dataObject != null) {
            Field sourceField = this.reflectionProcessor.getAnyFieldFromObject(copySourceDstField.dataObject, copySourceDstField.sourceFieldName);
            if (sourceField != null) {
                copySourceDstField.isCallback = this.isFieldCustomCall(field);
            }

            return sourceField;
        } else {
            return null;
        }
    }

    private CopySourceDstField getCopiableSrcDstField(CopySourceDstField copySourceDstField) {
        if (copySourceDstField.destination != null) {
            copySourceDstField.source = this.getField(copySourceDstField.destination, copySourceDstField);
        } else if (copySourceDstField.source != null) {
            copySourceDstField.destination = this.getField(copySourceDstField.source, copySourceDstField);
        }

        return copySourceDstField;
    }

    private List<CopySourceDstField> dstAnnotatedNotSrc(List<Field> dstFields, Object dataObject, String nestedKey, ObjectCopierInfoDetails objectCopierInfoDetails) {
        List<CopySourceDstField> list = new ArrayList<>();

        for (Field field : dstFields) {
            CopySourceDstField copySourceDstField = new CopySourceDstField();
            copySourceDstField.setDataObject(dataObject);
            copySourceDstField.setDestination(field);
            copySourceDstField.isStrictMapping = objectCopierInfoDetails.isStrictMapping;
            this.getCopiableSrcDstField(copySourceDstField);
            if (this.isValidateTypeOrReport(copySourceDstField, nestedKey)) {
                list.add(copySourceDstField);
            }
        }

        return list;
    }

    private List<CopySourceDstField> srcAnnotatedNotDst(List<Field> srcFields, Object dataObject, String nestedKey, ObjectCopierInfoDetails objectCopierInfoDetails) {
        List<CopySourceDstField> list = new ArrayList();

        for (Field field : srcFields) {
            CopySourceDstField copySourceDstField = new CopySourceDstField();
            copySourceDstField.setDataObject(dataObject);
            copySourceDstField.setSource(field);
            copySourceDstField.isStrictMapping = objectCopierInfoDetails.isStrictMapping;
            copySourceDstField = this.getCopiableSrcDstField(copySourceDstField);
            if (this.isValidateTypeOrReport(copySourceDstField, nestedKey)) {
                list.add(copySourceDstField);
            }
        }

        return list;
    }

    private List<CopySourceDstField> srcDstNotAnnotated(List<Field> fields, Object dataObject, String nestedKey, ObjectCopierInfoDetails objectCopierInfoDetails) {
        return this.dstAnnotatedNotSrc(fields, dataObject, nestedKey, objectCopierInfoDetails);
    }

    private <S, D> ObjectCopierInfoDetails<?, ?> processDetailsInfo(S sourceObject, D destinationObject, String nestedKey) {
        Class<?> sourceClass = sourceObject.getClass();
        Class<?> destinationClass = destinationObject.getClass();
        ObjectCopierInfoDetails<?, ?> objectCopierInfoDetails = this.processInfo(destinationObject, sourceObject, destinationObject);
        objectCopierInfoDetails.amIDestination = true;
        List<Field> toKlassFields = this.reflectionProcessor.getAllField(destinationClass);
        if (!this.isDataMappingInfoAnnotation(destinationClass) && !this.isDataMapperAnnotationAvailable(toKlassFields)) {
            objectCopierInfoDetails = this.processInfo(sourceObject, sourceObject, destinationObject);
            List<Field> fromObjectFields = this.reflectionProcessor.getAllField(sourceClass);
            if (!this.isDataMappingInfoAnnotation(sourceClass) && !this.isDataMapperAnnotationAvailable(fromObjectFields)) {
                if (!objectCopierInfoDetails.isStrictMapping) {
                    objectCopierInfoDetails.copySourceDstFields = this.srcDstNotAnnotated(toKlassFields, sourceObject, nestedKey, objectCopierInfoDetails);
                }

            } else {
                objectCopierInfoDetails = this.processInfo(sourceObject, sourceObject, destinationObject);
                objectCopierInfoDetails.amIDestination = false;
                objectCopierInfoDetails.copySourceDstFields = this.srcAnnotatedNotDst(fromObjectFields, destinationObject, nestedKey, objectCopierInfoDetails);
            }
        } else {
            objectCopierInfoDetails.copySourceDstFields = this.dstAnnotatedNotSrc(toKlassFields, sourceObject, nestedKey, objectCopierInfoDetails);
        }
        return objectCopierInfoDetails;
    }

    private Class<?> getClassFromType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class<?>) {
                return (Class<?>) rawType;
            }
        }

        return null;
    }

    private Class<?> getParameterizedType(Field field, int index) {
        if (field == null) {
            return null;
        }

        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            return null;
        }

        Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
        if (index < 0 || index >= typeArguments.length) {
            return null;
        }

        return this.getClassFromType(typeArguments[index]);
    }

    private Object getObjectNewInstance(Class<?> klass) {
        return klass == null ? null : this.reflectionProcessor.newInstance(klass);
    }

    private Object getObjectNewInstanceOrDefault(Class<?> klass, Object source) {
        Object instance = this.getObjectNewInstance(klass);
        return instance != null ? instance : (source != null ? this.getObjectNewInstance(source) : null);
    }

    private Object processMap(Object sourceObject, Class<?> destinationProperty, Field destinationField) throws IllegalAccessException, ObjectCopierException {
        if (sourceObject != null && destinationProperty != null) {
            Map<?, ?> map = (Map) sourceObject;
            Map response = this.reflectionProcessor.instanceOfMap(destinationProperty);
            Class<?> mapKeyType = this.getParameterizedType(destinationField, 0);
            Class<?> mapValueType = this.getParameterizedType(destinationField, 1);

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object sourceKey = entry.getKey();
                Object sourceValue = entry.getValue();
                Object mappedKey = sourceKey == null ? null : this.processAndGetValue(sourceKey, this.getObjectNewInstanceOrDefault(mapKeyType, sourceKey), mapKeyType != null ? mapKeyType : sourceKey.getClass(), null);
                Object mappedValue = sourceValue == null ? null : this.processAndGetValue(sourceValue, this.getObjectNewInstanceOrDefault(mapValueType, sourceValue), mapValueType != null ? mapValueType : sourceValue.getClass(), null);
                response.put(mappedKey, mappedValue);
            }

            return response.size() == 0 ? null : response;
        } else {
            return null;
        }
    }

    private Object processSet(Object sourceObject, Class<?> destinationProperty, Field destinationField) throws ObjectCopierException, IllegalAccessException {
        if (sourceObject != null && destinationProperty != null) {
            Set<?> list = (Set) sourceObject;
            Set response = this.reflectionProcessor.instanceOfSet(destinationProperty);
            Class<?> elementType = this.getParameterizedType(destinationField, 0);

            for (Object data : list) {
                if (data != null) {
                    response.add(this.processAndGetValue(data, this.getObjectNewInstanceOrDefault(elementType, data), elementType != null ? elementType : data.getClass(), null));
                }
            }

            if (response.size() == 0) {
                return null;
            } else {
                return response;
            }
        } else {
            return null;
        }
    }

    private Object processQueue(Object sourceObject, Class<?> destinationProperty, Field destinationField) throws ObjectCopierException, IllegalAccessException {
        if (sourceObject != null && destinationProperty != null) {
            Queue<?> list = (Queue) sourceObject;
            Queue response = this.reflectionProcessor.instanceOfQueue(destinationProperty);
            Class<?> elementType = this.getParameterizedType(destinationField, 0);

            for (Object data : list) {
                if (data != null) {
                    response.add(this.processAndGetValue(data, this.getObjectNewInstanceOrDefault(elementType, data), elementType != null ? elementType : data.getClass(), null));
                }
            }

            if (response.size() == 0) {
                return null;
            } else {
                return response;
            }
        } else {
            return null;
        }
    }

    private Object processList(Object sourceObject, Class<?> destinationProperty, Field destinationField) throws IllegalAccessException, ObjectCopierException {
        if (sourceObject != null && destinationProperty != null) {
            Collection<?> list = (Collection) sourceObject;
            Collection response = this.reflectionProcessor.instanceOfList(destinationProperty);
            Class<?> elementType = this.getParameterizedType(destinationField, 0);

            for (Object data : list) {
                if (data != null) {
                    response.add(this.processAndGetValue(data, this.getObjectNewInstanceOrDefault(elementType, data), elementType != null ? elementType : data.getClass(), null));
                }
            }

            if (response.size() == 0) {
                return null;
            } else {
                return response;
            }
        } else {
            return null;
        }
    }

    private Object processAndGetValue(Object source, Object destination, Class<?> klass, Field destinationField) throws ObjectCopierException, IllegalAccessException {
        if (source == null && destination != null) {
            return destination;
        } else if (source == null) {
            return null;
        } else if (this.reflectionProcessor.isPrimitive(source.getClass())) {
            return source;
        } else if (source.getClass().isEnum()) {
            return source;
        } else if (this.reflectionProcessor.isList(source.getClass())) {
            return this.processList(source, klass, destinationField);
        } else if (this.reflectionProcessor.isMap(source.getClass())) {
            return this.processMap(source, klass, destinationField);
        } else if (this.reflectionProcessor.isSet(source.getClass())) {
            return this.processSet(source, klass, destinationField);
        } else {
            if (this.reflectionProcessor.isQueue(source.getClass())) {
                return this.processQueue(source, klass, destinationField);
            }

            Object destinationObject = destination != null ? destination : this.getObjectNewInstance(klass);
            if (destinationObject == null) {
                throw new ObjectCopierException("Unable to instantiate destination type: " + (klass != null ? klass.getName() : "null"));
            }

            String guardKey = System.identityHashCode(source) + ":" + System.identityHashCode(destinationObject);
            Set<String> activeCopies = this.copyGuard.get();
            if (activeCopies.contains(guardKey)) {
                return destinationObject;
            }

            activeCopies.add(guardKey);
            try {
                return this.processCopy(source, destinationObject, destinationObject.getClass().getSimpleName());
            } finally {
                activeCopies.remove(guardKey);
                if (activeCopies.isEmpty()) {
                    this.copyGuard.remove();
                }
            }
        }
    }

    private Object getObjectNewInstance(Object object) {
        return this.reflectionProcessor.newInstance(object.getClass());
    }

    private Object getFieldValue(Object data, Field field) throws IllegalAccessException {
        if (field != null && data != null) {
            field.setAccessible(true);
            return field.get(data);
        } else {
            return null;
        }
    }

    private Object getFieldValueOrObject(Object data, Field field) throws IllegalAccessException {
        Object fieldValue = this.getFieldValue(data, field);
        return fieldValue == null ? this.reflectionProcessor.newInstance(field.getType()) : fieldValue;
    }

    private <S, D> D processCopy(S source, D destination, String nestedKey) throws ObjectCopierException {
        try {
            if (source != null && destination != null) {
                ObjectCopierInfoDetails<?, ?> details = this.processDetailsInfo(source, destination, nestedKey);
                details.callGlobalCallBack(source, destination);

                for (CopySourceDstField copySourceDstField : details.copySourceDstFields) {
                    Object sourceValue = this.getFieldValue(source, copySourceDstField.source);
                    Object destinationValue = this.getFieldValueOrObject(destination, copySourceDstField.destination);
                    copySourceDstField.destination.setAccessible(true);
                    copySourceDstField.destination.set(destination, this.processAndGetValue(sourceValue, destinationValue, copySourceDstField.destination.getType(), copySourceDstField.destination));
                }

                return destination;
            } else {
                return null;
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new ObjectCopierException(e.getMessage());
        }
    }

    private <S, D> D processCopy(S source, Class<D> klass, String nestedKey) throws ObjectCopierException {
        D toInstance = (D) this.reflectionProcessor.newInstance(klass);
        if (source != null && toInstance == null) {
            throw new ObjectCopierException("Unable to instantiate destination type: " + (klass != null ? klass.getName() : "null"));
        }
        return (D) this.processCopy(source, toInstance, nestedKey);
    }

    public <S, D> D copy(S source, D destination) throws ObjectCopierException {
        return (D) this.processCopy(source, destination, (String) null);
    }

    public <S, D> D copy(S source, Class<D> destination) throws ObjectCopierException {
        return (D) this.processCopy(source, destination, (String) null);
    }
}

package com.problemfighter.java.oc.data;

import com.problemfighter.java.oc.common.ProcessCustomCopy;
import com.problemfighter.java.oc.reflection.ReflectionProcessor;
import java.util.ArrayList;
import java.util.List;

public class ObjectCopierInfoDetails<S, D> {
    public Boolean isStrictMapping = true;
    public Boolean amIDestination = true;
    public ProcessCustomCopy<Object, Object> processCustomCopy;
    public String mappingClassName;
    private ReflectionProcessor reflectionProcessor = new ReflectionProcessor();
    public List<CopySourceDstField> copySourceDstFields = new ArrayList();

    public Boolean callMeAsDst(Object source, Object destination) {
        if (this.reflectionProcessor.isMethodExist(this.processCustomCopy.getClass(), "meAsDst", new Class[]{destination.getClass(), source.getClass()})) {
            this.reflectionProcessor.invokeMethod(this.processCustomCopy, "meAsDst", new Object[]{destination, source});
        } else {
            if (!this.reflectionProcessor.isMethodExist(this.processCustomCopy.getClass(), "meAsDst", new Class[]{source.getClass(), destination.getClass()})) {
                return false;
            }

            this.reflectionProcessor.invokeMethod(this.processCustomCopy, "meAsDst", new Object[]{source, destination});
        }

        return true;
    }

    public Boolean callMeAsSst(Object source, Object destination) {
        if (this.reflectionProcessor.isMethodExist(this.processCustomCopy.getClass(), "meAsSrc", new Class[]{destination.getClass(), source.getClass()})) {
            this.reflectionProcessor.invokeMethod(this.processCustomCopy, "meAsSrc", new Object[]{destination, source});
        } else {
            if (!this.reflectionProcessor.isMethodExist(this.processCustomCopy.getClass(), "meAsSrc", new Class[]{source.getClass(), destination.getClass()})) {
                return false;
            }

            this.reflectionProcessor.invokeMethod(this.processCustomCopy, "meAsSrc", new Object[]{source, destination});
        }

        return true;
    }

    public void callGlobalCallBack(Object source, Object destination) {
        if (this.processCustomCopy != null) {
            Boolean isSuccess = false;
            if (this.amIDestination) {
                isSuccess = this.callMeAsDst(source, destination);
            } else {
                isSuccess = this.callMeAsSst(source, destination);
            }

            if (!isSuccess) {
                ProcessCustomCopy var10000 = this.processCustomCopy;
                String var10001 = source.getClass().getSimpleName();
                var10000.whyNotCalled("Method not found with the parameter " + var10001 + " and " + destination.getClass().getSimpleName());
            }
        }

    }
}

package com.security.base.privilege;

import com.problemfighter.pfspring.restapi.inter.CopyInterceptor;
import org.springframework.stereotype.Component;

@Component
public class PrivilegeInterceptor implements CopyInterceptor<Privilege, PrivilegeDTO, PrivilegeDTO> {

    @Override
    public void meAsSrc(PrivilegeDTO source, Privilege destination) {
        // No custom logic needed - all fields (name) are auto-mapped by library
    }

    @Override
    public void meAsDst(Privilege source, PrivilegeDTO destination) {
        // No custom logic needed - all fields (id, name) are auto-mapped by library
    }
}

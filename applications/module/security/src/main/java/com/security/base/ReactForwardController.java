package com.security.base;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * Serve Reacts index.html for all requests that are not relevant for the backend.
 */
@Controller
public class ReactForwardController {

    // Keep static asset paths out of SPA forwarding so admin console files are served directly.
    @GetMapping("{path:^(?!api|public|assets|css|js|images|swagger|v3)[^.]*}/**")
    public String handleForward() {
        return "forward:/";
    }

}

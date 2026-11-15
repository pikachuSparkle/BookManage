package com.study.initialize;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;
import jakarta.servlet.ServletContext;

public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
    @Override
    protected String getDispatcherWebApplicationContextSuffix() {
        return "dispatcher";
    }

    @Override
    protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
        // Set the context default as well, before any filter can access request parameters.
        servletContext.setRequestCharacterEncoding("UTF-8");
        servletContext.setResponseCharacterEncoding("UTF-8");
        insertFilters(servletContext, new CharacterEncodingFilter("UTF-8", true));
    }
}

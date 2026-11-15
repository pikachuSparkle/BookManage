package com.study.initialize;

import com.study.config.MainConfiguration;
import com.study.config.MvcConfiguration;
import com.study.config.SecurityConfiguration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MvcInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{MainConfiguration.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{MvcConfiguration.class, SecurityConfiguration.class};
    }

    @Override
    protected String[] getServletMappings(){
        return new String[]{"/"};
    }


}

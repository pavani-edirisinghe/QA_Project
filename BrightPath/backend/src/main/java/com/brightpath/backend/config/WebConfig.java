package com.brightpath.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String profileImageDir;

    @Value("${course.upload-dir}")
    private String courseImageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve profile images under /uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + profileImageDir + "/");

        // Serve course images under /courses/**
        registry.addResourceHandler("/courses/**")
                .addResourceLocations("file:" + courseImageDir + "/");
    }
}

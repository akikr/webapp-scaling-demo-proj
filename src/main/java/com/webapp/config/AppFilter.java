package com.webapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static com.webapp.config.AppFilterLogger.logRequest;
import static com.webapp.config.AppFilterLogger.logResponse;

@Component
public class AppFilter extends OncePerRequestFilter {

    private final AppFilterProperties appFilterProperties;

    public AppFilter(AppFilterProperties appFilterProperties) {
        this.appFilterProperties = appFilterProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = requestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = responseWrapper(response);

        if (appFilterProperties.enabled())
            logRequest(requestWrapper, appFilterProperties);

        filterChain.doFilter(requestWrapper, responseWrapper);

        if (appFilterProperties.enabled())
            logResponse(responseWrapper, appFilterProperties);

        responseWrapper.copyBodyToResponse();
    }

    private ContentCachingRequestWrapper requestWrapper(HttpServletRequest httpServletRequest) {
        if (httpServletRequest instanceof ContentCachingRequestWrapper requestWrapper) {
            return requestWrapper;
        }
        return new ContentCachingRequestWrapper(httpServletRequest);
    }

    private ContentCachingResponseWrapper responseWrapper(HttpServletResponse httpServletResponse) {
        if (httpServletResponse instanceof ContentCachingResponseWrapper responseWrapper) {
            return responseWrapper;
        }
        return new ContentCachingResponseWrapper(httpServletResponse);
    }
}

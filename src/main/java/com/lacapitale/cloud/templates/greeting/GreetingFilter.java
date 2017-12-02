package com.lacapitale.cloud.templates.greeting;

import javax.servlet.*;
import java.io.IOException;

public class GreetingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("Greetings from filter!");
    }

    @Override
    public void destroy() {

    }
}
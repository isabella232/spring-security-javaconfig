/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.config.annotation.web;


import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.AuthenticationBuilder;
import org.springframework.security.config.annotation.web.SpringSecurityFilterChainBuilder.IgnoredRequestRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author Rob Winch
 *
 */
public abstract class WebSecurityConfigurerAdapter implements WebSecurityConfigurer {
    @Autowired
    private ApplicationContext context;

    private AuthenticationBuilder authenticationRegistry = new AuthenticationBuilder();
    private AuthenticationManager authenticationManager;
    private HttpConfiguration springSecurityFilterChain;

    protected abstract AuthenticationManager authenticationManager(AuthenticationBuilder authentication) throws Exception;

    protected void applyDefaults(HttpConfiguration http) throws Exception {
        http.applyDefaultConfigurators();
        authorizeUrls(http.authorizeUrls());
    }

    protected abstract void authorizeUrls(ExpressionUrlAuthorizations interceptUrls);

    private HttpConfiguration springSecurityFilterChain() throws Exception {
        if(springSecurityFilterChain == null) {
            springSecurityFilterChain = new HttpConfiguration(getAuthenticationManager());
        }
        return springSecurityFilterChain;
    }

    public HttpConfiguration httpConfiguration() throws Exception {
        HttpConfiguration springSecurityFilterChain = springSecurityFilterChain();
        springSecurityFilterChain.setSharedObject(UserDetailsService.class, getUserDetailsService());
        applyDefaults(springSecurityFilterChain);
        configure(springSecurityFilterChain);
        return springSecurityFilterChain;
    }

    @Bean(name=BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager() throws Exception {
        return getAuthenticationManager();
    }

    private AuthenticationManager getAuthenticationManager() throws Exception {
        if(authenticationManager == null) {
            authenticationManager = authenticationManager(authenticationRegistry);
            if(authenticationManager == null) {
                authenticationManager = getBeanExcluding(AuthenticationManager.class, BeanIds.AUTHENTICATION_MANAGER);
            }
        }
        return authenticationManager;
    }

    @Bean(name=BeanIds.USER_DETAILS_SERVICE)
    public UserDetailsService userDetailsService() throws Exception {
        return getUserDetailsService();
    }

    private UserDetailsService getUserDetailsService() throws Exception {
        return userDetailsService(authenticationRegistry);
    }

    protected UserDetailsService userDetailsService(AuthenticationBuilder authenticationRegistry) {
        return authenticationRegistry.userDetailsService();
    }

    protected void performConfigure(SpringSecurityFilterChainBuilder securityFilterChains){

    }

    public void init(WebSecurityConfiguration builder) throws Exception {
        SpringSecurityFilterChainBuilder securityFilterChains = builder.springSecurityFilterChainBuilder();
        ignoredRequests(securityFilterChains.ignoring());
        performConfigure(securityFilterChains);
        securityFilterChains
            .securityFilterChains(httpConfiguration());
    }

    public void configure(WebSecurityConfiguration builder) throws Exception {
    }

    protected void ignoredRequests(IgnoredRequestRegistry ignoredRequests) {

    }

    private <T> T getBeanExcluding(Class<T> clazz, String beanNameToExclude) {
        String[] beanNames = context.getBeanNamesForType(clazz);
        if(beanNames.length == 1) {
            return context.getBean(beanNames[0],clazz);
        }
        if(beanNames.length == 2) {
            if(beanNameToExclude.equals(beanNames[0])) {
                return context.getBean(beanNames[1],clazz);
            }
            if(beanNameToExclude.equals(beanNames[1])) {
                return context.getBean(beanNames[0],clazz);
            }
        }
        throw new IllegalStateException("Failed to find bean of type " + clazz
                + " excluding " + beanNameToExclude + ". Got "
                + Arrays.asList(beanNames));
    }

    protected abstract void configure(HttpConfiguration http) throws Exception;
}
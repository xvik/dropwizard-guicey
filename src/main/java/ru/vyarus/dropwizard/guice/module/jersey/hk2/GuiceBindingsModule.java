/*
 * Copyright 2014 Squarespace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.vyarus.dropwizard.guice.module.jersey.hk2;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.RequestScoped;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.process.AsyncContext;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;

import static ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent;

/**
 * Registers important services from HK2 context, making them available for injection in guice beans.
 * <ul>
 * <li>{@link javax.ws.rs.core.Application}
 * <li>{@link javax.ws.rs.ext.Providers}
 * <li>{@link javax.ws.rs.core.UriInfo}
 * <li>{@link javax.ws.rs.core.HttpHeaders}
 * <li>{@link javax.ws.rs.core.SecurityContext}
 * <li>{@link javax.ws.rs.core.Request}
 * <li>{@link org.glassfish.jersey.server.ContainerRequest}
 * <li>{@link org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider}
 * <li>{@link org.glassfish.jersey.server.internal.process.AsyncContext}</li>
 * </ul>
 * {@link org.glassfish.hk2.api.ServiceLocator} is registered by
 * {@link ru.vyarus.dropwizard.guice.module.jersey.GuiceFeature}
 *
 * @author Vyacheslav Rusakov
 * @since 15.11.2014
 */
public class GuiceBindingsModule extends AbstractModule {

    @Override
    protected void configure() {
        bindJerseyComponent(binder(), MultivaluedParameterExtractorProvider.class);
        bindJerseyComponent(binder(), Application.class);
        bindJerseyComponent(binder(), Providers.class);

        bindJerseyComponent(binder(), UriInfo.class).in(RequestScoped.class);
        bindJerseyComponent(binder(), HttpHeaders.class).in(RequestScoped.class);
        bindJerseyComponent(binder(), SecurityContext.class).in(RequestScoped.class);
        bindJerseyComponent(binder(), Request.class).in(RequestScoped.class);
        bindJerseyComponent(binder(), ContainerRequest.class).in(RequestScoped.class);
        bindJerseyComponent(binder(), AsyncContext.class).in(RequestScoped.class);
    }
}

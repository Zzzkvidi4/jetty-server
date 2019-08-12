package com.zzzkvidi4.server;

import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.QoSFilter;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class Application {
    public static void main(@NotNull String[] args) throws Exception {
        Server server = new Server();
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory();
        ServerConnector connector = new ServerConnector(server, httpConnectionFactory);
        connector.setPort(8010);
        server.setConnectors(new Connector[] {connector});

        LoginService loginService = new JDBCLoginService("LoginService", "src/main/resources/login-service.config");
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        FilterHolder filterHolder = new FilterHolder(new QoSFilter() {
            @Override
            public void doFilter(@NotNull ServletRequest request, @NotNull ServletResponse response, @NotNull FilterChain chain) throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                if (httpRequest.getMethod().equalsIgnoreCase("POST")) {
                    super.doFilter(request, response, chain);
                } else {
                    chain.doFilter(request, response);
                }
            }
        });
        filterHolder.setInitParameter("maxRequests", "1");
        ServletContextHandler context = new ServletContextHandler();
        context.addFilter(filterHolder, "/product/*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(ProductServlet.class, "/product");
        context.addServlet(CustomServlet.class, "/hello");


        /*ServletContextHandler staticResourceContext = new ServletContextHandler(
                ServletContextHandler.NO_SESSIONS
        );
        context.setContextPath("/file");
        final URL resource = Application.class.getResource("/static");
        context.setBaseResource(Resource.newResource(resource.toExternalForm()));
        context.setWelcomeFiles(new String[]{"/static"});
        context.addServlet(new ServletHolder("default", DefaultServlet.class), "/");*/

        HandlerCollection handlerCollection = new HandlerCollection(context/*, staticResourceContext*/);
        securityHandler.setHandler(handlerCollection);
        securityHandler.setLoginService(loginService);
        server.addBean(loginService);
        Authenticator authenticator = new BasicAuthenticator();
        securityHandler.setAuthenticator(authenticator);
        //securityHandler.setHandler(servletHandler);
        securityHandler.setConstraintMappings(
                asList(
                        createConstraintMapping("productGet", asList(Role.GUEST, Role.MANAGER), "/product/*", "GET"),
                        createConstraintMapping("productPost", singletonList(Role.MANAGER), "/product/*", "*")/*,
                        createConstraintMapping("auth", asList(Role.GUEST, Role.MANAGER), "/file/*", "*")*/
                )
        );


        server.setHandler(securityHandler);
        server.start();
    }

    @NotNull
    private static ConstraintMapping createConstraintMapping(@NotNull String constraintName, @NotNull List<Role> roles, @NotNull String path, @Nullable String methodName) {
        Constraint constraint = new Constraint();
        constraint.setName(constraintName);
        constraint.setAuthenticate(true);
        constraint.setRoles(roles.stream().map(Enum::name).toArray(String[]::new));
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setPathSpec(path);
        if (methodName != null) {
            constraintMapping.setMethod(methodName);
        }
        constraintMapping.setConstraint(constraint);
        return constraintMapping;
    }
}

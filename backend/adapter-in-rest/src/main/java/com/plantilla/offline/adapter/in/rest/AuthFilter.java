package com.plantilla.offline.adapter.in.rest;

import com.plantilla.offline.application.auth.AuthUseCase;
import com.plantilla.offline.application.auth.AuthUser;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Optional;

/** Exige sesión en todo endpoint que no esté marcado @Public, y comprueba @RequireRole. */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    public static final String USER_PROPERTY = "plantilla.auth.user";

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext ctx) {
        Method method = resourceInfo.getResourceMethod();
        Class<?> type = resourceInfo.getResourceClass();
        if (method.isAnnotationPresent(Public.class) || type.isAnnotationPresent(Public.class)) {
            return;
        }

        String token = bearer(ctx.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (token == null) {
            deny(ctx, Response.Status.UNAUTHORIZED, "No autenticado");
            return;
        }
        // Búsqueda programática: los filtros JAX-RS no son beans CDI en este despliegue.
        AuthUseCase auth = CDI.current().select(AuthUseCase.class).get();
        Optional<AuthUser> user = auth.authenticate(token);
        if (user.isEmpty()) {
            deny(ctx, Response.Status.UNAUTHORIZED, "Sesión inválida o vencida");
            return;
        }

        RequireRole required = method.getAnnotation(RequireRole.class);
        if (required == null) {
            required = type.getAnnotation(RequireRole.class);
        }
        if (required != null && !required.value().equals(user.get().role())) {
            deny(ctx, Response.Status.FORBIDDEN, "Permiso insuficiente");
            return;
        }
        ctx.setProperty(USER_PROPERTY, user.get());
    }

    static String bearer(String header) {
        if (header == null || header.length() < 8 || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        return header.substring(7).trim();
    }

    private void deny(ContainerRequestContext ctx, Response.Status status, String message) {
        ctx.abortWith(Response.status(status).header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .entity(message).build());
    }
}
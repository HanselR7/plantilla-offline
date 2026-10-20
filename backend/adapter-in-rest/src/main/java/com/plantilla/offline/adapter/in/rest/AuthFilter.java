package com.plantilla.offline.adapter.in.rest;

import com.plantilla.offline.application.auth.AuthUseCase;
import com.plantilla.offline.application.auth.AuthUser;
import com.plantilla.offline.application.syncauth.SyncClientUseCase;
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
import java.util.UUID;

/**
 * Exige credenciales en todo endpoint que no esté marcado @Public.
 * - @SyncAuth: credencial de instalación (Bearer + X-Installation-Id).
 * - Resto: sesión de usuario, con @RequireRole opcional.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    public static final String USER_PROPERTY = "plantilla.auth.user";
    public static final String INSTALLATION_PROPERTY = "plantilla.auth.installation";
    public static final String INSTALLATION_HEADER = "X-Installation-Id";

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

        if (method.isAnnotationPresent(SyncAuth.class) || type.isAnnotationPresent(SyncAuth.class)) {
            authenticateInstallation(ctx, token);
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

    private void authenticateInstallation(ContainerRequestContext ctx, String token) {
        UUID installationId;
        try {
            installationId = UUID.fromString(String.valueOf(ctx.getHeaderString(INSTALLATION_HEADER)).trim());
        } catch (IllegalArgumentException e) {
            deny(ctx, Response.Status.UNAUTHORIZED, "Instalación no identificada");
            return;
        }
        SyncClientUseCase clients = CDI.current().select(SyncClientUseCase.class).get();
        if (!clients.authenticate(installationId, token)) {
            deny(ctx, Response.Status.UNAUTHORIZED, "Credencial de instalación inválida");
            return;
        }
        ctx.setProperty(INSTALLATION_PROPERTY, installationId);
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
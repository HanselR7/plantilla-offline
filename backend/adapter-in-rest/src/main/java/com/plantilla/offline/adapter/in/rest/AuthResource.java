package com.plantilla.offline.adapter.in.rest;

import com.plantilla.offline.application.auth.AuthTokens;
import com.plantilla.offline.application.auth.AuthUseCase;
import com.plantilla.offline.application.auth.AuthUser;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/auth")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    public record LoginRequest(String username, String password) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record CreateUserRequest(String username, String password, String role) {
    }

    @Inject
    AuthUseCase auth;

    @POST
    @Path("/login")
    @Public
    public Response login(LoginRequest req) {
        if (req == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return auth.login(req.username(), req.password())
                .map(t -> Response.ok(tokensBody(t)).build())
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Usuario o contraseña incorrectos").build());
    }

    @POST
    @Path("/refresh")
    @Public
    public Response refresh(RefreshRequest req) {
        if (req == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return auth.refresh(req.refreshToken())
                .map(t -> Response.ok(tokensBody(t)).build())
                .orElse(Response.status(Response.Status.UNAUTHORIZED).entity("Sesión vencida").build());
    }

    @POST
    @Path("/logout")
    public Response logout(@HeaderParam(HttpHeaders.AUTHORIZATION) String header) {
        String token = AuthFilter.bearer(header);
        if (token != null) {
            auth.logout(token);
        }
        return Response.noContent().build();
    }

    @GET
    @Path("/me")
    public Response me(@Context ContainerRequestContext ctx) {
        return Response.ok(userBody((AuthUser) ctx.getProperty(AuthFilter.USER_PROPERTY))).build();
    }

    @POST
    @Path("/users")
    @RequireRole("ADMIN")
    public Response createUser(CreateUserRequest req) {
        if (req == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            AuthUser u = auth.createUser(req.username(), req.password(), req.role());
            return Response.status(Response.Status.CREATED).entity(userBody(u)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    private Map<String, Object> tokensBody(AuthTokens t) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accessToken", t.accessToken());
        body.put("accessExpiresAt", t.accessExpiresAt().toString());
        body.put("refreshToken", t.refreshToken());
        body.put("refreshExpiresAt", t.refreshExpiresAt().toString());
        body.put("user", userBody(t.user()));
        return body;
    }

    private Map<String, Object> userBody(AuthUser u) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", u.id().toString());
        body.put("username", u.username());
        body.put("role", u.role());
        return body;
    }
}
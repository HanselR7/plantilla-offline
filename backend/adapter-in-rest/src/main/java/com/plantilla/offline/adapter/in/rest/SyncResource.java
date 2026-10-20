package com.plantilla.offline.adapter.in.rest;

import com.plantilla.offline.application.sync.SyncBatch;
import com.plantilla.offline.application.sync.SyncChangesUseCase;
import com.plantilla.offline.application.sync.SyncPushUseCase;
import com.plantilla.offline.application.syncauth.SyncClientUseCase;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/sync")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SyncResource {

    public record RegisterClientRequest(UUID installationId, String name) {
    }

    @Inject
    SyncPushUseCase push;

    @Inject
    SyncChangesUseCase changes;

    @Inject
    SyncClientUseCase clients;

    @POST
    @Path("/push")
    @SyncAuth
    @Transactional
    public Response push(SyncBatch batch,
                         @HeaderParam(AuthFilter.INSTALLATION_HEADER) String installationHeader) {
        if (batch == null || batch.operations() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        // El filtro ya comprobó que este encabezado corresponde al token. Aquí se exige
        // que el lote pertenezca a esa misma instalación.
        UUID authenticated;
        try {
            authenticated = UUID.fromString(installationHeader.trim());
        } catch (RuntimeException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (!authenticated.equals(batch.installationId())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("El lote no pertenece a la instalación autenticada").build();
        }
        List<UUID> confirmed = push.push(batch);
        return Response.ok(Map.of("confirmed", confirmed)).build();
    }

    @GET
    @Path("/pull")
    @SyncAuth
    public Response pull(@QueryParam("type") String type,
                         @QueryParam("since") @DefaultValue("0") long since,
                         @QueryParam("limit") @DefaultValue("100") int limit) {
        return changes.changes(type, since, limit)
                .map(page -> Response.ok(page).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).entity("Tipo desconocido").build());
    }

    /** Un administrador de la nube autoriza a un servidor local. El token solo se muestra aquí. */
    @POST
    @Path("/clients")
    @RequireRole("ADMIN")
    public Response register(RegisterClientRequest req) {
        if (req == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            String token = clients.register(req.installationId(), req.name());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("installationId", req.installationId().toString());
            body.put("syncToken", token);
            return Response.status(Response.Status.CREATED).entity(body).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/clients/{id}")
    @RequireRole("ADMIN")
    public Response revoke(@PathParam("id") UUID id) {
        return clients.revoke(id)
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
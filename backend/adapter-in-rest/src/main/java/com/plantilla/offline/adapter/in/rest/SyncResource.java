package com.plantilla.offline.adapter.in.rest;

import com.plantilla.offline.application.sync.SyncBatch;
import com.plantilla.offline.application.sync.SyncPushUseCase;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.plantilla.offline.application.sync.SyncChangesUseCase;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;

@Path("/sync")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SyncResource {

    @Inject
    SyncPushUseCase push;

    @Inject
    SyncChangesUseCase changes;

    @Public
    @POST
    @Path("/push")
    @Transactional
    public Response push(SyncBatch batch) {
        if (batch == null || batch.operations() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<UUID> confirmed = push.push(batch);
        return Response.ok(Map.of("confirmed", confirmed)).build();
    }

    @Public
    @GET
    @Path("/pull")
    public Response pull(@QueryParam("type") String type,
                         @QueryParam("since") @DefaultValue("0") long since,
                         @QueryParam("limit") @DefaultValue("100") int limit) {
        return changes.changes(type, since, limit)
                .map(page -> Response.ok(page).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).entity("Tipo desconocido").build());
    }
}
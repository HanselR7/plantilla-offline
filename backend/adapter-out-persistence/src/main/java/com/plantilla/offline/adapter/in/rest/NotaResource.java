package com.plantilla.offline.adapter.in.rest;

import com.plantilla.offline.application.nota.NotaUseCase;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/notas")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotaResource {

    @Inject
    NotaUseCase notas;

    @GET
    public List<NotaDto> listar() {
        return notas.listar().stream().map(NotaDto::from).toList();
    }

    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") UUID id) {
        return notas.obtener(id)
                .map(n -> Response.ok(NotaDto.from(n)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response crear(NotaDto dto) {
        try {
            NotaDto creada = NotaDto.from(notas.crear(dto.id(), dto.titulo(), dto.contenido()));
            return Response.status(Response.Status.CREATED).entity(creada).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response actualizar(@PathParam("id") UUID id, NotaDto dto) {
        try {
            return notas.actualizar(id, dto.titulo(), dto.contenido())
                    .map(n -> Response.ok(NotaDto.from(n)).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND).build());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        return notas.eliminar(id)
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
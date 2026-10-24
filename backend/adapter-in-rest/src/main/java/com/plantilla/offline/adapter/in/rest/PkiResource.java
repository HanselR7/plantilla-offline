package com.plantilla.offline.adapter.in.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@jakarta.ws.rs.Path("/pki")
@RequestScoped
public class PkiResource {

    /** Certificado público de la CA local, para instalarlo en los dispositivos. */
    @GET
    @jakarta.ws.rs.Path("/ca")
    @Public
    public Response ca() throws IOException {
        String dir = System.getProperty("jboss.server.config.dir");
        Path file = dir == null ? null : Path.of(dir, "plantilla-ca.crt");
        if (file == null || !Files.exists(file)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(Files.readAllBytes(file), "application/x-x509-ca-cert")
                .header("Content-Disposition", "attachment; filename=\"plantilla-ca.crt\"")
                .build();
    }
}
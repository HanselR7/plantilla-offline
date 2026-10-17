package com.plantilla.offline.adapter.in.rest;

import com.plantilla.offline.application.pairing.NewPairingCode;
import com.plantilla.offline.application.pairing.PairingPayload;
import com.plantilla.offline.application.pairing.PairingUseCase;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/pairing")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PairingResource {

    public record RedeemRequest(String code, String deviceName) {
    }

    @Inject
    PairingUseCase pairing;

    /** TODO (paso de seguridad): exigir sesión de administrador. Hoy está abierto. */
    @POST
    @Path("/codes")
    public Response createCode() {
        NewPairingCode c = pairing.createCode();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", c.code());
        body.put("expiresAt", c.expiresAt().toString());
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @POST
    @Path("/redeem")
    public Response redeem(RedeemRequest req) {
        if (req == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return pairing.redeem(req.code(), req.deviceName())
                .map(p -> Response.ok(toBody(p)).build())
                // Mismo mensaje para código inexistente, usado o vencido: no revela cuál.
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Código inválido o vencido").build());
    }

    private Map<String, Object> toBody(PairingPayload p) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deviceId", p.deviceId().toString());
        body.put("deviceToken", p.deviceToken());
        body.put("cloudUrl", p.cloudUrl());
        return body;
    }
}
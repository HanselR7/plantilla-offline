# Pendientes antes de producción

## Críticos

- [ ] Probar el instalador en una VM o Windows Sandbox (sin probar a la fecha)
- [ ] HTTPS con confianza para clientes (hoy certificado autofirmado)
- [ ] Desplegar la nube real (Cloud Run/VPS + PostgreSQL administrado)
- [ ] CORS: hoy abierto con "\*"; restringir
- [ ] Cambio de contraseña de usuarios y límite de intentos de login
- [ ] Sincronizar usuarios entre nube y local
- [ ] sync_token en texto plano en la base local
- [ ] Entre local y nube usar HTTPS

## Importantes

- [ ] Pantalla de administración de operaciones fallidas del outbox
- [ ] Limpieza de sesiones vencidas
- [ ] CSP para mitigar XSS (los tokens están en localStorage)
- [ ] AuthResource.me usa @Context ContainerRequestContext (mismo riesgo que causó el 500 en SyncResource)
- [ ] Resolución de conflictos más fina que "gana el último"
- [ ] Margen de relectura en el pull por orden de commit de secuencias

## Instalador

- [ ] Firma de código del .exe
- [ ] Revertir si install.ps1 falla a medias
- [ ] Pedir cloud_url y sync_token en el asistente
- [ ] Probar actualización sobre versión anterior
- [ ] IP del certificado puede cambiar con DHCP
- [ ] Revisar licencias de lo redistribuido
- [ ] Instalador: las claves del servicio están en PlantillaServer.xml con permisos restringidos; evaluar el credential store

## Calidad

- [ ] Pruebas automáticas (hoy no hay)
- [ ] Logging estructurado y manejo uniforme de errores
- [ ] Limpiar avisos de IntelliJ (dialecto SQL) y de antd

# Qué cambiar al crear un proyecto nuevo con la plantilla

## Pendiente de resolver (se completa en la fase del instalador)

- [ ] Base de datos: nombre, usuario y contraseña (hoy: plantilla_local / plantilla / postgres)
- [ ] Datasource de JBoss: installer/jboss/datasource.cli (hoy lleva contraseña de desarrollo)
- [ ] URL de la nube: installation.cloud_url y variable PLANTILLA_MODE=CLOUD en el despliegue
- [ ] Certificado HTTPS: plantilla.p12, su clave y la IP/dominio del servidor (installer/jboss/https.cli)
- [ ] CORS: hoy abierto con "\*"; restringir
- [ ] Nombre del paquete Java (com.plantilla.offline), artefacto y contexto (/plantilla-api)
- [ ] Nombre y logo de la PWA (vite.config.ts, public/pwa-\*.png)
- [ ] Contraseñas aleatorias por instalación (PostgreSQL, keystore, tokens)
- [ ] Autenticación de usuarios y de /pairing/codes (hoy abierto)
- [ ] Reemplazar JBoss EAP por WildFly si no habrá suscripción de Red Hat
- [ ] Cambiar la contraseña de un usuario (hoy no hay endpoint).
- [ ] Bloqueo o límite de intentos de login fallidos.
- [ ] Limpieza de sesiones vencidas en auth_session.
- [ ] Sincronizar usuarios entre nube y local, para que sean las mismas credenciales.
- [ ] Los tokens van en localStorage. En una app offline-first es lo habitual, pero un XSS los expondría, así que conviene un buen CSP más adelante.
- [x] /sync/push y /sync/pull abiertos (el 22B).
- [ ] installation.sync_token está en texto plano en la base local; protegerlo (credential store o cifrado)
- [ ] Entre el local y la nube debe usarse HTTPS en producción: el token viaja en un encabezado
- [ ] Registro de instalaciones manual (admin de la nube + SQL en el local); automatizarlo en el instalador
- [ ] Backoff de reintentos en OutboxRelay (hoy reintenta cada 10 s sin límite) y lista de operaciones fallidas visible
- [ ] Limpiar outbox.last_error al enviar con éxito
- [ ] AuthResource.me usa @Context ContainerRequestContext: mismo riesgo que causó el 500 en SyncResource
- [ ] Instalador: el certificado HTTPS se genera con las IPs del momento; si el router cambia la IP, deja de coincidir (reservar IP o usar nombre)
- [ ] Instalador: las claves del servicio están en PlantillaServer.xml con permisos restringidos; evaluar el credential store
- [ ] Instalador: cloud_url y sync_token se configuran a mano tras instalar; automatizar en el asistente
- [ ] Instalador: firmar el .exe y los scripts (certificado de firma de código)
- [ ] Revisar licencias de lo redistribuido (WildFly, Temurin, PostgreSQL, WinSW)

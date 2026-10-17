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

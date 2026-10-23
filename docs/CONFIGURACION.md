# Parámetros de configuración

## Variables de entorno (servidor)

| Variable                    | Qué es                      | Obligatoria                                   |
| --------------------------- | --------------------------- | --------------------------------------------- |
| PLANTILLA_DB_URL            | URL JDBC de la base         | No (por defecto localhost/plantilla_local)    |
| PLANTILLA_DB_USER           | Usuario de la base          | No (plantilla)                                |
| PLANTILLA_DB_PASSWORD       | Clave de la base            | Sí                                            |
| PLANTILLA_ADMIN_PASSWORD    | Clave del admin inicial     | No (se genera y se muestra una vez en el log) |
| PLANTILLA_KEYSTORE_PASSWORD | Clave del certificado HTTPS | Solo con HTTPS local                          |
| PLANTILLA_MODE              | LOCAL o CLOUD               | No (LOCAL)                                    |

## Propiedades Java (-D)

| Propiedad                        | Qué es                                            |
| -------------------------------- | ------------------------------------------------- |
| plantilla.mode                   | LOCAL o CLOUD (tiene prioridad sobre la variable) |
| jboss.bind.address               | 0.0.0.0 para aceptar la red local                 |
| jboss.socket.binding.port-offset | Desplaza los puertos (para correr dos servidores) |

## Tabla `installation`

- `cloud_url`: dirección de la API de la nube (en el servidor local)
- `sync_token`: credencial para sincronizar (en el servidor local)

## Registrar un servidor local en la nube

1. Obtener `server.installationId` de `/health` del local
2. Admin de la nube: `POST /sync/clients` con `{installationId, name}` -> devuelve `syncToken`
3. En la base local: `UPDATE installation SET sync_token='...', cloud_url='...'`

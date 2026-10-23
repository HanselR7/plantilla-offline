# Crear un proyecto nuevo con la plantilla

## 1. Copiar y renombrar

- [ ] Copiar la carpeta `plantilla-offline` con otro nombre y borrar `.git`; iniciar un repositorio nuevo
- [ ] Paquete Java: renombrar `com.plantilla.offline` (IntelliJ: Refactor > Rename en el paquete)
- [ ] groupId y artifactId en los `pom.xml` (padre y 5 módulos)
- [ ] Nombre del WAR y contexto: `<finalName>plantilla-api</finalName>` en `bootstrap/pom.xml`
- [ ] Si cambia el contexto, cambiar `/plantilla-api` en: `appConfig.ts`, `urlUtils.ts`, `discovery.ts`, `vite.config.ts` (denylist), Dockerfile y scripts del instalador
- [ ] Instalador: versión de la app y AppId definidos por proyecto

## 2. Identidad visual y PWA

- [ ] `vite.config.ts`: name, short_name, theme_color
- [ ] `public/pwa-192.png` y `pwa-512.png` con el logo
- [ ] Título en `index.html` y en `App.tsx`
- [ ] Colores en `styles/abstracts/_variables.scss` y `app/theme.ts`

## 3. Datos

- [ ] Nombres de base: `plantilla_local` y `plantilla_cloud` (migraciones, instalador, `datasource.cli`)
- [ ] Borrar el ejemplo `Nota` (ver sección 4) cuando ya no haga falta
- [ ] Nombre de la instalación por defecto en `V4__installation.sql`
- [ ] Certificado HTTPS: plantilla.p12, su clave y la IP/dominio del servidor (installer/jboss/https.cli)

## 4. Añadir un modelo nuevo (receta)

Backend, por cada modelo `X`:

1. Migración `V<n>__x.sql`: columnas de negocio + `sync_version`, `updated_at`, `deleted_at` + trigger `sync_touch()`
2. `domain/x/X.java` (Lombok `@Data`)
3. `application/x/`: `XRepository`, `XUseCase`, `XService`
4. `adapter-out-persistence/x/`: `XEntity extends BaseEntity`, `XJpaRepository` (con `outbox.record`), `XSyncHandler`
5. `adapter-in-rest`: `XDto`, `XResource`
6. `UseCaseConfig`: un `@Produces` para `XUseCase`

Frontend, por cada modelo:

1. `features/x/`: `types.ts`, `xApi.ts` (con `snapshotDb` y `applyPending`), `useX.ts`, página y formulario
2. Registrar la página en `App.tsx`

## 5. Probar

- [ ] Backend compila: `mvn clean package`
- [ ] Sincronización: crear dato en local, ver que llega a la nube, editarlo en la nube, ver que baja
- [ ] Offline: apagar servidor, crear y editar, reconectar

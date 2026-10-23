# ---------- Etapa 1: frontend ----------
FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/yarn.lock ./
RUN yarn install --frozen-lockfile
COPY frontend/ ./
RUN yarn build

# ---------- Etapa 2: backend ----------
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /build
COPY backend/ ./
# El frontend compilado se copia al WAR (WEB-INF se conserva).
COPY --from=frontend /app/dist/ bootstrap/src/main/webapp/
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 3: servidor ----------
FROM quay.io/wildfly/wildfly:41.0.1.Final-jdk21 AS runtime

# Driver de PostgreSQL como módulo de WildFly.
ARG PG_VERSION=42.7.4
USER root
RUN curl -fsSL -o /tmp/postgresql.jar \
      https://repo1.maven.org/maven2/org/postgresql/postgresql/${PG_VERSION}/postgresql-${PG_VERSION}.jar \
 && mkdir -p /opt/jboss/wildfly/modules/org/postgresql/main \
 && mv /tmp/postgresql.jar /opt/jboss/wildfly/modules/org/postgresql/main/postgresql.jar \
 && chown -R jboss:root /opt/jboss/wildfly/modules/org/postgresql
USER jboss

COPY --chown=jboss:root installer/jboss/postgresql-module.xml \
     /opt/jboss/wildfly/modules/org/postgresql/main/module.xml

# Datasource (lee PLANTILLA_DB_* al arrancar).
COPY --chown=jboss:root installer/jboss/datasource.cli /tmp/datasource.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/datasource.cli \
 && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history

COPY --from=backend --chown=jboss:root /build/bootstrap/target/plantilla-api.war \
     /opt/jboss/wildfly/standalone/deployments/plantilla-api.war

ENV PLANTILLA_MODE=CLOUD
EXPOSE 8080

# 0.0.0.0 para aceptar conexiones desde fuera del contenedor.
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
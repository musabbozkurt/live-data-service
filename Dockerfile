# Use a multi-stage build to optimize the image size
# Stage 1: Build the application
FROM maven:4.0.0-rc-5-ibm-semeru-25-noble AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml first and resolve dependencies for better layer caching
COPY pom.xml /app/live-data-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/live-data-service/pom.xml dependency:go-offline -B -q || true

# Copy source and build the application
COPY . /app/live-data-service
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/live-data-service/pom.xml clean package -DskipTests

# Create a custom Java runtime with only the modules required by the application.
# Modules: java.base (core), java.sql/java.transaction.xa (JDBC/JPA - PostgreSQL, H2),
# java.naming (JNDI - Spring datasource lookup), java.net.http (RestClient/HTTP),
# java.xml (Spring/Thymeleaf XML processing), java.desktop (AWT deps in some libraries),
# java.management/jdk.management (JMX - Actuator, Micrometer), java.instrument (Spring agent),
# java.security.jgss/java.security.sasl (Kerberos/SASL - Kafka, broker auth),
# jdk.crypto.ec (TLS/SSL with EC algorithms), java.compiler (javax.lang.model.SourceVersion),
# java.logging (JUL bridge for logback), jdk.unsupported (sun.misc.Unsafe - Netty, gRPC)
RUN "$JAVA_HOME"/bin/jlink \
         --add-modules jdk.unsupported,java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.compiler,java.net.http,java.xml,java.logging,java.transaction.xa,java.security.sasl,jdk.crypto.ec,jdk.management,jdk.net \
         --strip-java-debug-attributes \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --exclude-files="**/legal/**,**.diz" \
         --output /mbjavaruntime

# Strip debug symbols from native libraries to reduce size
RUN find /mbjavaruntime -name '*.so' -exec strip --strip-unneeded {} + 2>/dev/null || true

# Stage 2: Create the final image
FROM debian:bookworm-slim

ENV JAVA_HOME=/home/java/jdk25
ENV PATH=$JAVA_HOME/bin:$PATH

# Set the working directory for the final image
WORKDIR /app

COPY --from=build /mbjavaruntime $JAVA_HOME

# Copy the packaged jar file from the build stage
COPY --from=build /app/live-data-service/target/*.jar app.jar

EXPOSE 8080

# --enable-native-access=ALL-UNNAMED: Java 25's security model requires explicit
# permission for libraries accessing native code (Netty, gRPC, Redisson, etc.)
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]

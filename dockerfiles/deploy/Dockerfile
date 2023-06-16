FROM clojure:tools-deps-jammy AS build

WORKDIR /build

RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.desktop,java.naming,java.sql,jdk.unsupported \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /build/javaruntime

COPY deps.edn .
RUN clojure -P -X:run
RUN clojure -P -X:uberjar

COPY . .
RUN clojure -X:uberjar
RUN cp target/mongo-index-inspector-*-standalone.jar /build/mongo-index-inspector-standalone.jar

FROM debian:buster-slim

EXPOSE 3000

RUN mkdir /app
WORKDIR /app

RUN groupadd -g 999 appuser \
  && useradd -r -u 999 -g appuser appuser -s /bin/false
RUN chown -R appuser:appuser /app

COPY --from=build /build/dokku/CHECKS .
COPY --from=build /build/dokku/app.json .

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=build /build/javaruntime $JAVA_HOME

COPY --from=build /build/mongo-index-inspector-standalone.jar .

USER appuser

CMD ["java", "-jar", "mongo-index-inspector-standalone.jar"]

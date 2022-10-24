FROM adoptopenjdk/openjdk11

VOLUME /tmp
ENV TZ=Asia/Tehran

RUN  mkdir -p /var/log/hambaam-gateway
RUN  chmod -R 777 /var/log/hambaam-gateway

COPY target/*.jar hambaam-gateway-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-Xdebug","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9502","-jar","/hambaam-gateway-0.0.1-SNAPSHOT.jar"]
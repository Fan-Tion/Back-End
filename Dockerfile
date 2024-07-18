FROM bellsoft/liberica-openjdk-alpine:17
COPY build/libs/fantion-0.0.1-SNAPSHOT.jar fantion-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","fantion-0.0.1-SNAPSHOT.jar"]
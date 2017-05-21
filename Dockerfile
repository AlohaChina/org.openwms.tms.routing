FROM java:8-jre
VOLUME library
ADD target/openwms-tms-routing.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

FROM openjdk:8u131-jdk-alpine
LABEL maintainer "Emil Koutanov <ekoutanov@gmail.com>"

COPY build/libs/flywheel-standalone-full-*jar .
COPY docker/flywheel-boot.sh .
RUN chmod +x flywheel-boot.sh

VOLUME /ext
EXPOSE 8080
ENTRYPOINT ["/flywheel-boot.sh"]
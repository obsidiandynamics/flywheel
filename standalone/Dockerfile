FROM openjdk:8u131-jdk-alpine
LABEL maintainer "Emil Koutanov <ekoutanov@gmail.com>"

COPY build/libs/flywheel-*jar /build/libs/
COPY scripts/* /
RUN mkdir conf
COPY conf/ /conf/
RUN chmod +x flywheel-*.sh

VOLUME /ext
VOLUME /log
EXPOSE 8080
ENTRYPOINT ["/flywheel-launch.sh"]
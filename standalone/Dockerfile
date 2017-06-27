FROM openjdk:8u131-jdk-alpine
ADD docker/docker-flywheel.sh .
ADD build/libs/flywheel-standalone-full-*jar .
RUN chmod +x docker-flywheel.sh

EXPOSE 8080
ENTRYPOINT ["/docker-flywheel.sh"]
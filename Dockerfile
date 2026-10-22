FROM maven:3.9.9-amazoncorretto-21

ARG TEST_PROFILE=api
ARG API_BASE_URL=http://host.docker.internal:4111
ARG UI_BASE_URL=http://nginx
ARG UI_REMOTE=http://host.docker.internal:4444/wd/hub
ARG DB_URL=jdbc:postgresql://host.docker.internal:5433/nbank
ARG WIREMOCK_HOST=host.docker.internal
ARG DB_CHECKS_ENABLED=true

ENV TEST_PROFILE=${TEST_PROFILE} \
    API_BASE_URL=${API_BASE_URL} \
    UI_BASE_URL=${UI_BASE_URL} \
    UI_REMOTE=${UI_REMOTE} \
    DB_URL=${DB_URL} \
    WIREMOCK_HOST=${WIREMOCK_HOST} \
    DB_CHECKS_ENABLED=${DB_CHECKS_ENABLED} \
    RESULTS_ROOT=/app/results

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY . .
RUN chmod +x run-tests.sh

ENTRYPOINT ["./run-tests.sh"]

FROM openjdk:10

LABEL "com.github.actions.name"="run kotlin builds"
LABEL "com.github.actions.description"="run tests/code style and package project"
LABEL "com.github.actions.icon"="eye"
LABEL "com.github.actions.color"="gray-dark"

LABEL version="1.0.0"

RUN apt-get update -y && apt-get install curl jq wget -y

COPY "run.sh" "/run.sh"
RUN chmod +x /run.sh
ENTRYPOINT ["/run.sh"]
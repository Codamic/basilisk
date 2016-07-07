FROM clojure:alpine
MAINTAINER lxsameer@gnu.org

RUN addgroup -g 1000 lxsameer && \
    adduser -u 1000 -D -G lxsameer lxsameer && \
    echo "lxsameer  ALL=(ALL) ALL" >> /etc/sudoers && \
    mkdir -p /home/lxsameer/app && \
    mkdir -p /home/lxsameer/.m2 && \
    mkdir -p /home/lxsameer/.lein

COPY ./.lein/profiles.clj /home/lxsameer/.lein/profiles.clj

RUN chown lxsameer.lxsameer -R /home/lxsameer/app && \
    chown lxsameer.lxsameer -R /home/lxsameer/.m2 && \
    chown lxsameer.lxsameer -R /home/lxsameer/.lein && \
    chmod 755 -R /home/lxsameer/.lein && \
    chmod 755 -R /home/lxsameer/.m2

USER lxsameer
ENV LEIN_REPL_PORT=9182 LEIN_REPL_HOST='0.0.0.0'
EXPOSE 9182-9192

WORKDIR /home/lxsameer/app
RUN ls /home/lxsameer/app

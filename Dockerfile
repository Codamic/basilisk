FROM clojure:alpine
MAINTAINER lxsameer@gnu.org
RUN addgroup -g 1000 lxsameer
RUN adduser -u 1000 -D -G lxsameer lxsameer
RUN echo "lxsameer  ALL=(ALL) ALL" >> /etc/sudoers
RUN mkdir -p /home/lxsameer/app
RUN mkdir -p /home/lxsameer/.m2
RUN chown lxsameer.lxsameer -R /home/lxsameer/app
RUN chown lxsameer.lxsameer -R /home/lxsameer/.m2
RUN chmod 755 -R /home/lxsameer/.m2

USER lxsameer

WORKDIR /home/lxsameer/app
RUN ls /home/lxsameer/app

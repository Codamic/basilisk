FROM clojure:alpine
MAINTAINER lxsameer@gnu.org
RUN addgroup -g 1000 lxsameer
RUN adduser -u 1000 -D -G lxsameer lxsameer
RUN echo "lxsameer  ALL=(ALL) ALL" >> /etc/sudoers
RUN mkdir -p /home/lxsameer/app
RUN chown lxsameer.lxsameer -R /home/lxsameer/app
USER lxsameer

WORKDIR /home/lxsameer/app
#COPY project.clj /home/lxsameer/app/
#RUN lein deps
#VOLUME ["./:/home/lxsameer/app"]
#COPY ./ /home/lsameer/app
RUN ls /home/lxsameer/app
#RUN lein run
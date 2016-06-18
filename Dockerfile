FROM clojure:alpine
MAINTAINER lxsameer@gnu.org

RUN adduser -S lxsameer
USER lxsameer
RUN mkdir -p /home/lxsameer/app
#COPY project.clj /home/lxsameer/app/
#RUN lein deps
WORKDIR /home/lxsameer/app
VOLUME ["./:/home/lxsameer/app"]
CMD ["echo", "Run a lein command as you want"]

FROM egovio/alpine-node-builder-14:yarn AS build
#FROM ghcr.io/egovernments/alpine-node-builder-14:yarn AS build
RUN apk update && apk upgrade
RUN apk add --no-cache git>2.30.0
ARG WORK_DIR
WORKDIR /app
ENV NODE_OPTIONS "--max-old-space-size=4792"

COPY ${WORK_DIR} .
RUN ls -lah

#RUN node web/envs.js
RUN cd web/ \
    && node -e 'console.log(v8.getHeapStatistics().heap_size_limit/(1024*1024))' \
    && node -e 'console.log("workbench only")' \
    && cd workbench/ \
    &&  ./install-deps.sh \
    && cd ../ \
    && yarn install \ 
    && yarn build:webpack 

FROM nginx:mainline-alpine
#FROM ghcr.io/egovernments/nginx:mainline-alpine
ENV WORK_DIR=/var/web/workbench-ui

RUN mkdir -p ${WORK_DIR}

COPY --from=build /app/web/build ${WORK_DIR}/
COPY --from=build /app/web/workbench/nginx.conf /etc/nginx/conf.d/default.conf

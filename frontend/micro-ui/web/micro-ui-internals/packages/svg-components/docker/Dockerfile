FROM egovio/alpine-node-builder-14:yarn AS build
RUN apk update && apk upgrade
RUN apk add --no-cache git>2.30.0
ARG WORK_DIR
WORKDIR /app
ENV NODE_OPTIONS "--max-old-space-size=1792"

COPY ${WORK_DIR} .

#RUN node web/envs.js
RUN  yarn install \
    && yarn build-storybook

FROM nginx:mainline-alpine
ENV WORK_DIR=/var/storybook-svg

RUN mkdir -p ${WORK_DIR}

COPY --from=build /app/dist-storybook ${WORK_DIR}/
COPY --from=build /app/docker/nginx.conf /etc/nginx/conf.d/default.conf
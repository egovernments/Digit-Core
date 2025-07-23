# syntax=docker/dockerfile:1.4
ARG WORK_DIR

################################
# 1) Build stage
################################
FROM node:10 AS builder
WORKDIR /app

# cache npm downloads in /tmp
ENV npm_config_cache=/tmp

# copy your source
COPY ${WORK_DIR} .

# install dependencies
RUN npm install

################################
# 2) Runtime stage
################################
FROM node:10
WORKDIR /app

# preserve npm cache (if you ever run installs at runtime)
ENV npm_config_cache=/tmp

# grab built app + deps
COPY --from=builder /app /app

# runtime settings
ENV PORT=8080
EXPOSE 8080

CMD ["npm", "start"]
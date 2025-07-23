# syntax=docker/dockerfile:1.4

ARG WORK_DIR
ENV npm_config_cache=/tmp

################################
# 1) Build stage
################################
FROM node:10-alpine AS builder
WORKDIR /app

# copy your app’s source
COPY ${WORK_DIR} .

# install deps (uses npm as before)
RUN npm install

################################
# 2) Runtime stage
################################
FROM node:10-alpine
WORKDIR /app

# pull in compiled app + deps
COPY --from=builder /app /app

# runtime env
ENV PORT=8080
EXPOSE 8080

# same start command
CMD ["npm", "start"]
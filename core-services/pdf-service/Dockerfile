# syntax=docker/dockerfile:1.4

################################
# 1) Build stage
################################
FROM node:10 AS builder
WORKDIR /app

# cache npm downloads in /tmp
ENV npm_config_cache=/tmp

# copy your source
COPY . .

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
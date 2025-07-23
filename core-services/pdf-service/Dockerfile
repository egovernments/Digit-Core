# syntax=docker/dockerfile:1.4
ARG WORK_DIR=.

################################
# 1) Build stage
################################
FROM node:10 AS builder
WORKDIR /app

# Debug: List the contents of the build context
RUN ls -la /

# cache npm downloads in /tmp
ENV npm_config_cache=/tmp

# Debug: Show the value of WORK_DIR
ARG WORK_DIR
RUN echo "WORK_DIR is set to: ${WORK_DIR}"

# Debug: Check if the directory exists
RUN ls -la /${WORK_DIR} || echo "Directory not found"

# Try different copy approaches
COPY ${WORK_DIR}/package.json ./
COPY ${WORK_DIR}/package-lock.json ./
COPY ${WORK_DIR}/src ./src
COPY ${WORK_DIR}/.babelrc ./

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

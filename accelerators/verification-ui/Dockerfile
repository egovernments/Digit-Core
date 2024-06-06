# Use an official Node.js runtime as the base image
FROM node:18 as build

# Set the working directory in the container
WORKDIR /app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

# Install Node.js dependencies
RUN npm install

# Copy the rest of the application code to the working directory
COPY . .

# Check the contents of the '/app/src' directory
RUN ls /app/src

# Build the React app
RUN npm run build

# Add the ARGs as environment variables
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

# Copy labels from build stage
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

FROM nginx:mainline-alpine

ENV WORK_DIR=/var/web/qr-scanner-ui

RUN mkdir -p ${WORK_DIR}


COPY --from=build /app/dist ${WORK_DIR}/
COPY --from=build /app/docker/nginx.conf /etc/nginx/conf.d/default.conf


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

# Use an official Nginx image as the final production image
FROM nginx:mainline-alpine


ENV WORK_DIR=/var/web/qr-scanner-ui

RUN mkdir -p ${WORK_DIR}

# Copy the built React app from the build container to the Nginx container
COPY --from=build /app/build ${WORK_DIR}/
COPY ./nginx.conf /etc/nginx/conf.d/default.conf



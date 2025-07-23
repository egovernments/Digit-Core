# syntax=docker/dockerfile:1.4
FROM node:10 AS builder

ARG WORK_DIR
WORKDIR /app
COPY ${WORK_DIR:-core-services/pdf-service}/ .
RUN npm install

FROM node:10
WORKDIR /app
COPY --from=builder /app /app

ENV PORT=8080
EXPOSE 8080

CMD ["npm", "start"]

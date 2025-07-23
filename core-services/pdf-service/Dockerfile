# syntax=docker/dockerfile:1.4
FROM node:10 AS builder

WORKDIR /app
COPY . .
RUN npm install

FROM node:10
WORKDIR /app
COPY --from=builder /app /app

ENV PORT=8080
EXPOSE 8080

CMD ["npm", "start"]

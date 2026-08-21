FROM golang:1.25-alpine AS build

ARG WORK_DIR
WORKDIR /app

COPY ${WORK_DIR}/go.mod ${WORK_DIR}/go.sum ./
RUN go mod download

COPY ${WORK_DIR}/ ./

ARG TARGETOS
ARG TARGETARCH
RUN CGO_ENABLED=0 GOOS=${TARGETOS} GOARCH=${TARGETARCH} go build -trimpath -ldflags="-s -w"

FROM scratch

WORKDIR /opt/egov
COPY --from=build --chmod=0755 /app/zuul-kubernetes-discovery /opt/egov/
USER 10001:10001

CMD ["/opt/egov/zuul-kubernetes-discovery"]

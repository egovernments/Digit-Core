
FROM --platform=$BUILDPLATFORM golang:1.25.13-alpine AS build

ENV GO111MODULE=on
ENV GOCACHE=/tmp

ARG WORK_DIR
ARG TARGETOS
ARG TARGETARCH
WORKDIR /app

COPY ${WORK_DIR}/go.mod .
COPY ${WORK_DIR}/go.sum .

RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=${TARGETOS} GOARCH=${TARGETARCH} go build


# Create runtime image
FROM gcr.io/distroless/static-debian12:nonroot

WORKDIR /opt/egov

COPY --chown=nonroot:nonroot --from=build /app/zuul-kubernetes-discovery /opt/egov/

USER nonroot:nonroot

CMD ["/opt/egov/zuul-kubernetes-discovery"]

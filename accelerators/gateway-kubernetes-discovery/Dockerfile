# docker buildx build \
# -f utilities/zuul-kubernetes-discovery/Dockerfile \
# --platform linux/amd64,linux/arm64 \
# --build-arg WORK_DIR=utilities/zuul-kubernetes-discovery \
# -t egovio/zuul-kubernetes-discovery:multiarch \
# --push \
# .

# 1) BUILD STAGE
FROM golang:1.13-alpine AS builder


# expose BuildKit’s target vars
ARG TARGETOS
ARG TARGETARCH

# point this at your zuul module folder
ARG WORK_DIR=utilities/zuul-kubernetes-discovery

# enable modules and disable CGO
ENV GO111MODULE=on \
    CGO_ENABLED=0 \
    GOOS=${TARGETOS} \
    GOARCH=${TARGETARCH}

WORKDIR /app

# only bring in the module files first (for caching)
COPY ${WORK_DIR}/go.mod ${WORK_DIR}/go.sum ./
RUN go mod download

# then copy the actual source
COPY ${WORK_DIR}/ ./

# build into a single binary
RUN go build -o zuul-kubernetes-discovery .



# 2) RUNTIME STAGE
FROM alpine:3

WORKDIR /opt/egov
RUN addgroup -S egov && adduser -S -G egov egov

# pull in the correctly-arch compiled binary
COPY --from=builder /app/zuul-kubernetes-discovery .

RUN chmod +x ./zuul-kubernetes-discovery
USER egov

CMD ["./zuul-kubernetes-discovery"]

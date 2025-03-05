FROM egovio/flyway:10.7.1

RUN rm -rf /usr/lib/libssl.so.3

COPY ./migration/main /flyway/sql

COPY migrate.sh /usr/bin/migrate.sh

RUN chmod +x /usr/bin/migrate.sh

ENTRYPOINT ["/usr/bin/migrate.sh"]

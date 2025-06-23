FROM egovio/flyway:10.7.1

COPY ./ddl /flyway/sql

# COPY ./seed /flyway/seed

COPY migrate.sh /usr/bin/migrate.sh

RUN chmod +x /usr/bin/migrate.sh

ENTRYPOINT ["/usr/bin/migrate.sh"]

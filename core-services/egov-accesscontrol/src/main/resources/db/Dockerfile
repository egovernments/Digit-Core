FROM egovio/flyway:10.7.1

COPY ./migration/ddl /flyway/sql

COPY ./migration/dev /flyway/dev

COPY migrate.sh /usr/bin/migrate.sh

RUN chmod +x /usr/bin/migrate.sh

ENTRYPOINT ["/usr/bin/migrate.sh"]

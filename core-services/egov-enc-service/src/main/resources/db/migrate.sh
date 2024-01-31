#!/bin/sh
echo "Running Flyway with the following parameters:"
echo "URL: $DB_URL"
echo "Table: $SCHEMA_TABLE"
echo "User: $FLYWAY_USER"
echo "Password: $FLYWAY_PASSWORD"
echo "Locations: $FLYWAY_LOCATIONS"
flyway migrate
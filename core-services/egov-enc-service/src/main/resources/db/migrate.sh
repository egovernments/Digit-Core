#!/bin/sh
echo "Running Flyway with the following parameters:"
echo "URL: $DB_URL"
echo "Table: $SCHEMA_TABLE"
echo "User: $FLYWAY_USER"
echo "Password: $FLYWAY_PASSWORD"
echo "Locations: $FLYWAY_LOCATIONS"
flyway -url=$DB_URL -table=$SCHEMA_TABLE -user=$FLYWAY_USER -password=$FLYWAY_PASSWORD -locations=$FLYWAY_LOCATIONS -baselineOnMigrate=true -outOfOrder=true migrate
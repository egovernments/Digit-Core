#!/bin/bash


baseurl=$DB_URL
echo "the baseurl : $DB_URL"
schemasetter="?currentSchema="
schemas=$SCHEMA_NAME
echo "the schemas : $schemas"

# Non-central deployments do not set SCHEMA_NAME: migrate the default schema once, as before
if [ -z "$schemas" ]; then
    flyway -url=$DB_URL -table=$SCHEMA_TABLE -user=$FLYWAY_USER -password=$FLYWAY_PASSWORD -locations=$FLYWAY_LOCATIONS -baselineOnMigrate=true -outOfOrder=true migrate
    exit $?
fi

for schemaname in ${schemas//,/ }
do
    echo "the schema name : ${baseurl}${schemasetter}${schemaname}"
    flyway -url=${baseurl}${schemasetter}${schemaname}  -table=$SCHEMA_TABLE -user=$FLYWAY_USER -password=$FLYWAY_PASSWORD -locations=$FLYWAY_LOCATIONS -baselineOnMigrate=true -outOfOrder=true migrate
done

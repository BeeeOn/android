#!/bin/sh
DBname="home6"
mac=101
type=10

echo "delete from devices where fk_facilities_mac='"$mac"' and type="$type | psql $DBname

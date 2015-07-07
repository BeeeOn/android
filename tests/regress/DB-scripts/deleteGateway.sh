#!/bin/sh
DBname="home6" 
gatewayID=101010

echo "delete from adapters where adapter_id="$gatewayID | psql $DBname

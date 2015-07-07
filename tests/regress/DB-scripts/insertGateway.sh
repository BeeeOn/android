#!/bin/sh
DBname="home6" 
gatewayID=101010
gatewayName="at 10"

echo "INSERT INTO ADAPTERS (ADAPTER_ID,NAME) VALUES ($gateWayID, '$gtewayName')" | psql $DBname

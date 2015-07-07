#!/bin/sh
DBname="home6" 

echo "INSERT INTO DEVICES (fk_facilities_mac, name, type, value ) VALUES ('$1','$2','$3',$4)" | psql $DBname

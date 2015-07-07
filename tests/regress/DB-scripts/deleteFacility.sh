#!/bin/sh
DBname="home6" 
mac=101

echo "delete from facilities where mac='$mac'" | psql $DBname

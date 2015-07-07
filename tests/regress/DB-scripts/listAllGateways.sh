#!/bin/sh
DBname="home6" 

echo "select * from adapters"| psql $DBname

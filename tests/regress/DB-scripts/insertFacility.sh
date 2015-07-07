#!/bin/sh
DBname="home6"

roomId = $3

if test -z "$roomId"
then
   roomId='null'
fi


echo "INSERT INTO facilities (mac, init, refresh, battery, quality,timestamp, involved, fk_adapter_id,fk_room_id) VALUES ('$1', 0, 100, 100, 100, 946767600, 946767500, $2, $roomId )" | psql $DBname

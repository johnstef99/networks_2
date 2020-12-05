#!/bin/sh

[ -z $1 ] && echo -e "run sh getCodes.sh URL_FROM_ITHAKI_SESSION" && exit

curl -v $1 2>&1 | grep ":" | grep "=red" | cut -d' ' -f6 > codes

#/bin/bash

SEED_FILE_NAME='err_seeds'

if [ ! -f ${SEED_FILE_NAME} ] ; then
	touch ${SEED_FILE_NAME}
fi

grep "\*\*" log.out | while read -r line; do
	possible_seed=$(echo $line | rev | cut -d' ' -f1 | rev)
	if [[ ${possible_seed} == ?(-)+([0-9]*) ]] ; then
		echo ${possible_seed} >> ${SEED_FILE_NAME}
	fi
done


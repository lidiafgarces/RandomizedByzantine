#!/bin/bash  
echo "How many processes should be started?: "
read n
declare -i f=$[(n-1)/5]
declare -i registry=1099

declare -i count=0

while [ $count -lt $n ]
do
	declare -i newRegistry=$[registry-count]
	declare addresses="java DA_Process_main localhost "
    if [ "$count" -lt "$f" ]; then
		echo "Registry: "$newRegistry", faulty process"
	else
		echo "Registry: "$newRegistry
	fi
    count=$[count + 1]
    addresses=$addresses$newRegistry" "$count
    echo "The address will be:"
    echo $addresses

	i=1
	while [ $i -le $n ]
	do
		if [ "$i" -ne "$count" ]; then
			addresses=$addresses" localhost"
		fi
		i=$[i + 1]
	done
	echo "The final address will be:"
    echo $addresses
    $addresses &
done

echo "You have started "$n" processes, from which "$f" are faulty."
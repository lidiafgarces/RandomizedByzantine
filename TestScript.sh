#!/bin/bash
echo "How many processes should be started?: "
read n

echo "How many processes should have the value 0?: "
read numberOf0

echo "How many processes should have the value 1?: "
read numberOf1

declare -i f=$[(n-1)/5]
numberOf0=$[numberOf0+f]
numberOf1=$[numberOf1+numberOf0]
echo "Processes 1 to "$f" will be faulty, "$[f+1]" to "$numberOf0" will start with 0, and "$[numberOf0+1]" to "$numberOf1" will start with 1"
echo ""
declare -i registry=1099

declare -i count=0
bg_pid=""
faulty=false

while [ $count -lt $n ]
do
	declare -i newRegistry=$[registry-count]
	declare addresses="java DA_Process_main localhost "
	declare initialValue
	echo ""
    if [ "$count" -lt "$f" ]; then
		echo "Registry: "$newRegistry", faulty process"
		faulty=true
		initialValue=-1
	else
		faulty=false
		if [ "$count" -lt "$numberOf0" ]; then
			echo "Registry: "$newRegistry", 0 process"
			initialValue=0
		elif [ "$count" -lt "$numberOf1" ]; then
			echo "Registry: "$newRegistry", 1 process"
			initialValue=1
		else
			echo "Registry: "$newRegistry", random process"
			initialValue=-1
		fi
	fi
    count=$[count + 1]
    addresses=$addresses$newRegistry" "$count" "$faulty" "$initialValue
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
    sleep 0.5
    $addresses &
    bg_pid=$bg_pid$!" "
    echo $bg_pid
done

echo "You have started "$n" processes, from which "$f" are faulty."

trap "kill $bg_pid" 2 15
wait $bg_pid

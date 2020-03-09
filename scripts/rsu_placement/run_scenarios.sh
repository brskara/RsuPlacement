#!/bin/bash

script_root_path="$(dirname "$(sh readLink.sh "$0")")"
root_out_folder=${script_root_path}/output
num_of_processes=$1
min_no_of_vehicles=500
max_no_of_vehicles=4000
inc=500
process_counter=0

date=$(date '+%d-%m-%Y_%H-%M')
simulation_out_folder=${root_out_folder}/${date}
mkdir -p $simulation_out_folder

simulations=$(cat ${script_root_path}/simulation.list)

rm -rf ${script_root_path}/tmp_runner*
		
for sim_args in $simulations
do
	scenario_name=$(echo $sim_args | cut -d ';' -f1)
	edge_devices_file=$(echo $sim_args | cut -d ';' -f2)
	applications_file=$(echo $sim_args | cut -d ';' -f3)
	for (( i=$min_no_of_vehicles; i<=max_no_of_vehicles; i+=$inc ))
	do
		process_id=$(($process_counter % $num_of_processes))
		process_counter=$(($process_counter + 1))
		
		echo "${script_root_path}/runner.sh $simulation_out_folder $scenario_name $edge_devices_file $applications_file ${i}" >> "${simulation_out_folder}/tmp_runner${process_id}.sh"
	done
done

for (( i=0; i<$num_of_processes; i++ ))
do
	chmod +x ${simulation_out_folder}/tmp_runner${i}.sh
	${simulation_out_folder}/tmp_runner${i}.sh &
done

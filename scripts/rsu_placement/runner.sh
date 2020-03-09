#!/bin/sh

script_root_path="$(dirname "$(sh readLink.sh "$0")")"
simulation_out_folder=$1
scenario_name=$2
edge_devices_file=$3
applications_file=$4
number_of_vehicles=$5

scenario_out_folder=${simulation_out_folder}/${edge_devices_file}/${number_of_vehicles}
scenario_conf_file=${script_root_path}/config/${scenario_name}.properties
scenario_edge_devices_file=${script_root_path}/config/${edge_devices_file}
scenario_applications_file=${script_root_path}/config/${applications_file}
scenario_input_file=${script_root_path}/input/traffic${number_of_vehicles}.xml

mkdir -p $scenario_out_folder
java -Xms7168m -Xmx12288m -classpath '../../bin:../../lib/cloudsim-4.0.jar:../../lib/commons-math3-3.6.1.jar:../../lib/colt.jar:../../lib/kd.jar' edu.boun.edgecloudsim.applications.rsu_placement.MainApp $scenario_conf_file $scenario_edge_devices_file $scenario_applications_file $scenario_out_folder $scenario_input_file $number_of_vehicles > ${scenario_out_folder}.log
#tar -czf ${scenario_out_folder}.tar.gz -C $simulation_out_folder/${scenario_name} ite${number_of_vehicles}
#rm -rf $scenario_out_folder

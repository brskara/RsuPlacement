#!/bin/sh
rm -rf ../../bin
mkdir ../../bin
javac -classpath "../../lib/cloudsim-4.0.jar:../../lib/commons-math3-3.6.1.jar:../../lib/colt.jar:../../lib/kd.jar" -sourcepath ../../src ../../src/edu/boun/edgecloudsim/applications/rsu_placement/MainApp.java -d ../../bin

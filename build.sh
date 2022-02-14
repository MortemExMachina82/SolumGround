set -e
cd src
javac --release 8 org/solumground/Main.java -d ../classes
cd ../classes
jar cfm ../SolumGround.jar ../assets/MANIFEST.MF ./*
cd ../
chmod +x SolumGround.jar
java -jar SolumGround.jar

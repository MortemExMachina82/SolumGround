set -e
DIR="$( cd "$( dirname "$0" )" && pwd )"
cd src
javac -cp ".:../lib/json-simple/*:../lib/lwjgl/*:../lib/lwjgl-glfw/*:../lib/lwjgl-opengl/*" --release 8 org/solumground/Main.java -d out/production/SolumGround
#cd ../
#jar cfm SolumGround.jar ./out/production/SolumGround/META-INF/MANIFEST.MF ./out/production/SolumGround/*
#cd ../
#chmod +x SolumGround.jar
#java -jar SolumGround.jar


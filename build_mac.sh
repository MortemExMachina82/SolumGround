set -e
DIR="$( cd "$( dirname "$0" )" && pwd )"
if [ -f "$DIR/out/.libsloaded" ]; then
    echo "Libs Loaded"
else
    echo "One Time Lib Loading" 
    mkdir -p $DIR/out/production/SolumGround/META-INF
    mkdir -p $DIR/out/production/SolumGround/org
    cp $DIR/MANIFEST.MF $DIR/out/production/SolumGround/META-INF

    mkdir -p $DIR/tmp
    cd $DIR/tmp
    jar -xf $DIR/lib/lwjgl/lwjgl.jar
    jar -xf $DIR/lib/lwjgl-glfw/lwjgl-glfw.jar
    jar -xf $DIR/lib/lwjgl-opengl/lwjgl-opengl.jar
    jar -xf $DIR/lib/lwjgl/lwjgl-natives-linux.jar
    jar -xf $DIR/lib/lwjgl/lwjgl-natives-linux-arm32.jar
    jar -xf $DIR/lib/lwjgl/lwjgl-natives-linux-arm64.jar
    jar -xf $DIR/lib/lwjgl/lwjgl-natives-macos.jar
    jar -xf $DIR/lib/lwjgl/lwjgl-natives-macos-arm64.jar
    jar -xf $DIR/lib/lwjgl/lwjgl-natives-windows.jar
    jar -xf $DIR/lib/lwjgl/lwjgl-natives-windows-arm64.jar
    jar -xf $DIR/lib/lwjgl/lwjgl-natives-windows-x86.jar
    
    jar -xf $DIR/lib/lwjgl-glfw/lwjgl-glfw-natives-linux.jar
    jar -xf $DIR/lib/lwjgl-glfw/lwjgl-glfw-natives-linux-arm32.jar
    jar -xf $DIR/lib/lwjgl-glfw/lwjgl-glfw-natives-linux-arm64.jar
    jar -xf $DIR/lib/lwjgl-glfw/lwjgl-glfw-natives-macos.jar
    jar -xf $DIR/lib/lwjgl-glfw/lwjgl-glfw-natives-macos-arm64.jar
    jar -xf $DIR/lib/lwjgl-glfw/lwjgl-glfw-natives-windows.jar
    jar -xf $DIR/lib/lwjgl-glfw/lwjgl-glfw-natives-windows-arm64.jar
    jar -xf $DIR/lib/lwjgl-glfw/lwjgl-glfw-natives-windows-x86.jar
    
    jar -xf $DIR/lib/lwjgl-opengl/lwjgl-opengl-natives-linux.jar
    jar -xf $DIR/lib/lwjgl-opengl/lwjgl-opengl-natives-linux-arm32.jar
    jar -xf $DIR/lib/lwjgl-opengl/lwjgl-opengl-natives-linux-arm64.jar
    jar -xf $DIR/lib/lwjgl-opengl/lwjgl-opengl-natives-macos.jar
    jar -xf $DIR/lib/lwjgl-opengl/lwjgl-opengl-natives-macos-arm64.jar
    jar -xf $DIR/lib/lwjgl-opengl/lwjgl-opengl-natives-windows.jar
    jar -xf $DIR/lib/lwjgl-opengl/lwjgl-opengl-natives-windows-arm64.jar
    jar -xf $DIR/lib/lwjgl-opengl/lwjgl-opengl-natives-windows-x86.jar
    
    
    cp -R $DIR/tmp/org $DIR/out/production/SolumGround
    cp -R $DIR/tmp/linux $DIR/out/production/SolumGround
    cp -R $DIR/tmp/macos $DIR/out/production/SolumGround
    cp -R $DIR/tmp/windows $DIR/out/production/SolumGround

    cd $DIR
    rm -R $DIR/tmp
    touch $DIR/out/.libsloaded
fi

echo "Compiling"
cd $DIR/src
javac -cp ".:../lib/json-simple/*:../lib/lwjgl/*:../lib/lwjgl-glfw/*:../lib/lwjgl-opengl/*" --release 8 org/solumground/Main.java -d ../out/production/SolumGround
echo "Packing Into Jar"
cd $DIR/out/production/SolumGround
jar cfm $DIR/SolumGround.jar ./META-INF/MANIFEST.MF ./*
cd $DIR
chmod +x SolumGround.jar
echo "Starting"
java -jar -XstartOnFirstThread SolumGround.jar


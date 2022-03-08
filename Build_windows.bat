@ECHO OFF
set DIR=%~pd0

if exist "%DIR%out\.libsloaded" (
    echo "libs Loaded" || goto :error
) ELSE (
    echo "One Time Lib Loading"  || goto :error
    if not exist %DIR%out\production\SolumGround\META-INF mkdir %DIR%out\production\SolumGround\META-INF || goto :error
    if not exist %DIR%out\production\SolumGround\org mkdir %DIR%out\production\SolumGround\org || goto :error
    copy %DIR%MANIFEST.MF %DIR%out\production\SolumGround\META-INF || goto :error

    if not exist %DIR%tmp mkdir %DIR%tmp || goto :error
    cd %DIR%tmp || goto :error
    jar -xf %DIR%lib\lwjgl\lwjgl.jar || goto :error
    jar -xf %DIR%lib\lwjgl-glfw\lwjgl-glfw.jar || goto :error
    jar -xf %DIR%lib\lwjgl-opengl\lwjgl-opengl.jar || goto :error
    jar -xf %DIR%lib\lwjgl\lwjgl-natives-linux.jar || goto :error
    jar -xf %DIR%lib\lwjgl\lwjgl-natives-linux-arm32.jar || goto :error
    jar -xf %DIR%lib\lwjgl\lwjgl-natives-linux-arm64.jar || goto :error
    jar -xf %DIR%lib\lwjgl\lwjgl-natives-macos.jar || goto :error
    jar -xf %DIR%lib\lwjgl\lwjgl-natives-macos-arm64.jar || goto :error
    jar -xf %DIR%lib\lwjgl\lwjgl-natives-windows.jar || goto :error
    jar -xf %DIR%lib\lwjgl\lwjgl-natives-windows-arm64.jar || goto :error
    jar -xf %DIR%lib\lwjgl\lwjgl-natives-windows-x86.jar || goto :error
    
    jar -xf %DIR%lib\lwjgl-glfw\lwjgl-glfw-natives-linux.jar || goto :error
    jar -xf %DIR%lib\lwjgl-glfw\lwjgl-glfw-natives-linux-arm32.jar || goto :error
    jar -xf %DIR%lib\lwjgl-glfw\lwjgl-glfw-natives-linux-arm64.jar || goto :error
    jar -xf %DIR%lib\lwjgl-glfw\lwjgl-glfw-natives-macos.jar || goto :error
    jar -xf %DIR%lib\lwjgl-glfw\lwjgl-glfw-natives-macos-arm64.jar || goto :error
    jar -xf %DIR%lib\lwjgl-glfw\lwjgl-glfw-natives-windows.jar || goto :error
    jar -xf %DIR%lib\lwjgl-glfw\lwjgl-glfw-natives-windows-arm64.jar || goto :error
    jar -xf %DIR%lib\lwjgl-glfw\lwjgl-glfw-natives-windows-x86.jar || goto :error
    
    jar -xf %DIR%lib\lwjgl-opengl\lwjgl-opengl-natives-linux.jar || goto :error
    jar -xf %DIR%lib\lwjgl-opengl\lwjgl-opengl-natives-linux-arm32.jar || goto :error
    jar -xf %DIR%lib\lwjgl-opengl\lwjgl-opengl-natives-linux-arm64.jar || goto :error
    jar -xf %DIR%lib\lwjgl-opengl\lwjgl-opengl-natives-macos.jar || goto :error
    jar -xf %DIR%lib\lwjgl-opengl\lwjgl-opengl-natives-macos-arm64.jar || goto :error
    jar -xf %DIR%lib\lwjgl-opengl\lwjgl-opengl-natives-windows.jar || goto :error
    jar -xf %DIR%lib\lwjgl-opengl\lwjgl-opengl-natives-windows-arm64.jar || goto :error
    jar -xf %DIR%lib\lwjgl-opengl\lwjgl-opengl-natives-windows-x86.jar || goto :error
    
    
    Xcopy /E /I %DIR%tmp\org %DIR%out\production\SolumGround\org || goto :error
    Xcopy /E /I %DIR%tmp\linux %DIR%out\production\SolumGround\linux || goto :error
    Xcopy /E /I %DIR%tmp\macos %DIR%out\production\SolumGround\macos || goto :error
    Xcopy /E /I %DIR%tmp\windows %DIR%out\production\SolumGround\windows || goto :error

    cd %DIR% || goto :error
    rmdir /s /q %DIR%tmp || goto :error
    copy /y NUL %DIR%out\.libsloaded >NUL || goto :error
)

echo "Compiling" || goto :error
cd %DIR%src || goto :error
javac -cp ".;../lib/json-simple/*;../lib/lwjgl/*;../lib/lwjgl-glfw/*;../lib/lwjgl-opengl/*" --release 8 %DIR%src\org\solumground\Main.java -d %DIR%out\production\SolumGround || goto :error
echo "Packing Into Jar" || goto :error
cd %DIR%out\production\SolumGround || goto :error
jar cfm %DIR%SolumGround.jar ./META-INF/MANIFEST.MF ./* || goto :error
cd %DIR% || goto :error
echo "Starting" || goto :error
java -jar %DIR%SolumGround.jar || goto :error
goto :EOF

:error
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
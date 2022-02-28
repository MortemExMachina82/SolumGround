package org.solumground;

import org.solumground.Json.*;

import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Calendar;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;

import static java.util.Calendar.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL21.*;

public class Main {
    public static String jar_folder_path;
    public static String SaveFolder;
    public static String FontPath;

    public static float FOV;
    public static float nearPlane;
    public static float farPlane;
    public static int win_X;
    public static int win_Y;
    public static int OriginalWinX;
    public static int OriginalWinY;
    public static float aspectRatio;
    public static long monitor;
    public static int monitor_W;
    public static int monitor_H;
    public static boolean FullScreen;
    public static long win;
    public static float TimeElapsed;
    public static float AvgTimeElapsed;
    public static float fps;
    public static int RenderDistance;

    public static boolean showCollisionBox;
    public static boolean DrawSkyBox;

    public static int MainShaderProgram;
    public static int MainShader_Vertex;
    public static int MainShader_TextureCords;
    public static int MainShader_light;
    public static int MainShader_Projection;
    public static int MainShader_ModelMat;
    public static int MainShader_WorldMat;

    public static int TextShaderProgram;
    public static int TextShader_Vertex;
    public static int TextShader_TextureCords;

    public static double mouse_past_X;
    public static double mouse_past_Y;

    public static float playerMoveSpeed;
    public static float playerMoveSpeedSprint;
    public static float playerRotateSpeedKey;
    public static float playerRotateSpeedMouse;

    public static int MaxChunks = 10000;
    public static int ChunkCount = 0;
    public static Chunk [] ChunkArray = new Chunk[MaxChunks];

    public static CollisionBox unit_cube_collisionBox;

    enum GLStatus{
        Ready,
        Working,
        Done
    }

    public static GLStatus glGenBuffersStatus = GLStatus.Done;
    public static int glGenBuffers_Out;
    public static int glBindBuffer_In1;
    public static int glBindBuffer_In2;
    public static GLStatus glEnableVertexAttribArrayStatus = GLStatus.Done;
    public static int glEnableVertexAttribArray_In1;
    public static GLStatus glDisableVertexAttribArrayStatus = GLStatus.Done;
    public static int glDisableVertexAttribArray_In1;
    public static GLStatus glBufferDataStatus = GLStatus.Done;
    public static int glBufferData_In1;
    public static float [] glBufferData_In2;
    public static int glBufferData_In3;
    public static GLStatus glBufferSubDataStatus = GLStatus.Done;
    public static int glBufferSubData_In1;
    public static long glBufferSubData_In2;
    public static float [] glBufferSubData_In3;
    public static GLStatus glDeleteBuffersStatus = GLStatus.Done;
    public static int glDeleteBuffers_In1;


    public static int LoadShader(String Path){
        String vertex_shader_source_path = Path+"/Vertex.vsh";
        String fragment_shader_source_path = Path+"/Fragment.fsh";
        String vertex_shader_source;
        String fragment_shader_source;
        try{
            vertex_shader_source = String.join("\n", Files.readAllLines(Paths.get(vertex_shader_source_path)));
            fragment_shader_source = String.join("\n", Files.readAllLines(Paths.get(fragment_shader_source_path)));
        }
        catch(Exception e){
            System.out.println("Failed to load shader");
            e.printStackTrace();
            return 0;
        }

        int shaderProgram = glCreateProgram();
        int vertex_shader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex_shader,vertex_shader_source);
        glCompileShader(vertex_shader);
        int [] is_complied =  new int[1];
        glGetShaderiv(vertex_shader, GL_COMPILE_STATUS, is_complied);
        if(is_complied[0] == 0){
            System.out.println("Error in vertex shader");
            System.out.println(glGetShaderInfoLog(vertex_shader));
        }

        int fragment_shader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment_shader,fragment_shader_source);
        glCompileShader(fragment_shader);

        glGetShaderiv(fragment_shader, GL_COMPILE_STATUS, is_complied);
        if(is_complied[0] == 0){
            System.out.println("Error in fragment shader");
            System.out.println(glGetShaderInfoLog(fragment_shader));
        }

        glAttachShader(shaderProgram, vertex_shader);
        glAttachShader(shaderProgram, fragment_shader);

        glLinkProgram(shaderProgram);

        glGetProgramiv(shaderProgram, GL_LINK_STATUS, is_complied);
        if(is_complied[0] == 0){
            System.out.println("Error Linking shader");
            System.out.println(glGetProgramInfoLog(shaderProgram));
        }



        return shaderProgram;
    }
    public static void wincallback(long window, int X, int Y){
        win_X = X;
        win_Y = Y;
        aspectRatio = (float)X/(float)Y;
        glViewport(0,0, X, Y);
        Player.set_Projection();
    }
    public static void mouseMoveCallback(long window, double X, double Y){
        float distX = (float)(mouse_past_X-X);
        float distY = (float)(mouse_past_Y-Y);
        mouse_past_X = X;
        mouse_past_Y = Y;
        Player.Rotate_Y(playerRotateSpeedMouse*TimeElapsed*distX);
        Player.Rotate_X(playerRotateSpeedMouse*TimeElapsed*distY);
    }
    public static void mouseButtonCallback(long win, int button, int action, int mods){
        if(action == GLFW_PRESS){
            if(button == GLFW_MOUSE_BUTTON_RIGHT){
                Player.place_block();
            }
            if(button == GLFW_MOUSE_BUTTON_LEFT){
                Player.break_block();
            }
        }
    }
    public static void keycallback(long window, int key, int scancode, int action, int mods){
        if (key == GLFW_KEY_ESCAPE) {
            glfwSetWindowShouldClose(window, true);
        }
        if(action == GLFW_PRESS){
            if(mods == 0){
                if(key == GLFW_KEY_F10){
                    int [] RawScreenData = new int[win_X*win_Y*3];
                    glReadBuffer(GL_BACK);
                    glReadPixels(0,0, win_X,win_Y, GL_RGB, GL_UNSIGNED_INT, RawScreenData);
                    int [] FlipedScreenData = new int[RawScreenData.length];
                    for(int Y=0;Y<win_Y;Y++){
                        System.arraycopy(RawScreenData, (win_Y-(Y+1))*win_X*3, FlipedScreenData, Y*win_X*3, win_X*3);
                    }
                    BufferedImage img = new BufferedImage(win_X, win_Y, BufferedImage.TYPE_INT_RGB);
                    WritableRaster wr  = img.getRaster();
                    wr.setPixels(0,0,win_X,win_Y, FlipedScreenData);
                    Calendar Time = Calendar.getInstance();
                    String TimeString = Time.get(YEAR)+"."+Time.get(MONTH)+"."+Time.get(DAY_OF_MONTH)+":"+
                            Time.get(HOUR)+"."+Time.get(MINUTE)+"."+Time.get(SECOND);
                    File ScreenShotFile = new File(jar_folder_path+"/screenshots/"+TimeString+".png");
                    ScreenShotFile.mkdirs();
                    try {
                        ImageIO.write(img, "png", ScreenShotFile);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    System.out.println("Saved ScreenShot as: "+TimeString+".png");
                }
            }
            if((mods & 0x0004) > 0) {
                if (key == GLFW_KEY_F) {
                    FullScreen = !FullScreen;
                    win_X /= 2;
                    win_Y /= 2;
                    update_fullscreen();
                }
                if(key == GLFW_KEY_S){
                    DrawSkyBox = !DrawSkyBox;
                }
                if(key == GLFW_KEY_C){
                    showCollisionBox = !showCollisionBox;
                }
                if(key == GLFW_KEY_K) {
                    Player.is_flying = !Player.is_flying;
                }
                if(key == GLFW_KEY_R){
                    Player.reSpawn();
                }
                if(key == GLFW_KEY_EQUAL){
                    RenderDistance++;
                    Player.ChunkReload = true;
                }
                if(key == GLFW_KEY_MINUS){
                    RenderDistance--;
                    Player.ChunkReload = true;
                }
            }
            if(key == GLFW_KEY_O){Player.move_hotbar(-1);}
            if(key == GLFW_KEY_P){Player.move_hotbar(1);}
            if((mods & 0x0002) > 0){
                if(key == GLFW_KEY_S){
                    System.out.print("Saving...");
                    for(int X=0;X<ChunkCount;X++){
                        ChunkArray[X].Save();
                    }
                    Player.Save();
                    System.out.println("Saved");
                }
            }
        }

    }
    public static void update_fullscreen(){
        if(FullScreen) {
            glfwSetWindowMonitor(win, monitor, 0,0, monitor_W,monitor_H, 0);
        }
        else{
            glfwSetWindowMonitor(win, 0, (monitor_W/2)-(OriginalWinX/2), (monitor_H/2)-(OriginalWinY/2), OriginalWinX, OriginalWinY, 0);
        }
    }
    public static void LoadSettings(String Path) {
        try {
            JsonObject jsonObject = new JsonParser(Path).mainJsonObject;

            SaveFolder = jar_folder_path+"/"+ jsonObject.Get("SaveFolder").GetString();
            FOV = jsonObject.Get("FOV").GetFloat();
            nearPlane = jsonObject.Get("NearClippingPlane").GetFloat();
            farPlane = jsonObject.Get("FarClippingPlane").GetFloat();

            OriginalWinX = jsonObject.Get("WindowWidth").GetInt();
            OriginalWinY = jsonObject.Get("WindowHight").GetInt();
            win_X = OriginalWinX;
            win_Y = OriginalWinY;
            aspectRatio = (float) win_X/(float)win_Y;

            FullScreen = jsonObject.Get("FullScreen").GetBoolean();
            DrawSkyBox = jsonObject.Get("DrawSkyBox").GetBoolean();


            playerMoveSpeed = jsonObject.Get("playerMoveSpeed").GetFloat();
            playerRotateSpeedKey = jsonObject.Get("playerRotateSpeedKey").GetFloat();
            playerRotateSpeedMouse = jsonObject.Get("playerRotateSpeedMouse").GetFloat();
            RenderDistance = jsonObject.Get("RenderDistance").GetInt();

            FontPath = jar_folder_path+"/"+ jsonObject.Get("Font").GetString();

        } catch (Exception e) {
            System.out.print("Error While Parseing Settings: ");
            System.out.println(Path);
            e.printStackTrace();
        }
    }
    public static void MakeGLCalls(){
        if(glGenBuffersStatus == GLStatus.Ready){
            glGenBuffersStatus = GLStatus.Working;
            glGenBuffers_Out = glGenBuffers();
            glGenBuffersStatus = GLStatus.Done;
        }
        if(glEnableVertexAttribArrayStatus == GLStatus.Ready){
            glEnableVertexAttribArrayStatus = GLStatus.Working;
            glBindBuffer(glBindBuffer_In1, glBindBuffer_In2);
            glEnableVertexAttribArray(glEnableVertexAttribArray_In1);
            glEnableVertexAttribArrayStatus = GLStatus.Done;
        }
        if(glDisableVertexAttribArrayStatus == GLStatus.Ready){
            glDisableVertexAttribArrayStatus = GLStatus.Working;
            glBindBuffer(glBindBuffer_In1, glBindBuffer_In2);
            glDisableVertexAttribArray(glDisableVertexAttribArray_In1);
            glDisableVertexAttribArrayStatus = GLStatus.Done;
        }
        if(glBufferDataStatus == GLStatus.Ready){
            glBufferDataStatus = GLStatus.Working;
            glBindBuffer(glBindBuffer_In1, glBindBuffer_In2);
            glBufferData(glBufferData_In1, glBufferData_In2, glBufferData_In3);
            glBufferDataStatus = GLStatus.Done;
        }
        if(glBufferSubDataStatus == GLStatus.Ready){
            glBufferSubDataStatus = GLStatus.Working;
            glBindBuffer(glBindBuffer_In1, glBindBuffer_In2);
            glBufferSubData(glBufferSubData_In1, glBufferSubData_In2, glBufferSubData_In3);
            glBufferSubDataStatus = GLStatus.Done;
        }
        if(glDeleteBuffersStatus == GLStatus.Ready){
            glDeleteBuffersStatus = GLStatus.Working;
            glDeleteBuffers(glDeleteBuffers_In1);
            glDeleteBuffersStatus = GLStatus.Done;
        }
    }

    public static void main(String[] args){
        try {
            jar_folder_path = System.getProperty("user.dir");
        }
        catch(Exception e){
            System.out.println("Faile");
            e.printStackTrace();
            return;
        }
        LoadSettings(jar_folder_path+"/assets/solumground/Settings.json");

        File save_folder_file = new File(SaveFolder);
        if(!save_folder_file.exists()){
            save_folder_file.mkdirs(); // create file path if it doesn't exist
        }
        File playerData = new File(Main.SaveFolder+"/Player.dat");
        try {
            playerData.createNewFile();
        }
        catch(Exception e){
            System.out.println("Error Making Player.dat File");
            e.printStackTrace();
            return;
        }
        
        glfwInit();

        monitor = glfwGetPrimaryMonitor();
        //GLFWVidMode.Buffer modes = glfwGetVideoModes(monitor);
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        assert mode != null;
        monitor_W = mode.width();
        monitor_H = mode.height();

        win = glfwCreateWindow(win_X, win_Y, "SolumGround", 0, 0);
        glfwMakeContextCurrent(win);
        GL.createCapabilities();

        update_fullscreen();
        glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        if (glfwRawMouseMotionSupported()) {
            glfwSetInputMode(win, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }
        glfwSetKeyCallback(win, Main::keycallback);
        glfwSetWindowSizeCallback(win, Main::wincallback);
        glfwSetCursorPosCallback(win, Main::mouseMoveCallback);
        glfwSetMouseButtonCallback(win, Main::mouseButtonCallback);
        glfwSwapInterval(1);

        glClearColor(0,0,0,1);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        MainShaderProgram = LoadShader(jar_folder_path +"/assets/solumground/shaders/Main");
        glUseProgram(MainShaderProgram);
        MainShader_Vertex = glGetAttribLocation(MainShaderProgram, "a_position");
        MainShader_TextureCords = glGetAttribLocation(MainShaderProgram, "a_texcord");
        MainShader_light = glGetAttribLocation(MainShaderProgram, "a_light");
        MainShader_Projection = glGetUniformLocation(MainShaderProgram, "ProjectionMat");
        MainShader_ModelMat = glGetUniformLocation(MainShaderProgram, "ModelMat");
        MainShader_WorldMat = glGetUniformLocation(MainShaderProgram, "WorldMat");
        glEnableVertexAttribArray(MainShader_Vertex);
        glEnableVertexAttribArray(MainShader_TextureCords);
        glEnableVertexAttribArray(MainShader_light);

        TextShaderProgram = LoadShader(jar_folder_path +"/assets/solumground/shaders/Text");
        glUseProgram(TextShaderProgram);
        TextShader_Vertex = glGetAttribLocation(TextShaderProgram, "a_position");
        TextShader_TextureCords = glGetAttribLocation(TextShaderProgram, "a_texcord");
        glEnableVertexAttribArray(TextShader_Vertex);
        glEnableVertexAttribArray(TextShader_TextureCords);

        glUseProgram(MainShaderProgram);

        unit_cube_collisionBox = new CollisionBox(new Vec3(0,0,0), .5f,.5f,.5f, -.5f,-.5f,-.5f);


        Player.Init();
        Player.is_flying = false;
        Block.Init();
        SkyBox.Init();
        Chunk.Init();

        new Light(new Vec3(0,35,0), 1f, 2f,.5f,.5f);
        new Light(new Vec3(30,35,0), 1f, .5f,2f,.5f);
        new Light(new Vec3(15,35,-15), 1f, .5f,.5f,2f);


        ChunkLoader chunkLoader = new ChunkLoader();
        chunkLoader.loadAroundPlayer(3);
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.Build();
        chunkLoader.setDaemon(true);
        meshBuilder.setDaemon(true);
        chunkLoader.start();
        meshBuilder.start();

        Font DefaultFont = new Font(FontPath);
        Text posText = new Text("",DefaultFont, .03f, new Vec3(-1f,1f-0.12f,0));
        Text fpsText = new Text("",DefaultFont, .03f, new Vec3(-1f,1f-0.06f,0));
        Text SelectedBlockText = new Text("", DefaultFont, 0.03f, new Vec3(-1f,1f,0));

        double PastTime = glfwGetTime();
        while (!glfwWindowShouldClose(win)){
            double CurentTime = glfwGetTime();
            TimeElapsed = (float)(CurentTime-PastTime);
            PastTime = CurentTime;
            AvgTimeElapsed += (TimeElapsed-AvgTimeElapsed)/20;
            fps = 1/AvgTimeElapsed;

            MakeGLCalls();
            glfwPollEvents();

            playerMoveSpeedSprint = 1.0f;
            if (glfwGetKey(win, GLFW_KEY_LEFT_SHIFT) == 1) {
                if (Player.is_flying) {
                    Player.Move_Y(playerMoveSpeed * playerMoveSpeedSprint * TimeElapsed);
                } else {
                    playerMoveSpeedSprint = 1.5f;
                }
            }
            if(glfwGetKey(win, GLFW_KEY_SPACE) == 1){
                if(Player.is_flying) {
                    Player.Move_Y(playerMoveSpeed * playerMoveSpeedSprint * TimeElapsed * -1);
                }
                else{
                    Player.jump();
                }
            }
            if(glfwGetKey(win, GLFW_KEY_W) == 1){Player.Move_Z(playerMoveSpeed*playerMoveSpeedSprint*TimeElapsed);}
            if(glfwGetKey(win, GLFW_KEY_A) == 1){Player.Move_X(playerMoveSpeed*playerMoveSpeedSprint*TimeElapsed);}
            if(glfwGetKey(win, GLFW_KEY_S) == 1){Player.Move_Z(playerMoveSpeed*playerMoveSpeedSprint*TimeElapsed*-1);}
            if(glfwGetKey(win, GLFW_KEY_D) == 1){Player.Move_X(playerMoveSpeed*playerMoveSpeedSprint*TimeElapsed*-1);}
            if(glfwGetKey(win, GLFW_KEY_LEFT) == 1){Player.Rotate_Y(playerRotateSpeedKey*TimeElapsed);}
            if(glfwGetKey(win, GLFW_KEY_RIGHT) == 1){Player.Rotate_Y(playerRotateSpeedKey*TimeElapsed*-1);}
            if(glfwGetKey(win, GLFW_KEY_UP) == 1){Player.Rotate_X(playerRotateSpeedKey*TimeElapsed);}
            if(glfwGetKey(win, GLFW_KEY_DOWN) == 1){Player.Rotate_X(playerRotateSpeedKey*TimeElapsed*-1);}

            if(!Player.is_flying){
                Player.applyGravity();
            }
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(MainShaderProgram);
            Player.update();
            if(DrawSkyBox) {
                SkyBox.draw();
            }
            Player.Draw();
            for(int X=0;X<ChunkCount;X++){
                if(ChunkArray[X] != null) {
                    if(ChunkArray[X].status == Chunk.Status.Complete) {
                        ChunkArray[X].main_mesh.draw();
                    }
                }
            }
            glUseProgram(TextShaderProgram);
            fpsText.updateText("FPS:"+ Math.round(fps));
            fpsText.render();
            SelectedBlockText.updateText("Selected Block:"+Block.Blocks[Player.hotbar_selected].Name);
            SelectedBlockText.render();
            posText.updateText(String.format("POS: %s", Player.position));
            posText.render();

            glfwSwapBuffers(win);
        }

        chunkLoader.close();
        meshBuilder.close();
        System.out.print("Saving...");
        for(int X=0;X<ChunkCount;X++){
            ChunkArray[X].Save();
        }
        Player.Save();
        System.out.println("Saved");
        glfwDestroyWindow(win);
        glfwTerminate();
    }
}
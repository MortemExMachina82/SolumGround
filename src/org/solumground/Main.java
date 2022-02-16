package org.solumground;

import java.io.*;
import java.nio.file.*;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL21.*;




public class Main{
    public static String jar_folder_path;
    public static String SaveFolder;
    public static String FontPath;

    public static float FOV;
    public static float nearPlane;
    public static float farPlane;
    public static int win_X;
    public static int win_Y;
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

    public static boolean showColisionBox = false;
    public static boolean DrawSkyBox;

    public static int shaderProgram;
    public static int shader_vertex_position;
    public static int shader_vtcord_position;
    public static int shader_projection_position;
    public static int shader_translation_position;
    public static int shader_rotation_l_position;
    public static int shader_rotation_g_position;
    public static int shader_scean_position;
    public static int shader_doubleDraw_position;
    public static int shader_light_position;

    public static double mouse_past_X;
    public static double mouse_past_Y;

    public static float playerMoveSpeed;
    public static float playerMoveSpeedSprint;
    public static float playerRotateSpeedKey;
    public static float playerRotateSpeedMouse;

    public static int MaxChunks = 10000;
    public static int ChunkCount = 0;
    public static Chunk [] ChunkArray = new Chunk[MaxChunks];

    public static ColisionBox unit_cube_colisionBox;

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
    public static GLStatus glDeleteBuffersStatus = GLStatus.Done;
    public static int glDeleteBuffers_In1;


    public static int Init_shader(){

        String vertex_shader_source_path = jar_folder_path +"/assets/solumground/shaders/Vertex.fsh";
        String fragment_shader_source_path = jar_folder_path +"/assets/solumground/shaders/Fragment.fsh";
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

        glUseProgram(shaderProgram);


        shader_vertex_position = glGetAttribLocation(shaderProgram, "a_position");
        shader_vtcord_position = glGetAttribLocation(shaderProgram, "a_texcord");
        shader_light_position = glGetAttribLocation(shaderProgram, "a_light");

        shader_projection_position = glGetUniformLocation(shaderProgram, "projection");
        shader_translation_position = glGetUniformLocation(shaderProgram, "translation");
        shader_rotation_l_position = glGetUniformLocation(shaderProgram, "rotation_l");
        shader_rotation_g_position = glGetUniformLocation(shaderProgram, "rotation_g");
        
        shader_scean_position = glGetUniformLocation(shaderProgram, "scean");
        shader_doubleDraw_position = glGetUniformLocation(shaderProgram, "doubleDraw");

        glEnableVertexAttribArray(shader_vtcord_position);
        glEnableVertexAttribArray(shader_light_position);

        glClearColor(0,0,0,1);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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
            if((mods & 0x0004) > 0) {
                if (key == GLFW_KEY_F) {
                    FullScreen = !FullScreen;
                    update_fullscreen();
                }
                if(key == GLFW_KEY_S){
                    DrawSkyBox = !DrawSkyBox;
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
            glfwSetWindowMonitor(win, 0, (monitor_W/2)-(win_X/2), (monitor_H/2)-(win_Y/2), win_X, win_Y, 0);
        }
    }
    public static void LoadSettings(String Path) {
        String fileContents;
        try {
            fileContents = String.join("\n", Files.readAllLines(Paths.get(Path)));
        } catch (Exception e) {
            System.out.print("Failed To Load Settings: ");
            System.out.println(Path);
            e.printStackTrace();
            return;
        }
        try {
            JsonObject object = (JsonObject) Jsoner.deserialize(fileContents);

            SaveFolder = jar_folder_path+"/"+ object.get("SaveFolder");
            FOV = Float.parseFloat((String)object.get("FOV"));
            nearPlane = Float.parseFloat((String)object.get("NearClippingPlane"));
            farPlane = Float.parseFloat((String)object.get("FarClippingPlane"));

            win_X = Integer.parseInt((String)object.get("WindowWidth"));
            win_Y = Integer.parseInt((String)object.get("WindowHight"));
            aspectRatio = win_X/(float)win_Y;

            FullScreen = (boolean)object.get("FullScreen");
            //showColisionBox = (boolean)object.get("showColisionBox");
            DrawSkyBox = (boolean)object.get("DrawSkyBox");


            playerMoveSpeed = Float.parseFloat((String)object.get("playerMoveSpeed"));
            playerRotateSpeedKey = Float.parseFloat((String)object.get("playerRotateSpeedKey"));
            playerRotateSpeedMouse = Float.parseFloat((String)object.get("playerRotateSpeedMouse"));
            RenderDistance = Integer.parseInt((String)object.get("RenderDistance"));

            FontPath = jar_folder_path+"/"+ object.get("Font");

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
        
        shaderProgram = Init_shader();
        glUniform1i(shader_doubleDraw_position, 0);
        glUniform1f(shader_scean_position, 0);

        Player.Init();
        Player.is_flying = false;
        Block.Init();
        SkyBox.Init();
        Chunk.Init();

        new Light(new Vec3(0,25,0), 1f, 2f,.5f,.5f);
        new Light(new Vec3(30,25,0), 1f, .5f,2f,.5f);
        new Light(new Vec3(15,25,-15), 1f, .5f,.5f,2f);


        unit_cube_colisionBox = new ColisionBox(new Vec3(0,0,0), .5f,.5f,.5f, -.5f,-.5f,-.5f);

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
            if(!showColisionBox){Player.update();}
            if(DrawSkyBox) {
                SkyBox.draw();
            }
            if(showColisionBox){Player.update();}
            Player.Draw();
            for(int X=0;X<ChunkCount;X++){
                if(ChunkArray[X] != null) {
                    if(ChunkArray[X].main_mesh != null && ChunkArray[X].main_mesh.status == Mesh.MeshStatus.Completed) {
                        ChunkArray[X].main_mesh.draw();
                    }
                }
            }
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
        double Time = glfwGetTime();
        while(chunkLoader.isAlive()){
            double CTime = glfwGetTime();
            if(CTime-Time > 5){
                chunkLoader.interrupt();
                break;
            }
            MakeGLCalls();
        }



        glfwTerminate();
    }

}

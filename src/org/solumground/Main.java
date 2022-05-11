package org.solumground;

import org.solumground.Json.*;
import org.solumground.GUI.*;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.solumground.Universe.Star;

import javax.imageio.ImageIO;

import static java.util.Calendar.*;
import static java.util.Calendar.SECOND;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL21.*;

public class Main {
    public static String jar_folder_path;
    public static String SaveFolder;
    public static String FontPath;
    public static String SettingsPath = "assets/solumground/Settings.json";

    public static float FOV;
    public static float ZoomFOV = 15;
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
    public static float Time;
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

    public static int TwoDShaderProgram;
    public static int TwoDShader_Vertex;
    public static int TwoDShader_TextureCords;
    public static int TwoDShader_TextPos;
    public static int TwoDShader_Alpha;

    public static double mouse_past_X;
    public static double mouse_past_Y;

    public static float playerMoveSpeed;
    public static float playerMoveSpeedSprint;
    public static float playerRotateSpeedKey;
    public static float playerRotateSpeedMouse;

    public static List<Chunk> ChunkArray = new ArrayList<>(0);

    public static CollisionBox unit_cube_collisionBox;

    public static ChunkLoader chunkLoader;
    public static MeshBuilder meshBuilder;
    public static SkyBox skyBox;

    public static Font DefaultFont;

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
    public static GLStatus glTexImage2DStatus = GLStatus.Done;
    public static int glBindTexture_In1;
    public static int glBindTexture_In2;
    public static int glTexImage2D_In1;
    public static int glTexImage2D_In2;
    public static int glTexImage2D_In3;
    public static int glTexImage2D_In4;
    public static int glTexImage2D_In5;
    public static int glTexImage2D_In6;
    public static int glTexImage2D_In7;
    public static int glTexImage2D_In8;
    public static int [] glTexImage2D_In9;


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
    public static int LoadTexture(String texture_Path, int Color){
        int Texture_Buffer_Object = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, Texture_Buffer_Object);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        if(new File(texture_Path).isFile()) {
            BufferedImage img = null;
            try {
                img = ImageIO.read(new File(texture_Path));
            } catch (IOException e) {
                System.out.print("Failed to load Texture: ");
                System.out.println(texture_Path);
                e.printStackTrace();
            }
            assert img != null;
            int Texture_width = img.getTileWidth();
            int Texture_hight = img.getTileHeight();
            int [] Texture_data = new int[Texture_width * Texture_hight];
            img.getRGB(0, 0, Texture_width, Texture_hight, Texture_data, 0, Texture_width);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, Texture_width, Texture_hight, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, Texture_data);
        }
        else{
            int [] Texture_data = new int[2];
            Texture_data[0] = Color;
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1,1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, Texture_data);
        }
        return Texture_Buffer_Object;
    }
    public static void update_fullscreen(){
        if(FullScreen) {
            glfwSetWindowMonitor(win, monitor, 0,0, monitor_W,monitor_H, 0);
        }
        else{
            glfwSetWindowMonitor(win, 0, (monitor_W/2)-(OriginalWinX/2), (monitor_H/2)-(OriginalWinY/2), OriginalWinX, OriginalWinY, 0);
        }
    }
    public static void LoadSettings() {
        try {
            JsonObject jsonObject = new JsonParser(jar_folder_path+"/"+SettingsPath).mainJsonObject;

            SaveFolder = jsonObject.Get("SaveFolder").GetString();
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

            FontPath = jsonObject.Get("Font").GetString();

        } catch (Exception e) {
            System.out.print("Error While Parseing Settings: ");
            System.out.println(SettingsPath);
            e.printStackTrace();
        }
    }
    public static void SaveSettings(){
        JsonObject object = new JsonObject();
        object.Add(new JsonItem("SaveFolder", SaveFolder));
        object.Add(new JsonItem("FOV", FOV));
        object.Add(new JsonItem("NearClippingPlane", nearPlane));
        object.Add(new JsonItem("FarClippingPlane", farPlane));
        object.Add(new JsonItem("WindowWidth", OriginalWinX));
        object.Add(new JsonItem("WindowHight", OriginalWinY));
        object.Add(new JsonItem("FullScreen", FullScreen));
        object.Add(new JsonItem("DrawSkyBox", DrawSkyBox));
        object.Add(new JsonItem("playerMoveSpeed", playerMoveSpeed));
        object.Add(new JsonItem("playerRotateSpeedKey", playerRotateSpeedKey));
        object.Add(new JsonItem("playerRotateSpeedMouse", playerRotateSpeedMouse));
        object.Add(new JsonItem("RenderDistance", RenderDistance));
        object.Add(new JsonItem("Font", FontPath));

        try{
            FileOutputStream Out = new FileOutputStream(jar_folder_path+"/"+SettingsPath);
            object.write(Out);
            Out.close();
        }
        catch(Exception e){
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
        if(glTexImage2DStatus == GLStatus.Ready){
            glTexImage2DStatus = GLStatus.Working;
            glBindTexture(glBindTexture_In1, glBindTexture_In2);
            glTexImage2D(glTexImage2D_In1,glTexImage2D_In2,glTexImage2D_In3,glTexImage2D_In4,glTexImage2D_In5,glTexImage2D_In6,glTexImage2D_In7,glTexImage2D_In8,glTexImage2D_In9);
            glTexImage2DStatus = GLStatus.Done;
        }
    }
    public static void TakeScreenShot(){
        int [] RawScreenData = new int[Main.win_X*Main.win_Y*3];
        glReadBuffer(GL_BACK);
        glReadPixels(0,0, Main.win_X,Main.win_Y, GL_RGB, GL_UNSIGNED_INT, RawScreenData);
        int [] FlipedScreenData = new int[RawScreenData.length];
        for(int Y=0;Y<Main.win_Y;Y++){
            System.arraycopy(RawScreenData, (Main.win_Y-(Y+1))*Main.win_X*3, FlipedScreenData, Y*Main.win_X*3, Main.win_X*3);
        }
        BufferedImage img = new BufferedImage(Main.win_X, Main.win_Y, BufferedImage.TYPE_INT_RGB);
        WritableRaster wr  = img.getRaster();
        wr.setPixels(0,0,Main.win_X,Main.win_Y, FlipedScreenData);
        Calendar Time = Calendar.getInstance();
        String TimeString = Time.get(YEAR)+"."+Time.get(MONTH)+"."+Time.get(DAY_OF_MONTH)+":"+
                Time.get(HOUR)+"."+Time.get(MINUTE)+"."+Time.get(SECOND);
        File ScreenShotFile = new File(Main.jar_folder_path+"/screenshots/"+TimeString+".png");
        ScreenShotFile.mkdirs();
        try {
            ImageIO.write(img, "png", ScreenShotFile);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Saved ScreenShot as: "+TimeString+".png");
        Console.Print("Saved ScreenShot as: "+TimeString+".png");
    }

    public static void SaveWorld(){
        System.out.print("Saving...");
        Console.Print("Saving...");
        for(int X=0;X<ChunkArray.size();X++){
            ChunkArray.get(X).Save();
        }
        Player.Save();
        SaveSettings();
        System.out.println("Saved");
        Console.Add("Saved");
    }
    public static void UnloadWorld(){
        while(ChunkArray.size() > 0){
            Chunk chunk = ChunkArray.remove(0);
            chunk.Unload();
        }
        MeshBuilder.Genbuffer = new ArrayList<>(0);
        MeshBuilder.ReGenBuffer = new ArrayList<>(0);
        MeshBuilder.LightUpdateBuffer = new ArrayList<>(0);
    }

    public static void main(String[] args){
        try {
            //Use In InteliJ
            jar_folder_path = System.getProperty("user.dir");
            //Use For Manual Compiling
            //jar_folder_path = new File(org.solumground.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        }
        catch(Exception e){
            e.printStackTrace();
            return;
        }
        LoadSettings();

        File save_folder_file = new File(jar_folder_path+"/"+SaveFolder);
        if(!save_folder_file.exists()){
            save_folder_file.mkdirs(); // create file path if it doesn't exist
        }
        File playerData = new File(jar_folder_path+"/"+Main.SaveFolder+"/Player.dat");
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

        glfwSwapInterval(1);
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

        TwoDShaderProgram = LoadShader(jar_folder_path +"/assets/solumground/shaders/2D");
        glUseProgram(TwoDShaderProgram);
        TwoDShader_Vertex = glGetAttribLocation(TwoDShaderProgram, "a_position");
        TwoDShader_TextureCords = glGetAttribLocation(TwoDShaderProgram, "a_texcord");
        TwoDShader_TextPos = glGetUniformLocation(TwoDShaderProgram, "TextPos");
        TwoDShader_Alpha = glGetUniformLocation(TwoDShaderProgram, "Alpha");
        glEnableVertexAttribArray(TwoDShader_Vertex);
        glEnableVertexAttribArray(TwoDShader_TextureCords);

        glUseProgram(MainShaderProgram);

        unit_cube_collisionBox = new CollisionBox(new Vec3(0,0,0), .5f,.5f,.5f, -.5f,-.5f,-.5f);

        DefaultFont = new Font(Main.jar_folder_path+"/"+Main.FontPath);

        Player.Init();
        SkyBox.Init();
        Block.Init();
        Chunk.Init();
        Console.Init();
        Page.Init();


        Page.Run();
    }
}

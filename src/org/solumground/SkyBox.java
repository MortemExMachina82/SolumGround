package org.solumground;


import org.lwjgl.opengl.GL;
import org.solumground.Universe.Sector;
import org.solumground.Universe.Star;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;

public class SkyBox extends Thread{
    public static boolean ShellOneActive;
    public static boolean ShellTwoActive;
    public static boolean ShellThreeActive;

    public static Mesh ShellMesh;
    public static int TileSize = 200;
    public static int ShellTextureY = TileSize;
    public static int ShellTextureX = TileSize*26;

    public static int ShellOneTBO;
    public static int [] ShellOneTexture;
    public static int ShellTwoTBO;
    public static int [] ShellTwoTexture;
    public static int ShellThreeTBO;
    public static int [] ShellThreeTexture;


    public static Vec3 PlayerPos;
    public static boolean inturupt = false;
    public static boolean ShellOneNeedsUpdate = true;
    public static boolean ShellTwoNeedsUpdate = true;
    public static boolean ShellThreeNeedsUpdate = false;
    public static float f1 = 0.382683432f;
    public static float f2 = 0.923879533f;
    public static float f3 = 0;
    public static float f4 = 0.541196f;
    public static float [] CamAngles;
    public static boolean [] CornerMask;

    public static Sector [] sectors;
    public static long contextWindow;
    public static boolean StarNeedsInit;

    public static void Init(){
        CamAngles = new float[]{
                0,0,0, //front
                0,180,0, //back
                0,-90,0, //right
                0,90,0, //left

                0,-45,0, //front-right
                0,45,0, //front-left
                0,-135,0, //back-right
                0,135,0, //back-left

                45,-45,0, //top-front-right
                45,45,0, //top-front-left
                45,-135,0, //top-back-right
                45,135,0, //top-back-left

                -45,-45,180, //bottom-front-right
                -45,45,180, //bottom-front-left
                -45,-135,180, //bottom-back-right
                -45,135,180, //bottom-back-left

                45,0,0, //top-front
                45,180,0, //top-back
                45,-90,0, //top-right
                45,90,0, //top-left

                -45,0,180, //bottom-front
                -45,180,180, //bottom-back
                -45,-90,180, //bottom-right
                -45,90,180, //bottom-left

                90,0,0, //top
                -90,0,0, //bottom
        };
        ShellOneActive = true;
        ShellTwoActive = true;
        ShellThreeActive = false;
        ShellOneTBO = glGenTextures();
        ShellOneTexture = new int[ShellTextureX*ShellTextureY];
        glBindTexture(GL_TEXTURE_2D, ShellOneTBO);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        ShellTwoTBO = glGenTextures();
        ShellTwoTexture = new int[ShellTextureX*ShellTextureY];
        glBindTexture(GL_TEXTURE_2D, ShellTwoTBO);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        ShellThreeTBO = glGenTextures();
        ShellThreeTexture = new int[ShellTextureX*ShellTextureY];
        glBindTexture(GL_TEXTURE_2D, ShellThreeTBO);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        ShellMesh = new Mesh(ShellOneTBO);
        ShellMesh.Original_VertexArray = new float[]{
                -f1, f1, -f2, //front
                f1, f1, -f2,
                f1, -f1, -f2,
                -f1, -f1, -f2,

                f1, f1, f2, //back
                -f1, f1, f2,
                -f1, -f1, f2,
                f1, -f1, f2,

                f2, f1, -f1, //right
                f2, f1, f1,
                f2, -f1, f1,
                f2, -f1, -f1,

                -f2, f1, f1, //left
                -f2, f1, -f1,
                -f2, -f1, -f1,
                -f2, -f1, f1,

                f1, f1, -f2, //front-right
                f2, f1, -f1,
                f2, -f1, -f1,
                f1, -f1, -f2,

                -f2, f1, -f1, //front-left
                -f1, f1, -f2,
                -f1, -f1, -f2,
                -f2, -f1, -f1,

                f2, f1, f1, //back-right
                f1, f1, f2,
                f1, -f1, f2,
                f2, -f1, f1,

                -f1, f1, f2, //back-left
                -f2, f1, f1,
                -f2, -f1, f1,
                -f1, -f1, f2,

                f3,f2,-f4, //top-front-right
                f4,f2,-f3,
                f2,f1,-f1,
                f1,f1,-f2,

                -f4,f2,-f3, //top-front-left
                -f3,f2,-f4,
                -f1,f1,-f2,
                -f2,f1,-f1,

                f4,f2,f3, //top-back-right
                f3,f2,f4,
                f1,f1,f2,
                f2,f1,f1,

                -f3,f2,f4, //top-back-left
                -f4,f2,f3,
                -f2,f1,f1,
                -f1,f1,f2,

                f4,-f2,-f3, //bottom-front-right
                f3,-f2,-f4,
                f1,-f1,-f2,
                f2,-f1,-f1,

                -f3,-f2,-f4, //bottom-front-left
                -f4,-f2,-f3,
                -f2,-f1,-f1,
                -f1,-f1,-f2,

                f3,-f2,f4, //bottom-back-right
                f4,-f2,f3,
                f2,-f1,f1,
                f1,-f1,f2,

                -f4,-f2,f3, //bottom-back-left
                -f3,-f2,f4,
                -f1,-f1,f2,
                -f2,-f1,f1,

                -f1, f2, -f1, //top-front
                f1, f2, -f1,
                f1, f1, -f2,
                -f1, f1, -f2,

                f1, f2, f1, //top-back
                -f1, f2, f1,
                -f1, f1, f2,
                f1, f1, f2,

                f1, f2, -f1, //top-right
                f1, f2, f1,
                f2, f1, f1,
                f2, f1, -f1,

                -f1, f2, f1, //top-left
                -f1, f2, -f1,
                -f2, f1, -f1,
                -f2, f1, f1,

                f1,-f2,-f1, //bottom-front
                -f1,-f2,-f1,
                -f1,-f1,-f2,
                f1,-f1,-f2,

                -f1,-f2,f1, //bottom-back
                f1,-f2,f1,
                f1,-f1,f2,
                -f1,-f1,f2,

                f1,-f2,f1, //bottom-right
                f1,-f2,-f1,
                f2,-f1,-f1,
                f2,-f1,f1,

                -f1,-f2,-f1, //bottom-left
                -f1,-f2,f1,
                -f2,-f1,f1,
                -f2,-f1,-f1,

                -f1, f2, f1, //top
                f1, f2, f1,
                f1, f2, -f1,
                -f1, f2, -f1,

                -f1, -f2, -f1, //bottom
                f1, -f2, -f1,
                f1, -f2, f1,
                -f1, -f2, f1,
        };
        ShellMesh.VTcords_array = new float[26*8];
        for(int X=0;X<26;X++){
            ShellMesh.VTcords_array[X*8] = (float)X/26.0f;
            ShellMesh.VTcords_array[X*8 + 1] = 0;
            ShellMesh.VTcords_array[X*8 + 2] = (1.0f+X)/26.0f;
            ShellMesh.VTcords_array[X*8 + 3] = 0;
            ShellMesh.VTcords_array[X*8 + 4] = (1.0f+X)/26.0f;
            ShellMesh.VTcords_array[X*8 + 5] = 1;
            ShellMesh.VTcords_array[X*8 + 6] = (float)X/26.0f;
            ShellMesh.VTcords_array[X*8 + 7] = 1;
        }

        ShellMesh.QuadFaceArray = new int[26*4*2];
        for(int X=0;X<26*4;X++){
            ShellMesh.QuadFaceArray[X*2] = X;
            ShellMesh.QuadFaceArray[X*2 + 1] = X;
        }

        ShellMesh.Number_of_Verts = 26*4;
        ShellMesh.Number_of_vtcords = 26*4;
        ShellMesh.Number_of_QuadFaces = 26;

        ShellMesh.setIs_skyBox();
        ShellMesh.FullLight = true;
        ShellMesh.position = new Vec3(-10, 200, -10);
        ShellMesh.upload_Vertex_data();

        CornerMask = new boolean[TileSize*TileSize];
        for(int Y=0;Y<TileSize;Y++){
            for(int X=0;X<TileSize;X++){
                if((float)(X) / Y < 0.35f){
                    CornerMask[Y*TileSize + X] = true;
                }
                if((float)(TileSize-(X)) / Y < 0.35f){
                    CornerMask[Y*TileSize + X] = true;
                }
            }
        }

        sectors = new Sector[8];
        sectors[0] = new Sector(0,0,0);
        sectors[1] = new Sector(-1,0,0);
        sectors[2] = new Sector(0,-1,0);
        sectors[3] = new Sector(-1,-1,0);
        sectors[4] = new Sector(0,0,-1);
        sectors[5] = new Sector(-1,0,-1);
        sectors[6] = new Sector(0,-1,-1);
        sectors[7] = new Sector(-1,-1,-1);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        contextWindow = glfwCreateWindow(TileSize, TileSize, "", 0, 0);
        StarNeedsInit = true;
    }
    public void run(){
        glfwMakeContextCurrent(contextWindow);
        GL.createCapabilities();

        float FarPlane = 10000000;
        float NearPlane = 1000;
        float [] projection_mat = new float[4*4];

        projection_mat[0] = 1 / (float)Math.tan(45*.5*3.1415/180);
        projection_mat[5] = 1 / (float)Math.tan(45*.5*3.1415/180);
        projection_mat[10] = ((FarPlane+NearPlane) / (NearPlane-FarPlane));
        projection_mat[11] = -1;
        projection_mat[14] = ((FarPlane*NearPlane) / (NearPlane-FarPlane))*2;

        int shader = Main.LoadShader(Main.RootDir +"/assets/solumground/shaders/Main");
        glUseProgram(shader);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glReadBuffer(GL_BACK);
        glClearColor(0,0,0,0);
        glEnableVertexAttribArray(Main.MainShader_Vertex);
        glEnableVertexAttribArray(Main.MainShader_TextureCords);
        glEnableVertexAttribArray(Main.MainShader_light);

        glUniformMatrix4fv(Main.MainShader_Projection, false, projection_mat);

        Vec3 Rotation = new Vec3();
        float [] RotationMat = new float[16];
        float [] TransMat = new float[16];
        float [] WorldMatrix = new float[16];
        TransMat[0] = 1;
        TransMat[5] = 1;
        TransMat[10] = 1;
        TransMat[15] = 1;

        if(StarNeedsInit) {
            Star.Init();
            StarNeedsInit = false;
        }

        PlayerPos = new Vec3();
        inturupt = false;
        while(!inturupt){
            while(!(ShellOneNeedsUpdate || ShellTwoNeedsUpdate || ShellThreeNeedsUpdate)){
                try{
                    Thread.sleep(1000);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            PlayerPos.X = Player.position.X;
            PlayerPos.Y = Player.position.Y;
            PlayerPos.Z = Player.position.Z;

            TransMat[12] = -PlayerPos.X;
            TransMat[13] = -PlayerPos.Y;
            TransMat[14] = -PlayerPos.Z;

            for(int X=0;X<26;X++){
                Rotation.X = CamAngles[X*3];
                Rotation.Y = CamAngles[X*3 + 1];
                Rotation.Z = CamAngles[X*3 + 2];
                Math3D.Make3DRotationMatrix44(Rotation, RotationMat);
                Math3D.Matrix44_Multiply(TransMat,RotationMat, WorldMatrix);
                glUniformMatrix4fv(Main.MainShader_WorldMat, false, WorldMatrix);

                if(ShellOneNeedsUpdate){
                    DrawShellOne(X);
                }
                if(ShellTwoNeedsUpdate){
                    DrawShellTwo(X);
                }
                if(ShellThreeNeedsUpdate){
                    DrawShellThree(X);
                }
            }
            if(ShellOneNeedsUpdate){
                UpdateShell(ShellOneTBO, ShellOneTexture);
                ShellOneNeedsUpdate = false;
            }
            if(ShellTwoNeedsUpdate){
                UpdateShell(ShellTwoTBO, ShellTwoTexture);
                ShellTwoNeedsUpdate = false;
            }
            if(ShellThreeNeedsUpdate){
                UpdateShell(ShellThreeTBO, ShellThreeTexture);
                ShellThreeNeedsUpdate = false;
            }

        }
        glDeleteProgram(shader);

    }
    public void DrawShellOne(int side){
        glClearColor(0,0,0,0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        for(Sector sector : sectors) {
            sector.Draw(PlayerPos);
        }
        CopyTile(side, ShellOneTexture);
    }
    public void DrawShellTwo(int side){
        glClearColor(0,.2f,.3f, .1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        CopyTile(side, ShellTwoTexture);
    }
    public void DrawShellThree(int side){

    }
    public void CopyTile(int side, int [] TBO){
        int [] RawScreenData = new int[TileSize*TileSize];
        glReadPixels (0,0,TileSize,TileSize,GL_RGBA,GL_UNSIGNED_BYTE,RawScreenData);
        if(side > 7 && side < 16) {
            for(int Y=0;Y<TileSize;Y++){
                for(int X=0;X<TileSize;X++){
                    if(CornerMask[Y*TileSize + X]){
                        RawScreenData[Y*TileSize + X] = 0x00000000; //make alpha
                    }
                }
            }
        }
        for (int Y = 0; Y < TileSize; Y++) {
            System.arraycopy(RawScreenData, Y * TileSize, TBO, Y * ShellTextureX + side * TileSize, TileSize);
        }
    }
    public void UpdateShell(int TBO, int [] Tex){
        while(Main.glTexImage2DStatus != Main.GLStatus.Done){
            try {
                Thread.sleep(1);
            }
            catch(Exception e1){
                e1.printStackTrace();
                return;
            }
        }
        Main.glBindTexture_In1 = GL_TEXTURE_2D;
        Main.glBindTexture_In2 = TBO;
        Main.glTexImage2D_In1 = GL_TEXTURE_2D;
        Main.glTexImage2D_In2 = 0;
        Main.glTexImage2D_In3 = GL_RGBA;
        Main.glTexImage2D_In4 = ShellTextureX;
        Main.glTexImage2D_In5 = ShellTextureY;
        Main.glTexImage2D_In6 = 0;
        Main.glTexImage2D_In7 = GL_RGBA;
        Main.glTexImage2D_In8 = GL_UNSIGNED_INT_8_8_8_8_REV;
        Main.glTexImage2D_In9 = Tex;
        Main.glTexImage2DStatus = Main.GLStatus.Ready;
    }
    public void kill(){
        inturupt = true;
    }

    public static void Draw(){
        if(ShellOneActive){
            ShellMesh.Texture_Buffer_Object = ShellOneTBO;
            ShellMesh.Scale(1,1,1);
            ShellMesh.draw();
        }
        if(ShellTwoActive){
            ShellMesh.Texture_Buffer_Object = ShellTwoTBO;
            ShellMesh.Scale(.9f,.9f,.9f);
            ShellMesh.draw();
        }
        if(ShellThreeActive){
            ShellMesh.Texture_Buffer_Object = ShellThreeTBO;
            ShellMesh.Scale(.8f,.8f,.8f);
            ShellMesh.draw();
        }
    }
}
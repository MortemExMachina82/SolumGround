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
    public static boolean Simple;
    public static boolean ShellOneActive;
    public static boolean ShellTwoActive;
    public static boolean ShellThreeActive;

    public static Mesh ShellOneMesh;
    public static Mesh ShellTwoMesh;
    public static Mesh ShellThreeMesh;
    public static int FancyVBO;
    public static int SimpleVBO;
    public static int TileSize = 200; //if !Simple TileSize Must Be >= 200
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
    public static float [] SimpleCamAngles;
    public static float [] FancyCamAngles;
    public static float CamFOV;
    public static float FarPlane = 10000000;
    public static float NearPlane = 1000;
    public static float [] projection_mat = new float[16];
    public static int [] CornerMask;

    public static Sector [] sectors;
    public static long contextWindow;
    public static boolean StarNeedsInit;

    public static void Init(){
        FancyCamAngles = new float[]{
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
        SimpleCamAngles = new float[]{
                0,0,0, //front
                0,180,0, //back
                0,-90,0, //right
                0,90,0, //left
                90,0,0, //top
                -90,0,0 //bottom
        };
        CamAngles = FancyCamAngles;
        CamFOV = 45;
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

        ShellOneMesh = new Mesh(ShellOneTBO);
        ShellOneMesh.Original_VertexArray = new float[]{
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
        ShellOneMesh.VTcords_array = new float[26*8];
        for(int X=0;X<26;X++){
            ShellOneMesh.VTcords_array[X*8] = (float)X/26.0f;
            ShellOneMesh.VTcords_array[X*8 + 1] = 0;
            ShellOneMesh.VTcords_array[X*8 + 2] = (1.0f+X)/26.0f;
            ShellOneMesh.VTcords_array[X*8 + 3] = 0;
            ShellOneMesh.VTcords_array[X*8 + 4] = (1.0f+X)/26.0f;
            ShellOneMesh.VTcords_array[X*8 + 5] = 1;
            ShellOneMesh.VTcords_array[X*8 + 6] = (float)X/26.0f;
            ShellOneMesh.VTcords_array[X*8 + 7] = 1;
        }

        ShellOneMesh.QuadFaceArray = new int[26*4*2];
        for(int X=0;X<26*4;X++){
            ShellOneMesh.QuadFaceArray[X*2] = X;
            ShellOneMesh.QuadFaceArray[X*2 + 1] = X;
        }

        ShellOneMesh.Number_of_Verts = 26*4;
        ShellOneMesh.Number_of_vtcords = 26*4;
        ShellOneMesh.Number_of_QuadFaces = 26;

        ShellOneMesh.setIs_skyBox();
        ShellOneMesh.position = new Vec3(-10,145.3f,-10);
        ShellOneMesh.FullLight = true;
        ShellOneMesh.upload_Vertex_data();

        FancyVBO = ShellOneMesh.VertexBufferObject;
        ShellTwoMesh = new Mesh(ShellOneMesh);
        ShellTwoMesh.Texture_Buffer_Object = ShellTwoTBO;
        glDeleteBuffers(ShellTwoMesh.VertexBufferObject);
        ShellTwoMesh.VertexBufferObject = FancyVBO;
        ShellTwoMesh.upload_Vertex_data();
        ShellThreeMesh = new Mesh(ShellOneMesh);
        ShellThreeMesh.Texture_Buffer_Object = ShellThreeTBO;
        glDeleteBuffers(ShellThreeMesh.VertexBufferObject);
        ShellThreeMesh.VertexBufferObject = FancyVBO;

        ShellOneMesh.Scale(1,1,1);
        ShellTwoMesh.Scale(.9f,.9f,.9f);
        ShellThreeMesh.Scale(.8f,.8f,.8f);



        ShellOneMesh.Init_VBO();
        ShellOneMesh.Original_VertexArray = new float[]{
                -1,1,-1, //front
                1,1,-1,
                1,-1,-1,
                -1,-1,-1,

                1,1,1, //back
                -1,1,1,
                -1,-1,1,
                1,-1,1,

                1,1,-1,
                1,1,1, //right
                1,-1,1,
                1,-1,-1,

                -1,1,1, //left
                -1,1,-1,
                -1,-1,-1,
                -1,-1,1,

                -1,1,1, //top
                1,1,1,
                1,1,-1,
                -1,1,-1,

                -1,-1,-1, //bottom
                1,-1,-1,
                1,-1,1,
                -1,-1,1,
        };
        ShellOneMesh.VTcords_array = new float[48];
        for(int X=0;X<6;X++){
            ShellOneMesh.VTcords_array[X*8] = (float)X/6.0f;
            ShellOneMesh.VTcords_array[X*8 + 1] = 0;
            ShellOneMesh.VTcords_array[X*8 + 2] = (1.0f+X)/6.0f;
            ShellOneMesh.VTcords_array[X*8 + 3] = 0;
            ShellOneMesh.VTcords_array[X*8 + 4] = (1.0f+X)/6.0f;
            ShellOneMesh.VTcords_array[X*8 + 5] = 1;
            ShellOneMesh.VTcords_array[X*8 + 6] = (float)X/6.0f;
            ShellOneMesh.VTcords_array[X*8 + 7] = 1;
        }
        ShellOneMesh.QuadFaceArray = new int[48];
        for(int X=0;X<24;X++){
            ShellOneMesh.QuadFaceArray[X*2] = X;
            ShellOneMesh.QuadFaceArray[X*2 + 1] = X;
        }


        ShellOneMesh.Number_of_QuadFaces = 6;
        ShellOneMesh.FullLight = true;
        ShellOneMesh.upload_Vertex_data();
        SimpleVBO = ShellOneMesh.VertexBufferObject;

        ShellOneMesh.VertexBufferObject = FancyVBO;
        ShellOneMesh.Number_of_QuadFaces = 26;
        ShellOneMesh.Calculate_Data_Positions();

        CornerMask = new int[TileSize*TileSize];
        for(int Y=0;Y<TileSize;Y++){
            for(int X=0;X<TileSize;X++){
                if((float)(X) / Y < 0.35f){
                    CornerMask[Y*TileSize + X] = 0x00000000;
                }
                else if((float)(TileSize-(X)) / Y < 0.6f){
                    CornerMask[Y*TileSize + X] = 0x00000000;
                }
                else{
                    CornerMask[Y*TileSize + X] = 0xFFFFFFFF;
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
    public static void SetSimple(boolean value){
        Simple = value;
        if(Simple){
            ShellTextureX = TileSize*6;
            CamAngles = SimpleCamAngles;
            CamFOV = 90;
            ShellOneMesh.VertexBufferObject = SimpleVBO;
            ShellOneMesh.Number_of_QuadFaces = 6;
            ShellTwoMesh.VertexBufferObject = SimpleVBO;
            ShellTwoMesh.Number_of_QuadFaces = 6;
            ShellThreeMesh.VertexBufferObject = SimpleVBO;
            ShellThreeMesh.Number_of_QuadFaces = 6;
        }
        else{
            ShellTextureX = TileSize*26;
            CamAngles = FancyCamAngles;
            CamFOV = 45;
            ShellOneMesh.VertexBufferObject = FancyVBO;
            ShellOneMesh.Number_of_QuadFaces = 26;
            ShellTwoMesh.VertexBufferObject = FancyVBO;
            ShellTwoMesh.Number_of_QuadFaces = 26;
            ShellThreeMesh.VertexBufferObject = FancyVBO;
            ShellThreeMesh.Number_of_QuadFaces = 26;
        }
        ShellOneMesh.Calculate_Data_Positions();
        ShellTwoMesh.Calculate_Data_Positions();
        ShellThreeMesh.Calculate_Data_Positions();
        ShellOneTexture = new int[ShellTextureX*ShellTextureY];
        ShellTwoTexture = new int[ShellTextureX*ShellTextureY];
        ShellThreeTexture = new int[ShellTextureX*ShellTextureY];
        ShellOneNeedsUpdate = true;
        ShellTwoNeedsUpdate = true;
        ShellThreeNeedsUpdate = true;
        projection_mat[0] = 1 / (float)Math.tan(CamFOV*.5*3.1415/180);
        projection_mat[5] = 1 / (float)Math.tan(CamFOV*.5*3.1415/180);
        projection_mat[10] = ((FarPlane+NearPlane) / (NearPlane-FarPlane));
        projection_mat[11] = -1;
        projection_mat[14] = ((FarPlane*NearPlane) / (NearPlane-FarPlane))*2;
    }
    public void run(){
        glfwMakeContextCurrent(contextWindow);
        GL.createCapabilities();

        projection_mat[0] = 1 / (float)Math.tan(CamFOV*.5*3.1415/180);
        projection_mat[5] = 1 / (float)Math.tan(CamFOV*.5*3.1415/180);
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

            for(int X=0;X<CamAngles.length/3;X++){
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
        glClearColor(0,.2f,.3f, .3f);
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
                    TBO[Y * ShellTextureX + side * TileSize + X] = RawScreenData[Y*TileSize + X]&CornerMask[Y*TileSize + X];
                }
            }
            return;
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
        if (ShellOneActive) {
            ShellOneMesh.draw();
        }
        if (ShellTwoActive) {
            ShellTwoMesh.draw();
        }
        if (ShellThreeActive) {
            ShellThreeMesh.draw();
        }
    }
}
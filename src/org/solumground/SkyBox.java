package org.solumground;



import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.lang.*;
import java.lang.Math.*;
import javax.imageio.*;
import java.awt.Image.*;
import java.awt.image.BufferedImage;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.Jsoner;

import org.lwjgl.*;
import org.lwjgl.opengl.*;



import static org.lwjgl.opengl.GL21.*;


public class SkyBox {
    public static int textureTileSize;
    public static int textureWidth;
    public static int textureHight;
    public static String meshPath;
    public static String texturePath;

    public static Mesh skyBoxMesh;
    public static int [] texture;
    public static int MaxRecursion;

    public static boolean Generated;
    public static boolean OneTexture;
    public static String DirectoryModelPath;
    public static String DirectoryTexturePath;
    public static String TextureName;
    public static String ModelName;

    public static String [] ModelNames = new String[6];
    public static String [] TextureNames = new String[6];


    public static void Init(){
        String Path = Main.jar_folder_path+"/assets/solumground/SkyBoxSettings.json";
        String fileContents;
        try {
            fileContents = String.join("\n", Files.readAllLines(Paths.get(Path)));
        } catch (Exception e) {
            System.out.print("Failed To Load SkyBox Settings Data: ");
            System.out.println(Path);
            e.printStackTrace();
            return;
        }
        try {
            JsonObject object = (JsonObject) Jsoner.deserialize(fileContents);

            Generated = (boolean) object.get("Generated");
            OneTexture = (boolean) object.get("OneTexture");
            DirectoryModelPath = (String) object.get("DirectoryModelPath");
            DirectoryTexturePath = (String) object.get("DirectoryTexturePath");
            ModelName = (String) object.get("ModelName");
            TextureName = (String) object.get("TextureName");

            if (!Generated & !OneTexture) {
                JsonArray modelarray = (JsonArray) object.get("Model");
                for (int X = 0; X < 6; X++) {
                    JsonArray side = (JsonArray) modelarray.get(X);
                    if ("Right".equals((String) side.get(0))) {
                        ModelNames[X] = Main.jar_folder_path+"/"+DirectoryModelPath+"/"+(String) side.get(1);
                        TextureNames[X] = Main.jar_folder_path+"/"+DirectoryTexturePath+"/"+(String) side.get(2);
                    }
                    if ("Left".equals((String) side.get(0))) {
                        ModelNames[X] = Main.jar_folder_path+"/"+DirectoryModelPath+"/"+(String) side.get(1);
                        TextureNames[X] = Main.jar_folder_path+"/"+DirectoryTexturePath+"/"+(String) side.get(2);
                    }
                    if ("Top".equals((String) side.get(0))) {
                        ModelNames[X] = Main.jar_folder_path+"/"+DirectoryModelPath+"/"+(String) side.get(1);
                        TextureNames[X] = Main.jar_folder_path+"/"+DirectoryTexturePath+"/"+(String) side.get(2);
                    }
                    if ("Bottom".equals((String) side.get(0))) {
                        ModelNames[X] = Main.jar_folder_path+"/"+DirectoryModelPath+"/"+(String) side.get(1);
                        TextureNames[X] = Main.jar_folder_path+"/"+DirectoryTexturePath+"/"+(String) side.get(2);
                    }
                    if ("Front".equals((String) side.get(0))) {
                        ModelNames[X] = Main.jar_folder_path+"/"+DirectoryModelPath+"/"+(String) side.get(1);
                        TextureNames[X] = Main.jar_folder_path+"/"+DirectoryTexturePath+"/"+(String) side.get(2);
                    }
                    if ("Back".equals((String) side.get(0))) {
                        ModelNames[X] = Main.jar_folder_path+"/"+DirectoryModelPath+"/"+(String) side.get(1);
                        TextureNames[X] = Main.jar_folder_path+"/"+DirectoryTexturePath+"/"+(String) side.get(2);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.print("Error While Parseing SkyBox Settings: ");
            System.out.println(Path);
            e.printStackTrace();
            return;
        }

        if(Generated){
            make(DirectoryModelPath+"/"+ModelName);
        }
        else{
            if(OneTexture){
                meshPath = Main.jar_folder_path+"/"+DirectoryModelPath+"/"+ModelName;
                texturePath = Main.jar_folder_path+"/"+DirectoryTexturePath+"/"+TextureName;

                skyBoxMesh = new Mesh(meshPath,texturePath);
                skyBoxMesh.setIs_skyBox();
            }
            else{
                Init_fromFiles();
            }
        }


    }

    public static void Init_fromFiles(){
        int FullTexMaxSizeX = 0;
        int FullTexMaxSizeY = 0;
        BufferedImage img = null;
        for(int X=0;X< 6;X++){
            try {
                img = ImageIO.read(new File(TextureNames[X]));
            } catch (IOException e) {
                System.out.print("Failed to load Texture: ");
                System.out.println(TextureNames[X]);
                e.printStackTrace();
            }
            int Texture_width = img.getTileWidth();
            int Texture_hight = img.getTileHeight();
            FullTexMaxSizeX += Texture_width;
            if(Texture_hight > FullTexMaxSizeY){
                FullTexMaxSizeY = Texture_hight;
            }
        }
        int CompTextureSizeX = FullTexMaxSizeX;
        int CompTextureSizeY = FullTexMaxSizeY;
        int [] CompTexture = new int[CompTextureSizeX*CompTextureSizeY];

        int [] TexPos = new int[6];
        int [] TexSizeX = new int[6];
        int [] TexSizeY = new int[6];
        int TexOffset = 0;
        for(int X=0;X<6;X++) {
            try {
                img = ImageIO.read(new File(TextureNames[X]));
            } catch (IOException e) {
                System.out.print("Failed to load Texture: ");
                System.out.println(TextureNames[X]);
                e.printStackTrace();
            }
            int Texture_width = img.getTileWidth();
            int Texture_hight = img.getTileHeight();
            TexSizeX[X] = Texture_width;
            TexSizeY[X] = Texture_hight;

            int[] Texture_data = new int[Texture_width * Texture_hight];
            img.getRGB(0, 0, Texture_width, Texture_hight, Texture_data, 0, Texture_width);

            for (int Y = 0; Y < Texture_hight; Y++) {
                for (int Z = 0; Z < Texture_width; Z++) {
                    CompTexture[Y * CompTextureSizeX + (Z + TexOffset)] = Texture_data[Y * Texture_width + Z];
                }
            }
            TexPos[X] = TexOffset;
            TexOffset += Texture_width;
        }
        int TextureBufferObject = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, TextureBufferObject);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, CompTextureSizeX, CompTextureSizeY, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, CompTexture);

        skyBoxMesh = new Mesh(TextureBufferObject);
        skyBoxMesh.setIs_skyBox();

        for(int Y=0;Y<6;Y++) {
            Mesh mesh = new Mesh(ModelNames[Y], Mesh.MESH_SMOBJ);
            int Pos = TexPos[Y];
            int SizeX = TexSizeX[Y];
            int SizeY = TexSizeY[Y];

            for(int Z=0;Z<mesh.Number_of_vtcords;Z++){
                float U = mesh.VTcords_array[Z*2 + 0];
                float V = mesh.VTcords_array[Z*2 + 1];
                U = U * ((float)SizeX/CompTextureSizeX);
                V = V * ((float)SizeY/CompTextureSizeY);
                U += ((float)Pos/CompTextureSizeX);
                mesh.VTcords_array[Z*2 + 0] = U;
                mesh.VTcords_array[Z*2 + 1] = V;
            }
            skyBoxMesh.add(mesh);
        }
        skyBoxMesh.upload_Vertex_data();

    }

    public static void make(String meshpath){
        meshPath = meshpath;

        skyBoxMesh = new Mesh(meshPath, Mesh.MESH_SMOBJ);
        skyBoxMesh.setIs_skyBox();

        textureTileSize = 800;
        textureWidth = textureTileSize*3;
        textureHight = textureTileSize*2;
        texture = new int[textureWidth*textureHight];

        MaxRecursion = 10;

        for(int X=-10;X<textureWidth+10;X+=40){
            for(int Y=-10;Y<textureHight+10;Y+=40){
                star(X, Y, 0);
            }
        }

        glBindTexture(GL_TEXTURE_2D, skyBoxMesh.Texture_Buffer_Object);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth,textureHight, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, texture);

    }
    public static void star(int X,int Y,int CurentRecursion){
        if(CurentRecursion >= MaxRecursion){
            return;
        }
        double rotation = Math.random()*3.1415*2;
        double distance = Math.random()*25 + 25;

        int RotatedX = (int)(0*Math.cos(rotation) + distance*Math.sin(rotation));
        int RotatedY = (int)(0*Math.sin(rotation)*-1 + distance*Math.cos(rotation));

        int FinalX = X+RotatedX;
        int FinalY = Y+RotatedY;

        if(FinalX >= 0 && FinalX < textureWidth){
            if(FinalY >= 0 && FinalY < textureHight){
                float Brightness = (float)Math.sin(rotation);
                //Brightness = .5f;
                int colorRGB = (int)(0x7F*Brightness);
                //colorRGB = 0xFF;
                int color = 0xFF000000;
                color += colorRGB;
                color += colorRGB<<8;
                color += colorRGB<<16;
                texture[FinalY*textureWidth + FinalX] = color;

            }
        }
        CurentRecursion++;
        star(FinalX,FinalY, CurentRecursion);
    }

    public static void draw(){
        skyBoxMesh.draw();
    }
}












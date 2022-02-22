package org.solumground;

import org.solumground.Json.JsonArray;
import org.solumground.Json.JsonObject;
import org.solumground.Json.JsonParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL21.*;

public class Block {
    public String Name;
    public int ID;
    public boolean Full;
    public String DirectoryModelPath;
    public String DirectoryTexturePath;
    public boolean HasCollisionBox;

    public String [] Models;
    public String [] Texture;
    public Mesh [] Sides;
    public Mesh mesh;
    public CollisionBox collisionBox;

    public static int Number;
    public static List<String> Textures = new ArrayList<>(1);
    public static Block [] Blocks;
    public static int CompTextureSizeX;
    public static int CompTextureSizeY;
    public static int [] CompTexture;
    public static int TextureBufferObject;

    public static int [] TexPos;
    public static int [] TexSizeX;
    public static int [] TexSizeY;

    public static void ConvertTextureCords(Mesh mesh, Block block, int Index){
        String tex = block.Texture[Index];
        int index = 0;
        for(int Z=0;Z<Textures.size();Z++){
            if(Textures.get(Z).equals(tex)){
                index = Z;
                break;
            }
        }
        int Pos = TexPos[index];
        int SizeX = TexSizeX[index];
        int SizeY = TexSizeY[index];

        for(int Z=0;Z<mesh.Number_of_vtcords;Z++){
            float U = mesh.VTcords_array[Z*2];
            float V = mesh.VTcords_array[Z*2 + 1];
            U = U * ((float)SizeX/CompTextureSizeX);
            V = V * ((float)SizeY/CompTextureSizeY);
            U += ((float)Pos/CompTextureSizeX);
            mesh.VTcords_array[Z*2] = U;
            mesh.VTcords_array[Z*2 + 1] = V;
        }
    }

    public static void Init() {
        File blocks_dir = new File(Main.jar_folder_path+"/assets/solumground/blocks");
        String [] filenames = blocks_dir.list();
        assert filenames != null;
        Blocks = new Block[filenames.length];
        for (String filename : filenames) {
            Block block = new Block(Main.jar_folder_path + "/assets/solumground/blocks/" + filename);
            Blocks[block.ID] = block;
            Number++;
        }


        List<String> NewTex = new ArrayList<>(Textures.size());
        for (String Tex : Textures) {
            boolean exists = false;
            for (String newTex : NewTex) {
                if (Tex.equals(newTex)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                NewTex.add(Tex);
            }
        }
        Textures = NewTex;

        int FullTexMaxSizeX = 0;
        int FullTexMaxSizeY = 0;


        BufferedImage img = null;
        for (String texture : Textures) {
            try {
                img = ImageIO.read(new File(texture));
            } catch (Exception e) {
                System.out.print("Failed to load Texture: ");
                System.out.println(texture);
                e.printStackTrace();
            }
            assert img != null;
            int Texture_width = img.getTileWidth();
            int Texture_hight = img.getTileHeight();
            FullTexMaxSizeX += Texture_width;
            if (Texture_hight > FullTexMaxSizeY) {
                FullTexMaxSizeY = Texture_hight;
            }
        }
        CompTextureSizeX = FullTexMaxSizeX;
        CompTextureSizeY = FullTexMaxSizeY;
        CompTexture = new int[CompTextureSizeX*CompTextureSizeY];

        TexPos = new int[Textures.size()];
        TexSizeX = new int[Textures.size()];
        TexSizeY = new int[Textures.size()];
        int TexOffset = 0;
        for(int X=0;X<Textures.size();X++){
            try {
                img = ImageIO.read(new File(Textures.get(X)));
            } catch (IOException e) {
                System.out.print("Failed to load Texture: ");
                System.out.println(Textures.get(X));
                e.printStackTrace();
            }
            int Texture_width = img.getTileWidth();
            int Texture_hight = img.getTileHeight();
            TexSizeX[X] = Texture_width;
            TexSizeY[X] = Texture_hight;

            int [] Texture_data = new int[Texture_width * Texture_hight];
            img.getRGB(0, 0, Texture_width, Texture_hight, Texture_data, 0, Texture_width);

            for(int Y=0;Y<Texture_hight;Y++){
                for(int Z=0;Z<Texture_width;Z++){
                    CompTexture[Y*CompTextureSizeX + (Z+TexOffset)] = Texture_data[Y*Texture_width + Z];
                }
            }
            TexPos[X] = TexOffset;
            TexOffset += Texture_width;
        }


        for(int X=1;X< Blocks.length;X++){
            Block block = Blocks[X];
            if(block.Full) {
                for(int Y=0;Y<6;Y++) {
                    Mesh mesh = new Mesh(block.Models[Y], Mesh.MESH_SMOBJ);
                    block.Sides[Y] = mesh;
                    ConvertTextureCords(mesh, block, Y);
                }
            }
            else{
                block.mesh = new Mesh(block.Models[0], Mesh.MESH_SMOBJ);
                ConvertTextureCords(block.mesh, block, 0);
            }
        }
        TextureBufferObject = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, TextureBufferObject);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, CompTextureSizeX, CompTextureSizeY, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, CompTexture);


    }

    public Block(String File){
        try {
            JsonObject jsonObject = new JsonParser(File).mainJsonObject;
            this.Name = jsonObject.Get("Name").GetString();
            this.ID = jsonObject.Get("ID").GetInt();
            this.Full = jsonObject.Get("Full").GetBoolean();
            this.DirectoryModelPath = jsonObject.Get("DirectoryModelPath").GetString();
            this.DirectoryTexturePath = jsonObject.Get("DirectoryTexturePath").GetString();
            this.HasCollisionBox = jsonObject.Get("HasCollisionBox").GetBoolean();


            if(Full) {
                Models = new String[6];
                Texture = new String[6];
                Sides = new Mesh[6];
                if(HasCollisionBox) {
                    this.collisionBox = Main.unit_cube_collisionBox;
                }
                JsonArray modelarray = jsonObject.Get("Model").GetArray();
                for (int Y = 0; Y < 6; Y++) {
                    JsonArray sidearray = modelarray.Get(Y).GetArray();
                    String Side = sidearray.Get(0).GetString();
                    String FileName = sidearray.Get(1).GetString();
                    String TexturePath = sidearray.Get(2).GetString();

                    if(Side.equals("Right")){
                        Models[0] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[0] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures.add(Texture[0]);
                    }

                    if(Side.equals("Left")){
                        Models[1] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[1] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures.add(Texture[1]);
                    }
                    if(Side.equals("Top")){
                        Models[2] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[2] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures.add(Texture[2]);
                    }
                    if(Side.equals("Bottom")){
                        Models[3] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[3] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures.add(Texture[3]);
                    }
                    if(Side.equals("Front")){
                        Models[4] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[4] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures.add(Texture[4]);
                    }
                    if(Side.equals("Back")){
                        Models[5] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[5] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures.add(Texture[5]);
                    }
                }
            }
            else{
                Models = new String[1];
                Texture = new String[1];

                JsonArray modelarray = jsonObject.Get("Model").GetArray();
                if(modelarray != null) {
                    Models[0] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+modelarray.Get(0).GetString();
                    Texture[0] = Main.jar_folder_path + "/" + this.DirectoryTexturePath + "/" + modelarray.Get(1).GetString();
                    Textures.add(Texture[0]);
                }
                if(HasCollisionBox){
                    JsonArray CBarray = jsonObject.Get("CollisionBoxSizes").GetArray();
                    float BPX = CBarray.Get(0).GetFloat();
                    float BPY = CBarray.Get(1).GetFloat();
                    float BPZ = CBarray.Get(2).GetFloat();
                    float BNX = CBarray.Get(3).GetFloat();
                    float BNY = CBarray.Get(4).GetFloat();
                    float BNZ = CBarray.Get(5).GetFloat();
                    this.collisionBox = new CollisionBox(new Vec3(), BPX,BPY,BPZ, BNX,BNY,BNZ);
                }
            }
        }
        catch(Exception e){
            System.out.print("Error While Parseing Block: ");
            System.out.println(File);
            e.printStackTrace();
        }
    }

}
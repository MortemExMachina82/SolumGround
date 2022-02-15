package org.solumground;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL21.*;

public class Block {
    public String Name;
    public int ID;
    public boolean Full;
    public String DirectoryModelPath;
    public String DirectoryTexturePath;

    public String [] Models;
    public String [] Texture;
    public Mesh [] Sides;

    public static int Number;
    public static String [] Textures = new String[0];
    public static Block [] Blocks;
    public static int CompTextureSizeX;
    public static int CompTextureSizeY;
    public static int [] CompTexture;
    public static int TextureBufferObject;

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


        String [] NewTex = new String[Textures.length];
        int NewTexCount = 0;
        for (String Tex : Textures) {
            boolean exists = false;
            for (int Y = 0; Y < NewTexCount; Y++) {
                if (Tex.equals(NewTex[Y])) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                NewTex[NewTexCount] = Tex;
                NewTexCount++;
            }
        }
        Textures = new String[NewTexCount];
        System.arraycopy(NewTex, 0, Textures, 0, NewTexCount);
        int FullTexMaxSizeX = 0;
        int FullTexMaxSizeY = 0;


        BufferedImage img = null;
        for (String texture : Textures) {
            try {
                img = ImageIO.read(new File(texture));
            } catch (IOException e) {
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

        int [] TexPos = new int[Textures.length];
        int [] TexSizeX = new int[Textures.length];
        int [] TexSizeY = new int[Textures.length];
        int TexOffset = 0;
        for(int X=0;X<Textures.length;X++){
            try {
                img = ImageIO.read(new File(Textures[X]));
            } catch (IOException e) {
                System.out.print("Failed to load Texture: ");
                System.out.println(Textures[X]);
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
                    String tex = block.Texture[Y];
                    int index = 0;
                    for(int Z=0;Z<Textures.length;Z++){
                        if(Textures[Z].equals(tex)){
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
                        //U=0;
                        //V=0;
                        mesh.VTcords_array[Z*2] = U;
                        mesh.VTcords_array[Z*2 + 1] = V;
                    }
                }
            }
            else{
                Mesh mesh = new Mesh(block.Models[0], Mesh.MESH_SMOBJ);

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
            String fileContents = String.join("\n", Files.readAllLines(Paths.get(File)));

            JsonObject object = (JsonObject)Jsoner.deserialize(fileContents);
            this.Name = (String)object.get("Name");
            this.ID = ((BigDecimal)object.get("ID")).intValue();
            this.Full = (boolean)object.get("Full");
            this.DirectoryModelPath = (String)object.get("DirectoryModelPath");
            this.DirectoryTexturePath = (String)object.get("DirectoryTexturePath");

            if(Full) {
                Models = new String[6];
                Texture = new String[6];
                Sides = new Mesh[6];
                JsonArray modelarray = (JsonArray)object.get("Model");
                int OldLength = Textures.length;
                String [] paths = new String[OldLength+6];
                System.arraycopy(Textures, 0, paths, 0, Textures.length);

                Textures = paths;
                for (int Y = 0; Y < 6; Y++) {
                    JsonArray sidearray = (JsonArray) modelarray.get(Y);
                    String Side = (String) sidearray.get(0);
                    String FileName = (String) sidearray.get(1);
                    String TexturePath = (String) sidearray.get(2);

                    if(Side.equals("Right")){
                        Models[0] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[0] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures[OldLength] = Texture[0];
                    }

                    if(Side.equals("Left")){
                        Models[1] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[1] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures[OldLength+1] = Texture[1];
                    }
                    if(Side.equals("Top")){
                        Models[2] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[2] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures[OldLength+2] = Texture[2];
                    }
                    if(Side.equals("Bottom")){
                        Models[3] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[3] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures[OldLength+3] = Texture[3];
                    }
                    if(Side.equals("Front")){
                        Models[4] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[4] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures[OldLength+4] = Texture[4];
                    }
                    if(Side.equals("Back")){
                        Models[5] = Main.jar_folder_path+"/"+this.DirectoryModelPath+"/"+FileName;
                        Texture[5] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                        Textures[OldLength+5] = Texture[5];
                    }
                }
            }
            else{
                Models = new String[1];
                Sides = new Mesh[1];
                JsonArray modelarray = (JsonArray)object.get("Model");
                if(modelarray != null) {
                    JsonArray sidearray = (JsonArray) modelarray.get(0);
                    String Side = (String) sidearray.get(0);
                    String FileName = (String) sidearray.get(1);
                    String TexturePath = (String) sidearray.get(2);
                    Texture[0] = Main.jar_folder_path+"/"+this.DirectoryTexturePath+"/"+TexturePath;
                    if(Side.equals("Model")){
                        Models[0] = FileName;
                        Textures[0] = Texture[0];
                    }
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
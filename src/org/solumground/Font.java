package org.solumground;

import org.solumground.Json.*;

import java.io.*;

import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static org.lwjgl.opengl.GL21.*;

public class Font {
    public String filepath;
    public String texpath;

    public String name;

    public int [] widths;

    public int Texture_width;
    public int Texture_hight;
    public int Texture_Buffer_Object;


    public Font(String Path){
        this.filepath = Path;
        this.widths = new int[127];
        try{
            JsonObject jsonObject = new JsonParser(Path).mainJsonObject;

            this.name = jsonObject.Get("name").GetString();
            this.texpath = Main.jar_folder_path+"/"+ jsonObject.Get("texturePath").GetString();
            boolean fixedWidth = jsonObject.Get("fixed-width").GetBoolean();
            if(fixedWidth){
                int width = jsonObject.Get("widths").GetInt();
                Arrays.fill(this.widths, width);
            }
            else{
                JsonArray jsonArray = jsonObject.Get("widths").GetArray();
                for(int X=0;X<this.widths.length;X++){
                    int element = jsonArray.Get(X).GetInt();
                    this.widths[X] = element;
                }
            }
        }
        catch(Exception e){
            System.out.print("Error While Parseing Font: ");
            System.out.println(this.filepath);
            e.printStackTrace();
            return;
        }

        //int [] widthAcum = new int[255];
        int acum = 0;
        for(int X=0;X<widths.length;X++){
            acum += this.widths[X];
            this.widths[X] = acum;
            //System.out.println(this.widths[X]);
        }



        Texture_Buffer_Object = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, Texture_Buffer_Object);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);


        BufferedImage img;
        try {
            img = ImageIO.read(new File(this.texpath));
        } catch (IOException e) {
            System.out.println("Error Loading Font Texture");
            e.printStackTrace();
            return;
        }
        Texture_width = img.getTileWidth();
        Texture_hight = img.getTileHeight();

        int [] Texture_data = new int[Texture_width * Texture_hight];
        img.getRGB(0, 0, Texture_width, Texture_hight, Texture_data, 0, Texture_width);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, Texture_width, Texture_hight, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, Texture_data);
        glEnableVertexAttribArray(Main.TwoDShader_TextureCords);
    }
    public int getWidth(int X){
        return this.widths[X-1]-this.widths[X-2];
    }
    public int getAcumWidth(int X){
        return this.widths[X-1];
    }
}

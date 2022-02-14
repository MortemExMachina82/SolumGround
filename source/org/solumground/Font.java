package org.solumground;


import java.io.*;
import java.nio.*;
import java.nio.file.*;
import javax.imageio.*;
import java.awt.Image.*;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import org.lwjgl.*;
import org.lwjgl.opengl.*;

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
        String fileContents;
        this.widths = new int[127];
        try{
            fileContents = String.join("\n", Files.readAllLines(Paths.get(this.filepath)));
        }
        catch(Exception e){
            System.out.print("Failed To Load Font: ");
            System.out.println(this.filepath);
            System.out.println(e);
            return;
        }
        try{
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject)parser.parse(fileContents);

            this.name = (String)object.get("name");
            this.texpath = Main.jar_folder_path+"/"+(String)object.get("texturePath");
            boolean fixedWidth = (boolean)object.get("fixed-width");
            if(fixedWidth){
                int width = Long.valueOf((long)object.get("widths")).intValue();
                for(int X=0;X<this.widths.length;X++){
                    this.widths[X] = width;
                }
            }
            else{
                JSONArray array = (JSONArray)object.get("widths");
                for(int X=0;X<this.widths.length;X++){
                    long element = (long)array.get(X);
                    this.widths[X] = Long.valueOf(element).intValue();
                }
            }
        }
        catch(Exception e){
            System.out.print("Error While Parseing Font: ");
            System.out.println(this.filepath);
            System.out.println(e);
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


        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(this.texpath));
        } catch (IOException e) {
            System.out.println("Error Loading Font Texture");
            System.out.println(e);
            return;
        }
        Texture_width = img.getTileWidth();
        Texture_hight = img.getTileHeight();

        int [] Texture_data = new int[Texture_width * Texture_hight];
        img.getRGB(0, 0, Texture_width, Texture_hight, Texture_data, 0, Texture_width);


        Texture_data = new int[Texture_width * Texture_hight];
        img.getRGB(0, 0, Texture_width, Texture_hight, Texture_data, 0, Texture_width);


        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, Texture_width, Texture_hight, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, Texture_data);

    }
    public int getWidth(int X){
        return this.widths[X-1]-this.widths[X-2];
    }
    public int getAcumWidth(int X){
        return this.widths[X-1];
    }
}

package org.solumground;

import org.lwjgl.opengl.GL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.lwjgl.opengl.GL21.*;

public class Mesh{
    String modelPath;
    String texturePath;
    boolean has_tex;
    boolean has_triangles;
    boolean has_quads;
    int Number_of_Verts;
    int Number_of_TriFaces;
    int Number_of_QuadFaces;
    int Number_of_vtcords;
    float [] Original_VertexArray;
    float [] VTcords_array;
    int [] TriFaceArray;
    int [] QuadFaceArray;
    int Texture_width;
    int Texture_hight;
    int VertexBufferObject;
    int Texture_Buffer_Object;
    boolean TBO_gen;
    Vec3 position = new Vec3(0,0,0);
    float XRot;
    float YRot;
    float ZRot;
    boolean is_skyBox;
    boolean FullLight = false;

    enum MeshStatus {
        NotDone,
        Completed
    }

    public MeshStatus status = MeshStatus.NotDone;

    public static int MESH_TEXTURE = 1;
    public static int MESH_SMOBJ = 2;

    public static boolean compareByteArray(ByteBuffer b1,int start,int end, byte [] b2){
        int bytestrue = 0;
        int b2count = 0;
        for(int X=start;X<end;X++){
            if(b1.get(X) == b2[b2count]){bytestrue++;}
            b2count++;
        }
        return bytestrue == (end - start);
    }
    public static int get_pos(ByteBuffer bb, int curent_pos, byte [] serchfor){
        int count = curent_pos;
        int position = -1;
        while(true){

            if(compareByteArray(bb, count,count+serchfor.length, serchfor)){
                position = count+serchfor.length;
                break;

            }
            if(count+serchfor.length == bb.capacity()){break;}
            count++;
        }
        return position;
    }

    public void LoadSMOBJ(String model_Path) {
        if(Objects.equals(model_Path, "")){
            this.has_triangles = true;
            this.has_quads = true;

            this.Number_of_Verts = 0;
            this.Number_of_vtcords = 0;
            this.Number_of_TriFaces = 0;
            this.Number_of_QuadFaces = 0;

            this.Original_VertexArray = new float[0];
            this.VTcords_array = new float[0];
            this.TriFaceArray = new int[0];
            this.QuadFaceArray = new int[0];

            return;
        }

        if (!Files.exists(Paths.get(model_Path))) {
            System.out.println("Error Loading smobj: File Does Not Exist");
            return;
        }
        byte[] model_data;
        try {
            model_data = Files.readAllBytes(Paths.get(model_Path));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ByteBuffer bb = ByteBuffer.wrap(model_data);


        byte[] smobj = "smobj{version=".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] version = "1.0".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] attributes = "attributes{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] VERTS = "VERTS{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] TEXTURECORDS = "TEXTURECORDS{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] TRIANGLEFACES1 = "TRIANGLEFACES1{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] TRIANGLEFACES2 = "TRIANGLEFACES2{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] TRIANGLEFACES3 = "TRIANGLEFACES3{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] QUADFACES1 = "QUADFACES1{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] QUADFACES2 = "QUADFACES2{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] QUADFACES3 = "QUADFACES3{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        //bb.get(line, 0, 15);
        if (!compareByteArray(bb, 0, 14, smobj)) {
            System.out.println("Failed To Load smobj: wrong data");
            return;
        }
        //bb.get(line, 15,18);
        if (compareByteArray(bb, 14, 17, version)) {
            System.out.println("Error smobj version is unreadable");
            return;
        }
        int count = 17;

        int position = get_pos(bb, count, attributes);

        int MaxX = bb.getInt(position + 7);
        int MaxY = bb.getInt(position + 20);
        int MaxZ = bb.getInt(position + 33);


        this.Number_of_Verts = bb.getInt(position + 7 + 13 * 3 + 1);
        byte t_or_f = bb.get(position + 7 + 13 * 3 + 17);
        this.has_tex = t_or_f == 't';
        this.Number_of_vtcords = bb.getInt(position + 7 + 13 * 3 + 1 + 17 + 17);
        this.Number_of_TriFaces = bb.getInt(position + 7 + 13 * 3 + 1 + 17 + 17 + 22);
        this.Number_of_QuadFaces = bb.getInt(position + 7 + 13 * 3 + 1 + 17 + 17 + 22 + 18);
        this.has_triangles = this.Number_of_TriFaces != 0;
        this.has_quads = this.Number_of_QuadFaces != 0;


        count = position + 7 + 13 * 3 + 1 + 17 + 17 + 22;
        position = get_pos(bb, count, VERTS);


        this.Original_VertexArray = new float[this.Number_of_Verts * 3];
        this.VTcords_array = new float[this.Number_of_vtcords * 2];
        if (this.has_tex) {
            this.TriFaceArray = new int[this.Number_of_TriFaces * 6];
            this.QuadFaceArray = new int[this.Number_of_QuadFaces * 8];
        } else {
            this.TriFaceArray = new int[this.Number_of_TriFaces * 3];
            this.QuadFaceArray = new int[this.Number_of_QuadFaces * 4];
        }

        for (int VertC = 0; VertC < this.Number_of_Verts; VertC++) {
            this.Original_VertexArray[VertC * 3] = ((float) bb.getShort(position + (VertC * 6)) * MaxX / 32767.0f);
            this.Original_VertexArray[VertC * 3 + 1] = ((float) bb.getShort(position + (VertC * 6) + 2) * MaxY / 32767.0f) * -1;
            this.Original_VertexArray[VertC * 3 + 2] = ((float) bb.getShort(position + (VertC * 6) + 4) * MaxZ / 32767.0f);
        }

        count = position + (this.Number_of_Verts * 3 * 2);
        position = get_pos(bb, count, TEXTURECORDS);

        for (int VTC = 0; VTC < this.Number_of_vtcords; VTC++) {
            this.VTcords_array[VTC * 2] = (float) (Short.toUnsignedInt(bb.getShort(position + (VTC * 4)))) / 65535.0f;
            this.VTcords_array[VTC * 2 + 1] = 1 - (float) (Short.toUnsignedInt(bb.getShort(position + (VTC * 4) + 2))) / 65535.0f;
        }

        count = position + (this.Number_of_vtcords * 2 * 2);

        int facesize = 1;
        position = get_pos(bb, count, TRIANGLEFACES1);
        if (position == -1) {
            position = get_pos(bb, count, TRIANGLEFACES2);
            if (position == -1) {
                position = get_pos(bb, count, TRIANGLEFACES3);
                facesize = 3;
            } else {
                facesize = 2;
            }
        }

        for (int FaceC = 0; FaceC < this.Number_of_TriFaces; FaceC++) {
            if (this.has_tex) {
                if (facesize == 1) {
                    this.TriFaceArray[FaceC * 6] = bb.get(position + (FaceC * facesize * 6));
                    this.TriFaceArray[FaceC * 6 + 1] = bb.get(position + (FaceC * facesize * 6) + facesize);
                    this.TriFaceArray[FaceC * 6 + 2] = bb.get(position + (FaceC * facesize * 6) + facesize * 2);
                    this.TriFaceArray[FaceC * 6 + 3] = bb.get(position + (FaceC * facesize * 6) + facesize * 3);
                    this.TriFaceArray[FaceC * 6 + 4] = bb.get(position + (FaceC * facesize * 6) + facesize * 4);
                    this.TriFaceArray[FaceC * 6 + 5] = bb.get(position + (FaceC * facesize * 6) + facesize * 5);
                }
                if (facesize == 2) {
                    this.TriFaceArray[FaceC * 6] = bb.getShort(position + (FaceC * facesize * 6));
                    this.TriFaceArray[FaceC * 6 + 1] = bb.getShort(position + (FaceC * facesize * 6) + facesize);
                    this.TriFaceArray[FaceC * 6 + 2] = bb.getShort(position + (FaceC * facesize * 6) + facesize * 2);
                    this.TriFaceArray[FaceC * 6 + 3] = bb.getShort(position + (FaceC * facesize * 6) + facesize * 3);
                    this.TriFaceArray[FaceC * 6 + 4] = bb.getShort(position + (FaceC * facesize * 6) + facesize * 4);
                    this.TriFaceArray[FaceC * 6 + 5] = bb.getShort(position + (FaceC * facesize * 6) + facesize * 5);
                }

            } else {
                if (facesize == 1) {
                    this.TriFaceArray[FaceC * 3] = bb.get(position + (FaceC * facesize * 3));
                    this.TriFaceArray[FaceC * 3 + 1] = bb.get(position + (FaceC * facesize * 3) + facesize);
                    this.TriFaceArray[FaceC * 3 + 2] = bb.get(position + (FaceC * facesize * 3) + facesize * 2);
                }
                if (facesize == 2) {
                    this.TriFaceArray[FaceC * 3] = bb.getShort(position + (FaceC * facesize * 3));
                    this.TriFaceArray[FaceC * 3 + 1] = bb.getShort(position + (FaceC * facesize * 3) + facesize);
                    this.TriFaceArray[FaceC * 3 + 2] = bb.getShort(position + (FaceC * facesize * 3) + facesize * 2);
                }
            }
        }
        position = get_pos(bb, count, QUADFACES1);
        if (position == -1) {
            position = get_pos(bb, count, QUADFACES2);
            if (position == -1) {
                position = get_pos(bb, count, QUADFACES3);
                facesize = 3;
            } else {
                facesize = 2;
            }
        } else {
            facesize = 1;
        }
        for (int FaceC = 0; FaceC < this.Number_of_QuadFaces; FaceC++) {
            if (this.has_tex) {
                if (facesize == 1) {
                    this.QuadFaceArray[FaceC * 8] = bb.get(position + (FaceC * facesize * 8));
                    this.QuadFaceArray[FaceC * 8 + 1] = bb.get(position + (FaceC * facesize * 8) + facesize);
                    this.QuadFaceArray[FaceC * 8 + 2] = bb.get(position + (FaceC * facesize * 8) + facesize * 2);
                    this.QuadFaceArray[FaceC * 8 + 3] = bb.get(position + (FaceC * facesize * 8) + facesize * 3);
                    this.QuadFaceArray[FaceC * 8 + 4] = bb.get(position + (FaceC * facesize * 8) + facesize * 4);
                    this.QuadFaceArray[FaceC * 8 + 5] = bb.get(position + (FaceC * facesize * 8) + facesize * 5);
                    this.QuadFaceArray[FaceC * 8 + 6] = bb.get(position + (FaceC * facesize * 8) + facesize * 6);
                    this.QuadFaceArray[FaceC * 8 + 7] = bb.get(position + (FaceC * facesize * 8) + facesize * 7);
                }
                if (facesize == 2) {
                    this.QuadFaceArray[FaceC * 8] = bb.getShort(position + (FaceC * facesize * 8));
                    this.QuadFaceArray[FaceC * 8 + 1] = bb.getShort(position + (FaceC * facesize * 8) + facesize);
                    this.QuadFaceArray[FaceC * 8 + 2] = bb.getShort(position + (FaceC * facesize * 8) + facesize * 2);
                    this.QuadFaceArray[FaceC * 8 + 3] = bb.getShort(position + (FaceC * facesize * 8) + facesize * 3);
                    this.QuadFaceArray[FaceC * 8 + 4] = bb.getShort(position + (FaceC * facesize * 8) + facesize * 4);
                    this.QuadFaceArray[FaceC * 8 + 5] = bb.getShort(position + (FaceC * facesize * 8) + facesize * 5);
                    this.QuadFaceArray[FaceC * 8 + 6] = bb.getShort(position + (FaceC * facesize * 8) + facesize * 6);
                    this.QuadFaceArray[FaceC * 8 + 7] = bb.getShort(position + (FaceC * facesize * 8) + facesize * 7);
                }
            } else {
                if (facesize == 1) {
                    this.QuadFaceArray[FaceC * 4] = bb.get(position + (FaceC * facesize * 4));
                    this.QuadFaceArray[FaceC * 4 + 1] = bb.get(position + (FaceC * facesize * 4) + facesize);
                    this.QuadFaceArray[FaceC * 4 + 2] = bb.get(position + (FaceC * facesize * 4) + facesize * 2);
                    this.QuadFaceArray[FaceC * 4 + 3] = bb.get(position + (FaceC * facesize * 4) + facesize * 3);
                }
                if (facesize == 2) {
                    this.QuadFaceArray[FaceC * 4] = bb.getShort(position + (FaceC * facesize * 4));
                    this.QuadFaceArray[FaceC * 4 + 1] = bb.getShort(position + (FaceC * facesize * 4) + facesize);
                    this.QuadFaceArray[FaceC * 4 + 2] = bb.getShort(position + (FaceC * facesize * 4) + facesize * 2);
                    this.QuadFaceArray[FaceC * 4 + 3] = bb.getShort(position + (FaceC * facesize * 4) + facesize * 3);
                }
            }
        }
        upload_Vertex_data();
    }
    public void LoadTexture(String texture_Path){
        this.Texture_Buffer_Object = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.Texture_Buffer_Object);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);



        if(!texture_Path.equals("")) {

            BufferedImage img = null;
            try {
                img = ImageIO.read(new File(texture_Path));
            } catch (IOException e) {
                System.out.print("Failed to load Texture: ");
                System.out.println(texture_Path);
                e.printStackTrace();
            }
            assert img != null;
            this.Texture_width = img.getTileWidth();
            this.Texture_hight = img.getTileHeight();

            int [] Texture_data = new int[this.Texture_width * this.Texture_hight];
            img.getRGB(0, 0, this.Texture_width, this.Texture_hight, Texture_data, 0, this.Texture_width);


            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.Texture_width, this.Texture_hight, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, Texture_data);
        }
        else{
            int [] Texture_data = new int[2];
            Texture_data[0] = 0xFF0000FF;
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1,1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, Texture_data);
        }

    }
    public Mesh(String model_Path,String texture_Path){
        this.modelPath = model_Path;
        this.texturePath = texture_Path;
        this.has_tex = !texture_Path.equals("");

        Init_VBO();
        LoadSMOBJ(this.modelPath);

        LoadTexture(this.texturePath);

        this.TBO_gen = true;

        this.position = new Vec3(0,0,0);
        this.XRot = 0;
        this.YRot = 0;
        this.ZRot = 0;
        this.is_skyBox = false;
    }
    public Mesh(String Path, int type){
        if(type == 1){
            this.modelPath = "";
            this.texturePath = Path;
            this.has_tex = true;
        }
        if(type == 2){
            this.modelPath = Path;
            this.texturePath = "";
            this.has_tex = false;
        }
        Init_VBO();

        LoadSMOBJ(this.modelPath);
        LoadTexture(this.texturePath);
        this.TBO_gen = true;

        this.position = new Vec3(0,0,0);
        this.XRot = 0;
        this.YRot = 0;
        this.ZRot = 0;
        this.is_skyBox = false;

    }
    public Mesh(int TBO){
        this.modelPath = "";
        this.texturePath = "";
        this.Texture_Buffer_Object = TBO;
        this.has_tex = true;


        Init_VBO();

        LoadSMOBJ(this.modelPath);
        this.TBO_gen = false;

        this.position = new Vec3(0,0,0);
        this.XRot = 0;
        this.YRot = 0;
        this.ZRot = 0;
        this.is_skyBox = false;
    }
    public Mesh(Mesh mesh){
        this.modelPath = mesh.modelPath;
        this.texturePath = mesh.texturePath;
        this.has_tex = mesh.has_tex;
        this.has_triangles = mesh.has_triangles;
        this.has_quads = mesh.has_quads;
        this.Number_of_Verts = mesh.Number_of_Verts;
        this.Number_of_TriFaces = mesh.Number_of_TriFaces;
        this.Number_of_QuadFaces = mesh.Number_of_QuadFaces;
        this.Number_of_vtcords = mesh.Number_of_vtcords;
        this.Original_VertexArray = mesh.Original_VertexArray;
        this.VTcords_array = mesh.VTcords_array;
        this.TriFaceArray = mesh.TriFaceArray;
        this.QuadFaceArray = mesh.QuadFaceArray;
        this.Texture_width = mesh.Texture_width;
        this.Texture_hight = mesh.Texture_width;
        this.VertexBufferObject = mesh.VertexBufferObject;
        this.Texture_Buffer_Object = mesh.Texture_Buffer_Object;
        this.TBO_gen = false;
        this.FullLight = mesh.FullLight;

        glBindBuffer(GL_ARRAY_BUFFER, this.VertexBufferObject);
        glEnableVertexAttribArray(Main.shader_vertex_position);
        glEnableVertexAttribArray(Main.shader_vtcord_position);



        this.position = new Vec3(0,0,0);
        this.XRot = 0;
        this.YRot = 0;
        this.ZRot = 0;
        this.is_skyBox = false;


    }

    public void Init_VBO(){
        if(Thread.currentThread().getName().equals("main")){
            this.VertexBufferObject = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, this.VertexBufferObject);
            glEnableVertexAttribArray(Main.shader_vertex_position);
            if (this.has_tex) {
                glEnableVertexAttribArray(Main.shader_vtcord_position);
            } else {
                glDisableVertexAttribArray(Main.shader_vtcord_position);
            }
        }
        else{
            while(Main.glGenBuffersStatus != Main.GLStatus.Done){
                try {
                    Thread.sleep(1);
                }
                catch(Exception e1){
                    e1.printStackTrace();
                    return;
                }
            }
            Main.glGenBuffersStatus = Main.GLStatus.Ready;
            while(Main.glGenBuffersStatus != Main.GLStatus.Done){
                try {
                    Thread.sleep(1);
                }
                catch(Exception e1){
                    e1.printStackTrace();
                    return;
                }
            }
            this.VertexBufferObject = Main.glGenBuffers_Out;


            while(Main.glEnableVertexAttribArrayStatus != Main.GLStatus.Done){
                try {
                    Thread.sleep(1);
                }
                catch(Exception e1){
                    e1.printStackTrace();
                    return;
                }
            }
            Main.glBindBuffer_In1 = GL_ARRAY_BUFFER;
            Main.glBindBuffer_In2 = this.VertexBufferObject;
            Main.glEnableVertexAttribArray_In1 = Main.shader_vertex_position;
            Main.glEnableVertexAttribArrayStatus = Main.GLStatus.Ready;


            if (this.has_tex) {
                while(Main.glEnableVertexAttribArrayStatus != Main.GLStatus.Done){
                    try {
                        Thread.sleep(1);
                    }
                    catch(Exception e1){
                        e1.printStackTrace();
                        return;
                    }
                }
                Main.glBindBuffer_In1 = GL_ARRAY_BUFFER;
                Main.glBindBuffer_In2 = this.VertexBufferObject;
                Main.glEnableVertexAttribArray_In1 = Main.shader_vtcord_position;
                Main.glEnableVertexAttribArrayStatus = Main.GLStatus.Ready;
            }
            else{
                while(Main.glEnableVertexAttribArrayStatus != Main.GLStatus.Done){
                    try {
                        Thread.sleep(1);
                    }
                    catch(Exception e1){
                        e1.printStackTrace();
                        return;
                    }
                }
                Main.glBindBuffer_In1 = GL_ARRAY_BUFFER;
                Main.glBindBuffer_In2 = this.VertexBufferObject;
                Main.glDisableVertexAttribArray_In1 = Main.shader_vtcord_position;
                Main.glDisableVertexAttribArrayStatus = Main.GLStatus.Ready;
            }


        }
    }
    public void add(Mesh mesh){
        if(this.Number_of_Verts*3 + mesh.Number_of_Verts*3 > this.Original_VertexArray.length){
            float [] New_VertexArray = new float[(this.Original_VertexArray.length + mesh.Original_VertexArray.length)*2];
            System.arraycopy(this.Original_VertexArray, 0, New_VertexArray, 0, this.Original_VertexArray.length);
            this.Original_VertexArray = New_VertexArray;
        }
        if(this.Number_of_vtcords*2 + mesh.Number_of_vtcords*2 > this.VTcords_array.length){
            float [] New_VTcordsArray = new float[(this.VTcords_array.length + mesh.VTcords_array.length)*2];
            System.arraycopy(this.VTcords_array, 0, New_VTcordsArray, 0, this.VTcords_array.length);
            this.VTcords_array = New_VTcordsArray;
        }
        if(this.has_tex) {
            if (this.Number_of_TriFaces * 6 + mesh.Number_of_TriFaces * 6 > this.TriFaceArray.length) {
                int [] New_TriFaceArray = new int[(this.TriFaceArray.length + mesh.TriFaceArray.length)*2];
                System.arraycopy(this.TriFaceArray, 0, New_TriFaceArray, 0, this.TriFaceArray.length);
                this.TriFaceArray = New_TriFaceArray;
            }
            if(this.Number_of_QuadFaces * 8 + mesh.Number_of_QuadFaces * 8 > this.QuadFaceArray.length){
                int [] New_QuadFaceArray = new int[(this.QuadFaceArray.length + mesh.QuadFaceArray.length)*2];
                System.arraycopy(this.QuadFaceArray, 0, New_QuadFaceArray, 0, this.QuadFaceArray.length);
                this.QuadFaceArray = New_QuadFaceArray;
            }
        }
        else{
            if(this.Number_of_TriFaces * 3 + mesh.Number_of_TriFaces * 3 > this.TriFaceArray.length){
                int [] New_TriFaceArray = new int[(this.TriFaceArray.length + mesh.TriFaceArray.length)*2];
                System.arraycopy(this.TriFaceArray, 0, New_TriFaceArray, 0, this.TriFaceArray.length);
                this.TriFaceArray = New_TriFaceArray;
            }
            if(this.Number_of_QuadFaces * 4 + mesh.Number_of_QuadFaces * 4 > this.QuadFaceArray.length){
                int [] New_QuadFaceArray = new int[(this.QuadFaceArray.length + mesh.QuadFaceArray.length)*2];
                System.arraycopy(this.QuadFaceArray, 0, New_QuadFaceArray, 0, this.QuadFaceArray.length);
                this.QuadFaceArray = New_QuadFaceArray;
            }
        }


        for(int X=0;X<mesh.Number_of_Verts;X++){
            this.Original_VertexArray[this.Number_of_Verts*3 + X*3] = mesh.Original_VertexArray[X*3] + (mesh.position.X);
            this.Original_VertexArray[this.Number_of_Verts*3 + X*3 + 1] = mesh.Original_VertexArray[X*3 + 1] + (mesh.position.Y);
            this.Original_VertexArray[this.Number_of_Verts*3 + X*3 + 2] = mesh.Original_VertexArray[X*3 + 2] + (mesh.position.Z);
        }
        System.arraycopy(mesh.VTcords_array, 0, this.VTcords_array, this.Number_of_vtcords*2, mesh.VTcords_array.length);

        if(this.has_tex) {
            for (int X = 0; X < mesh.Number_of_TriFaces*3;X++) {
                this.TriFaceArray[this.Number_of_TriFaces*6 + X*2] = this.Number_of_Verts + mesh.TriFaceArray[X*2];
                this.TriFaceArray[this.Number_of_TriFaces*6 + X*2 + 1] = this.Number_of_vtcords + mesh.TriFaceArray[X*2 + 1];
            }
            for(int X=0;X<mesh.Number_of_QuadFaces*4;X++){
                this.QuadFaceArray[this.Number_of_QuadFaces*8 + X*2] = this.Number_of_Verts + mesh.QuadFaceArray[X*2];
                this.QuadFaceArray[this.Number_of_QuadFaces*8 + X*2 + 1] = this.Number_of_vtcords + mesh.QuadFaceArray[X*2 + 1];
            }
        }
        else{
            for (int X = 0; X < mesh.Number_of_TriFaces*3;X++) {
                this.TriFaceArray[this.Number_of_TriFaces*3 + X*2] = this.Number_of_Verts + mesh.TriFaceArray[X*2];
                this.TriFaceArray[this.Number_of_TriFaces*3 + X*2 + 1] = this.Number_of_vtcords + mesh.TriFaceArray[X*2 + 1];
            }
            for(int X=0;X<mesh.Number_of_QuadFaces*4;X++){
                this.QuadFaceArray[this.Number_of_QuadFaces*4 + X*2] = this.Number_of_Verts + mesh.QuadFaceArray[X*2];
                this.QuadFaceArray[this.Number_of_QuadFaces*4 + X*2 + 1] = this.Number_of_vtcords + mesh.QuadFaceArray[X*2 + 1];
            }
        }


        this.Number_of_Verts += mesh.Number_of_Verts;
        this.Number_of_vtcords += mesh.Number_of_vtcords;
        this.Number_of_TriFaces += mesh.Number_of_TriFaces;
        this.Number_of_QuadFaces += mesh.Number_of_QuadFaces;
    }
    public void upload_Vertex_data(){
        float [] VertexArray = new float[this.Number_of_TriFaces * 3 * 8 + this.Number_of_QuadFaces*4*8];
        int vert_count = 0;


        if(this.has_triangles) {
            for (int F = 0; F < this.Number_of_TriFaces; F++) {
                for (int V = 0; V < 3; V++) {
                    int VPos = vert_count * 8;
                    if (this.has_tex) {
                        VertexArray[VPos] = this.Original_VertexArray[this.TriFaceArray[F * 6 + V * 2] * 3 ];
                        VertexArray[VPos + 1] = this.Original_VertexArray[this.TriFaceArray[F * 6 + V * 2] * 3 + 1];
                        VertexArray[VPos + 2] = this.Original_VertexArray[this.TriFaceArray[F * 6 + V * 2] * 3 + 2];
                        VertexArray[VPos + 3] = this.VTcords_array[this.TriFaceArray[F * 6 + V * 2 + 1] * 2];
                        VertexArray[VPos + 4] = 1 - this.VTcords_array[this.TriFaceArray[F * 6 + V * 2 + 1] * 2 + 1];
                    } else {
                        VertexArray[VPos] = this.Original_VertexArray[this.TriFaceArray[F * 3 + V] * 3];
                        VertexArray[VPos + 1] = this.Original_VertexArray[this.TriFaceArray[F * 3 + V] * 3 + 1];
                        VertexArray[VPos + 2] = this.Original_VertexArray[this.TriFaceArray[F * 3 + V] * 3 + 2];
                        VertexArray[VPos + 3] = .5f;
                        VertexArray[VPos + 4] = .5f;
                    }
                    if(FullLight){
                        VertexArray[VPos + 5] = 1.0f;
                        VertexArray[VPos + 6] = 1.0f;
                        VertexArray[VPos + 7] = 1.0f;
                    }
                    else {
                        Vec4 light = Light.getLight(new Vec3(VertexArray[VPos]+this.position.X,
                                VertexArray[VPos + 1]+this.position.Y,
                                VertexArray[VPos + 2]+this.position.Z));
                        VertexArray[VPos + 5] = light.X*light.W;
                        VertexArray[VPos + 6] = light.Y*light.W;
                        VertexArray[VPos + 7] = light.Z*light.W;
                    }
                    vert_count++;
                }
            }
        }
        //vert_count = 0;
        if(this.has_quads){
            for(int F=0;F<this.Number_of_QuadFaces;F++){
                for(int V=0;V<4;V++){
                    int VPos = vert_count * 8;
                    if(this.has_tex){
                        VertexArray[VPos] = this.Original_VertexArray[this.QuadFaceArray[F * 8 + V * 2] * 3];
                        VertexArray[VPos + 1] = this.Original_VertexArray[this.QuadFaceArray[F * 8 + V * 2] * 3 + 1];
                        VertexArray[VPos + 2] = this.Original_VertexArray[this.QuadFaceArray[F * 8 + V * 2] * 3 + 2];
                        VertexArray[VPos + 3] = this.VTcords_array[this.QuadFaceArray[F * 8 + V * 2 + 1] * 2];
                        VertexArray[VPos + 4] = 1-this.VTcords_array[this.QuadFaceArray[F * 8 + V * 2 + 1] * 2 + 1];
                    }
                    else{
                        VertexArray[VPos] = this.Original_VertexArray[this.QuadFaceArray[F * 4 + V] * 3];
                        VertexArray[VPos + 1] = this.Original_VertexArray[this.QuadFaceArray[F * 4 + V] * 3 + 1];
                        VertexArray[VPos + 2] = this.Original_VertexArray[this.QuadFaceArray[F * 4 + V] * 3 + 2];
                        VertexArray[VPos + 3] = .5f;
                        VertexArray[VPos + 4] = .5f;
                    }
                    if(FullLight){
                        VertexArray[VPos + 5] = 1.0f;
                        VertexArray[VPos + 6] = 1.0f;
                        VertexArray[VPos + 7] = 1.0f;
                    }
                    else {
                        Vec4 light = Light.getLight(new Vec3(VertexArray[VPos]+this.position.X,
                                VertexArray[VPos + 1]+this.position.Y,
                                VertexArray[VPos + 2]+this.position.Z));
                        VertexArray[VPos + 5] = light.X*light.W;
                        VertexArray[VPos + 6] = light.Y*light.W;
                        VertexArray[VPos + 7] = light.Z*light.W;
                    }
                    vert_count++;
                }
            }
        }

        if(Thread.currentThread().getName().equals("main")){
            glBindBuffer(GL_ARRAY_BUFFER, this.VertexBufferObject);
            glBufferData(GL_ARRAY_BUFFER, VertexArray, GL_STATIC_DRAW);
        }
        else{

            while(Main.glBufferDataStatus != Main.GLStatus.Done){
                try {
                    Thread.sleep(1);
                }
                catch(Exception e1){
                    e1.printStackTrace();
                    return;
                }
            }
            Main.glBindBuffer_In1 = GL_ARRAY_BUFFER;
            Main.glBindBuffer_In2 = this.VertexBufferObject;

            Main.glBufferData_In1 = GL_ARRAY_BUFFER;
            Main.glBufferData_In2 = VertexArray;
            Main.glBufferData_In3 = GL_STATIC_DRAW;
            Main.glBufferDataStatus = Main.GLStatus.Ready;
            while(Main.glBufferDataStatus != Main.GLStatus.Done){
                try {
                    Thread.sleep(1);
                }
                catch(Exception e1){
                    e1.printStackTrace();
                    return;
                }
            }
        }
        status = MeshStatus.Completed;
    }
    public void setColor(int color){
        glBindTexture(GL_TEXTURE_2D, this.Texture_Buffer_Object);
        int [] Texture_data = new int[1];
        Texture_data[0] = color;
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1,1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, Texture_data);
    }
    public void Scale(float X,float Y,float Z){
        for(int V=0;V<this.Number_of_Verts;V++){
            this.Original_VertexArray[V*3] *= X;
            this.Original_VertexArray[V*3 + 1] *= Y;
            this.Original_VertexArray[V*3 + 2] *= Z;
        }
        upload_Vertex_data();
    }
    public void setIs_skyBox(){
        this.is_skyBox = true;
        this.FullLight = true;
        upload_Vertex_data();
    }
    public void setWireFrame(){
        this.FullLight = true;
        upload_Vertex_data();
    }
    public void draw(){
        if(this.is_skyBox){
            float [] mat = new float[4*4];
            mat[0]=1;mat[5]=1;mat[10]=1;mat[15]=1;
            glUniformMatrix4fv(Main.shader_translation_position, false, mat);
            glUniformMatrix4fv(Main.shader_rotation_l_position, false, mat);
        }
        else {
            float[] mat = new float[4 * 4];
            mat[0] = 1;
            mat[4] = 0;
            mat[8] = 0;
            mat[1] = 0;
            mat[5] = 1;
            mat[9] = 0;
            mat[2] = 0;
            mat[6] = 0;
            mat[10] = 1;
            mat[3] = 0;
            mat[7] = 0;
            mat[11] = 0;

            mat[12] = this.position.X - Player.position.X;
            mat[13] = this.position.Y - Player.position.Y;
            mat[14] = this.position.Z - Player.position.Z;
            mat[15] = 1;

            glUniformMatrix4fv(Main.shader_translation_position, false, mat);

            Math3D.Make3DRotationMatrix44(this.XRot, this.YRot, this.ZRot, mat);
            glUniformMatrix4fv(Main.shader_rotation_l_position, false, mat);
        }

        glBindBuffer(GL_ARRAY_BUFFER, this.VertexBufferObject);
        glBindTexture(GL_TEXTURE_2D, this.Texture_Buffer_Object);

        glVertexAttribPointer(Main.shader_vertex_position, 3, GL_FLOAT, false, 8 * 4, 0);
        glVertexAttribPointer(Main.shader_vtcord_position, 2, GL_FLOAT, false, 8 * 4, 3 * 4);
        glVertexAttribPointer(Main.shader_light_position, 3, GL_FLOAT, false, 8 * 4, 3*4 + 2*4);

        if(this.is_skyBox){glDisable(GL_DEPTH_TEST);}
        glDrawArrays(GL_TRIANGLES, 0, this.Number_of_TriFaces*3);
        glDrawArrays(GL_QUADS, this.Number_of_TriFaces*3, this.Number_of_QuadFaces*4);
        if(this.is_skyBox){glEnable(GL_DEPTH_TEST);}
    }
    public void Remove(){
        this.Number_of_Verts = 0;
        this.Number_of_vtcords = 0;
        this.Number_of_TriFaces = 0;
        this.Number_of_QuadFaces = 0;

        this.Original_VertexArray = new float[0];
        this.VTcords_array = new float[0];
        this.TriFaceArray = new int[0];
        this.QuadFaceArray = new int[0];

        //upload_Vertex_data();


        if(Thread.currentThread().getName().equals("main")){
            glDeleteBuffers(this.VertexBufferObject);
            if (TBO_gen) {
                glDeleteBuffers(this.Texture_Buffer_Object);
            }
        }
        else{
            while(Main.glDeleteBuffersStatus != Main.GLStatus.Done){
                try {
                    Thread.sleep(1);
                }
                catch(Exception e1){
                    e1.printStackTrace();
                    return;
                }
            }
            Main.glDeleteBuffers_In1 = this.VertexBufferObject;
            Main.glDeleteBuffersStatus = Main.GLStatus.Ready;
            if (TBO_gen) {
                while (Main.glDeleteBuffersStatus != Main.GLStatus.Done) {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        return;
                    }
                }
                Main.glDeleteBuffers_In1 = this.Texture_Buffer_Object;
                Main.glDeleteBuffersStatus = Main.GLStatus.Ready;
            }
        }

    }


}

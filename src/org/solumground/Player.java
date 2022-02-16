package org.solumground;

import java.io.*;
import java.nio.*;
import java.lang.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL21.*;

public class Player{
    public static Vec3 position;
    public static Vec3 reSpawnPosition;
    public static Vec3 StandingInChunk;
    public static float XRot;
    public static float YRot;
    public static float ZRot;
    public static float [] RotationMatrix;
    public static boolean isOnGround() {return collisionBox.On_Ground();}
    public static boolean is_flying;
    public static boolean is_jumping;
    public static float fall_time;
    public static float jump_time;

    public static int looking_at_X;
    public static int looking_at_Y;
    public static int looking_at_Z;
    public static Mesh wireframe;
    public static int hotbar_selected;
    public static CollisionBox collisionBox;
    public static Chunk[] chunks = new Chunk[11];
    public static boolean ChunkReload = false;


    public static void Init(){

        reSpawnPosition = new Vec3(0,40,0);
        position = new Vec3(reSpawnPosition);
        StandingInChunk = Chunk.convert_to_chunk_pos(position);

        XRot = 0;
        YRot = 0;
        ZRot = 0;
        hotbar_selected = 0;

        String DataPath = Main.SaveFolder+"/Player.dat";
        File file = new File(DataPath);
        int bytesRead = 0;
        if(file.exists()){
            byte[] data = new byte[28];
            try {
                FileInputStream InStream = new FileInputStream(file);
                bytesRead = InStream.read(data, 0, 28);
                InStream.close();
            }
            catch(Exception e){
                System.out.println("Failed to load Player.dat");
                e.printStackTrace();
            }

            try {
                if(bytesRead == 28) {
                    ByteBuffer bb = ByteBuffer.wrap(data);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    position.X = bb.getFloat(0);
                    position.Y = bb.getFloat(4);
                    position.Z = bb.getFloat(8);
                    XRot = bb.getFloat(12);
                    YRot = bb.getFloat(16);
                    ZRot = bb.getFloat(20);
                    hotbar_selected = bb.getInt(24);
                }
            }
            catch(Exception e){
                System.out.println("Error Loading Player Data");
                e.printStackTrace();
            }
        }

        collisionBox = new CollisionBox(position, .33f,.1f,.33f, -.33f,-1.8f,-.33f);
        collisionBox.set_Is_player(true);

        RotationMatrix = new float[4*4];
        Math3D.Make3DRotationMatrix44(XRot, YRot, ZRot, RotationMatrix);

        is_jumping = false;
        is_flying = false;

        fall_time = (float)glfwGetTime();



        wireframe = new Mesh(Main.jar_folder_path+"/assets/solumground/models/wireFrame.smobj", "");
        wireframe.setWireFrame();
        wireframe.Scale(.504f,.504f,.504f);
        wireframe.setColor(0x0000FFFF);

        set_Projection();

    }
    public static void set_Projection(){
        float [] projection_mat = new float[4*4];

        projection_mat[0] = ((float)Main.win_Y/(float)Main.win_X) / (float)Math.tan(Main.FOV*.5*3.1415/180);
        projection_mat[5] = 1 / (float)Math.tan(Main.FOV*.5*3.1415/180);
        projection_mat[10] = ((Main.farPlane+Main.nearPlane) / (Main.nearPlane-Main.farPlane));
        projection_mat[11] = -1;
        projection_mat[14] = ((Main.farPlane*Main.nearPlane) / (Main.nearPlane-Main.farPlane))*2;

        glUniformMatrix4fv(Main.shader_projection_position, false, projection_mat);
    }

    public static void update(){
        Math3D.Make3DRotationMatrix44(XRot, YRot, ZRot, RotationMatrix);
        glUniformMatrix4fv(Main.shader_rotation_g_position, false, RotationMatrix);

        collisionBox.active_update();

        for(int Z=0;Z>-6;Z--){
            Vec3 pos = Math3D.Vec3X44MatrixMultiply(new Vec3(0,0,Z), RotationMatrix);
            looking_at_X = Math.round(pos.X+position.X);
            looking_at_Y = Math.round(pos.Y+position.Y);
            looking_at_Z = Math.round(pos.Z+position.Z);
            
            Chunk lookingChunk = Chunk.FromPos(new Vec3(looking_at_X,looking_at_Y,looking_at_Z));
            if(lookingChunk != null){
                int cX = (int)(looking_at_X-lookingChunk.position.X);
                int cY = (int)(looking_at_Y-lookingChunk.position.Y);
                int cZ = (int)(looking_at_Z-lookingChunk.position.Z);
                

                if (lookingChunk.Get(cX,cY,cZ) != 0) {
                    break;
                }

            }
        }

        wireframe.position.X = looking_at_X;
        wireframe.position.Y = looking_at_Y;
        wireframe.position.Z = looking_at_Z;
    }

    public static void place_block(){
        Vec3 pos = new Vec3(looking_at_X,looking_at_Y,looking_at_Z);
        Chunk selected_chunk = Chunk.FromPos(pos);
        if(selected_chunk == null){
            Vec3 CP = Chunk.convert_to_chunk_pos(pos);
            selected_chunk = new Chunk(Main.SaveFolder, (int)CP.X,(int)CP.Y,(int)CP.Z);
        }
        selected_chunk.Place(hotbar_selected, pos);


    }

    public static void break_block(){
        Vec3 pos = new Vec3(looking_at_X,looking_at_Y,looking_at_Z);
        Chunk selected_chunk = Chunk.FromPos(pos);
        if(selected_chunk != null){
            selected_chunk.Delete(pos);
        }
    }

    public static void move_hotbar(int dist){
        hotbar_selected += dist;
        while(hotbar_selected > 1){hotbar_selected -= Block.Number;}
        while(hotbar_selected < 0){hotbar_selected += Block.Number;}
    }

    public static void Move_X(float distance){
        position.X -= (float)Math.cos(YRot*3.1415/180)*distance;
        position.Z += (float)Math.sin(YRot*3.1415/180)*distance;
    }
    public static void Move_Y(float distance){
        position.Y -= distance;
    }
    public static void Move_Z(float distance){
        position.X -= (float)Math.sin(YRot*3.1415/180)*distance;
        position.Z -= (float)Math.cos(YRot*3.1415/180)*distance;
    }

    public static void Rotate_Y(float distance){
        YRot += distance;
    }
    public static void Rotate_X(float distance){
        if(XRot + distance < 90 && XRot + distance > -90) {
            XRot += distance;
        }
        else{
            if(distance > 0){XRot = 90;}
            if(distance < 0){XRot = -90;}
        }
    }
    public static void applyGravity(){
        float TimeElapsed = Main.TimeElapsed;
        if(TimeElapsed > 0.3f){TimeElapsed = 0.3f;}
        if(!isOnGround() && !is_flying){
            float time = (float)glfwGetTime() - fall_time;
            if(time > 3f){time = 3f;}
            float dist = (9.8f*time)*TimeElapsed;
            position.Y -= dist;
        }

        else{
            fall_time = (float)glfwGetTime();
        }


        if(is_jumping){
            float time = (float)glfwGetTime() - jump_time;
            float a = 1f-time;
            if(a > 0) {

                position.Y += a * 15f * TimeElapsed;

            }
            else{
                is_jumping = false;
            }
        }
        if(position.Y < -100){
            reSpawn();
        }

    }
    public static void jump(){
        if(!is_jumping && isOnGround()) {
            is_jumping = true;
            jump_time = (float) glfwGetTime();
        }
    }
    public static void reSpawn(){
        fall_time = (float)glfwGetTime();
        position.X = reSpawnPosition.X;
        position.Y = reSpawnPosition.Y;
        position.Z = reSpawnPosition.Z;
        XRot = 0;
        YRot = 0;
        ZRot = 0;
        update();
    }
    public static void Draw(){
        wireframe.draw();
        if(Main.showCollisionBox) {
            collisionBox.draw();
        }
    }

    public static void Save(){
        String s = Main.SaveFolder+"/Player.dat";

        byte[] data = new byte[28];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(0, position.X);
        bb.putFloat(4, position.Y);
        bb.putFloat(8, position.Z);
        bb.putFloat(12, XRot);
        bb.putFloat(16, YRot);
        bb.putFloat(20, ZRot);
        bb.putInt(24, hotbar_selected);

        try{
            FileOutputStream outputStream = new FileOutputStream(s);
            outputStream.write(data, 0, 28);
            outputStream.close();
        }
        catch(Exception e){
            System.out.println("Error Saving Player.dat");
            e.printStackTrace();
        }


    }
}

package org.solumground;

import java.io.*;
import java.nio.*;
import java.lang.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL21.*;

public class Player{
    public static Vec3 position;
    public static Vec3 reSpawnPosition;
    public static IVec3 StandingInChunk;
    public static Vec3 Rotation;
    public static float [] WorldMatrix;
    public static boolean isOnGround() {return collisionBox.On_Ground();}
    public static boolean is_flying;
    public static boolean is_jumping;
    public static float fall_time;
    public static float jump_time;

    public static IVec3 LookingAt;
    public static Block LookingAtBlock;
    public static Mesh wireframe;
    public static int hotbar_selected;
    public static CollisionBox collisionBox;
    public static Mesh AnimationMesh;
    public static boolean AnimationBreak = false;
    public static boolean AnimationPlace = false;
    public static float AnimationStartTime;
    public static IVec3 AnimationPos;
    public static boolean ChunkReload = false;


    public static void Init() {
        reSpawnPosition = new Vec3(0, 64, 0);
        position = new Vec3(reSpawnPosition);
        StandingInChunk = Chunk.convert_to_chunk_pos(position);

        Rotation = new Vec3();
        LookingAt = new IVec3();
        hotbar_selected = 0;

        collisionBox = new CollisionBox(position, .33f, .1f, .33f, -.33f, -1.8f, -.33f);
        collisionBox.set_Is_player(true);


        WorldMatrix = new float[4*4];
        Math3D.Make3DRotationMatrix44(Rotation, WorldMatrix);

        is_jumping = false;
        is_flying = false;

        fall_time = (float)glfwGetTime();



        wireframe = new Mesh(Main.jar_folder_path+"/assets/solumground/models/wireFrame.smobj", "");
        wireframe.setWireFrame();
        wireframe.ScaleData(.504f,.504f,.504f);
        wireframe.setColor(0x0000FFFF);

        set_Projection();
    }
    public static void Load(){
        String DataPath = Main.jar_folder_path+"/"+Main.SaveFolder+"/Player.dat";
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
                    Rotation.X = bb.getFloat(12);
                    Rotation.Y = bb.getFloat(16);
                    Rotation.Z = bb.getFloat(20);
                    hotbar_selected = bb.getInt(24);
                }
            }
            catch(Exception e){
                System.out.println("Error Loading Player Data");
                e.printStackTrace();
            }
        }
        fall_time = (float)glfwGetTime();
    }
    public static void set_Projection(){
        float [] projection_mat = new float[4*4];

        projection_mat[0] = ((float)Main.win_Y/(float)Main.win_X) / (float)Math.tan(Main.FOV*.5*3.1415/180);
        projection_mat[5] = 1 / (float)Math.tan(Main.FOV*.5*3.1415/180);
        projection_mat[10] = ((Main.farPlane+Main.nearPlane) / (Main.nearPlane-Main.farPlane));
        projection_mat[11] = -1;
        projection_mat[14] = ((Main.farPlane*Main.nearPlane) / (Main.nearPlane-Main.farPlane))*2;

        glUniformMatrix4fv(Main.MainShader_Projection, false, projection_mat);
    }

    public static void update(){
        if(Main.showCollisionBox){
            float [] RotationMat = new float[16];
            Math3D.Make3DRotationMatrix44(Rotation, RotationMat);
            float [] TransMat = new float[16];
            TransMat[0] = 1;
            TransMat[5] = 1;
            TransMat[10] = 1;
            TransMat[12] = -position.X;
            TransMat[13] = -position.Y;
            TransMat[14] = -position.Z;
            TransMat[15] = 1;
            Math3D.Matrix44_Multiply(TransMat,RotationMat, WorldMatrix);
            glUniformMatrix4fv(Main.MainShader_WorldMat, false, WorldMatrix);
        }
        collisionBox.active_update();

        float [] RotationMat = new float[16];
        Math3D.Make3DRotationMatrix44(Rotation, RotationMat);
        float [] TransMat = new float[16];
        TransMat[0] = 1;
        TransMat[5] = 1;
        TransMat[10] = 1;
        TransMat[12] = -position.X;
        TransMat[13] = -position.Y;
        TransMat[14] = -position.Z;
        TransMat[15] = 1;
        Math3D.Matrix44_Multiply(TransMat,RotationMat, WorldMatrix);
        glUniformMatrix4fv(Main.MainShader_WorldMat, false, WorldMatrix);


        for(int Z=0;Z>-6;Z--){
            Vec3 pos = Math3D.Vec3X44MatrixMultiply(new Vec3(0,0,Z), RotationMat);
            LookingAt.X = Math.round(pos.X+position.X);
            LookingAt.Y = Math.round(pos.Y+position.Y);
            LookingAt.Z = Math.round(pos.Z+position.Z);
            
            Chunk lookingChunk = Chunk.FromPos(new Vec3(LookingAt));
            if(lookingChunk != null){
                LookingAtBlock = Block.Blocks[lookingChunk.GetGlobal(LookingAt.ToFloat())];
                if (LookingAtBlock.ID != 0) {
                    break;
                }
            }
            else{
                LookingAtBlock = Block.Blocks[0];
            }
        }

        wireframe.position = new Vec3(LookingAt);
    }
    public static void StartAnimation(Block block){
        if(AnimationMesh == null){
            AnimationMesh = new Mesh(Block.TextureBufferObject);
        }
        AnimationMesh.Number_of_TriFaces = 0;
        AnimationMesh.Number_of_QuadFaces = 0;
        AnimationMesh.Number_of_Verts = 0;
        AnimationMesh.Number_of_vtcords = 0;
        AnimationMesh.Roation = new Vec3(0,0,0);

        if(block.Full){
            for(int X=0;X<6;X++) {
                Mesh mesh = block.Sides[X];
                mesh.position = new Vec3(0,0,0);
                AnimationMesh.add(mesh);
            }
        }
        else{
            Mesh mesh = block.mesh;
            mesh.position = new Vec3(0,0,0);
            AnimationMesh.add(mesh);
        }

        AnimationStartTime = Main.Time;
        AnimationPos = new IVec3(LookingAt);
        AnimationMesh.position = AnimationPos.ToFloat();

        AnimationMesh.upload_Vertex_data();
    }
    public static void place_block(){
        if(LookingAtBlock.ID == 0 && Block.Blocks[hotbar_selected].ID != 0) {
            Chunk selected_chunk = Chunk.FromPos(LookingAt.ToFloat());
            if (selected_chunk == null) {
                return;
            }
            boolean Placed = false;
            Placed = selected_chunk.Place(hotbar_selected, LookingAt);
            if(Placed){
                StartAnimation(Block.Blocks[hotbar_selected]);
                AnimationPlace = true;
            }
        }
    }
    public static void break_block(){
        Chunk selected_chunk = Chunk.FromPos(LookingAt.ToFloat());
        boolean Broke = false;
        if(selected_chunk != null){
            Broke = selected_chunk.Delete(LookingAt);
        }
        if(Broke){
            StartAnimation(LookingAtBlock);
            AnimationBreak = true;
        }
    }

    public static void move_hotbar(int dist){
        hotbar_selected += dist;
        while(hotbar_selected > 1){hotbar_selected -= Block.Number;}
        while(hotbar_selected < 0){hotbar_selected += Block.Number;}
    }

    public static void Move_X(float distance){
        position.X -= (float)Math.cos(Rotation.Y*3.1415/180)*distance;
        position.Z += (float)Math.sin(Rotation.Y*3.1415/180)*distance;
    }
    public static void Move_Y(float distance){
        position.Y += distance;
    }
    public static void Move_Z(float distance){
        position.X -= (float)Math.sin(Rotation.Y*3.1415/180)*distance;
        position.Z -= (float)Math.cos(Rotation.Y*3.1415/180)*distance;
    }
    public static void Rotate_Y(float distance){
        Rotation.Y += distance;
    }
    public static void Rotate_X(float distance){
        if(Rotation.X + distance < 90 && Rotation.X + distance > -90) {
            Rotation.X += distance;
        }
        else{
            if(distance > 0){Rotation.X = 90;}
            if(distance < 0){Rotation.X = -90;}
        }
    }
    public static void applyGravity(){
        float TimeElapsed = Main.TimeElapsed;
        if(TimeElapsed > 0.3f){TimeElapsed = 0.3f;}
        if(!isOnGround() && !is_flying){
            float time = (float)glfwGetTime() - fall_time;
            if(time > 3f){time = 3f;}
            float dist = (9.8f*time)*TimeElapsed;
            Move_Y(-dist);
        }

        else{
            fall_time = (float)glfwGetTime();
        }


        if(is_jumping){
            float time = (float)glfwGetTime() - jump_time;
            float a = 1f-time;
            if(a > 0) {

                Move_Y(a * 15f * TimeElapsed);

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
        update();
    }
    public static void Draw(){
        wireframe.draw();
        if(Main.showCollisionBox) {
            collisionBox.draw();
        }
        if(AnimationBreak){
            float LerpValue = (Main.Time-AnimationStartTime)/0.5f;
            if(LerpValue < 0.2f){
                float Scale = 1-(LerpValue/0.2f)*0.7f;
                AnimationMesh.Scale.X = Scale;
                AnimationMesh.Scale.Y = Scale;
                AnimationMesh.Scale.Z = Scale;
            }
            if(LerpValue < 1) {
                AnimationMesh.position.X = AnimationPos.X*(1-LerpValue) + position.X*LerpValue;
                AnimationMesh.position.Y = AnimationPos.Y*(1-LerpValue) + (position.Y-1)*LerpValue;
                AnimationMesh.position.Z = AnimationPos.Z*(1-LerpValue) + position.Z*LerpValue;

                AnimationMesh.Roation.X += 30*Main.TimeElapsed;
                AnimationMesh.Roation.Y += 60*Main.TimeElapsed;
                AnimationMesh.Roation.Z += 20*Main.TimeElapsed;
            }
            else{
                AnimationBreak = false;
            }
            AnimationMesh.draw();
        }
        if(AnimationPlace){
            float LerpValue = (Main.Time-AnimationStartTime)/0.5f;

            if(LerpValue < 0.8f){
                AnimationMesh.Roation.X += 30*Main.TimeElapsed;
                AnimationMesh.Roation.Y += 60*Main.TimeElapsed;
                AnimationMesh.Roation.Z += 20*Main.TimeElapsed;
            }
            if(LerpValue > 0.8f){
                float LocalLerp = (LerpValue-0.8f)*5;
                float Scale = 0.3f* (1-LocalLerp) + 1*LocalLerp;
                AnimationMesh.Scale.X = Scale;
                AnimationMesh.Scale.Y = Scale;
                AnimationMesh.Scale.Z = Scale;
                AnimationMesh.Roation.X = AnimationMesh.Roation.X*(1-LocalLerp);
                AnimationMesh.Roation.Y = AnimationMesh.Roation.Y*(1-LocalLerp);
                AnimationMesh.Roation.Z = AnimationMesh.Roation.Z*(1-LocalLerp);
            }
            if(LerpValue < 1) {
                AnimationMesh.position.X = AnimationPos.X*LerpValue + position.X*(1-LerpValue);
                AnimationMesh.position.Y = AnimationPos.Y*LerpValue + (position.Y-1)*(1-LerpValue);
                AnimationMesh.position.Z = AnimationPos.Z*LerpValue + position.Z*(1-LerpValue);
            }
            else{
                Chunk selected_chunk = Chunk.FromPos(AnimationPos.ToFloat());
                selected_chunk.ReBuildMesh();
                AnimationPlace = false;
            }
            AnimationMesh.draw();
        }
    }

    public static void Save(){
        String s = Main.jar_folder_path+"/"+Main.SaveFolder+"/Player.dat";

        byte[] data = new byte[28];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(0, position.X);
        bb.putFloat(4, position.Y);
        bb.putFloat(8, position.Z);
        bb.putFloat(12, Rotation.X);
        bb.putFloat(16, Rotation.Y);
        bb.putFloat(20, Rotation.Z);
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

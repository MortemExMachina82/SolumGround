package org.solumground;

import java.io.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;


public class DaemonChunkLoader extends Thread{
    public boolean ShouldClose = false;
    public Chunk [] NeedsMesh = new Chunk[2000];
    public int NeedsMeshCount = 0;
    public MeshBuilder meshBuilder;

    public void close(){
        ShouldClose = true;
        meshBuilder.shouldClose = true;
    }
    public DaemonChunkLoader(){

    }

    public void run(){
        System.out.println("Starting Chunkloader");
        meshBuilder = new MeshBuilder();
        meshBuilder.start();

        while(!ShouldClose) {
            try{
                Thread.sleep(1);
            }
            catch(Exception e){}
            Vec3 CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
            if (Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk) == false | Player.ChunkReload) {
                Player.StandingInChunk = CurentStandingInChunk;
                loadAroundPlayer(Main.RenderDistance);
                Player.ChunkReload = false;
            }
        }
    }
    public void loadAroundPlayer(int RenderDist){
        Vec3 pos;
        Chunk chunk;
        for(int R=0;R<RenderDist+1;R++){
            if(ShouldClose){break;}
            Vec3 CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
            if (Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk) == false){
                Player.StandingInChunk = CurentStandingInChunk;
                R = 0;
                Player.ChunkReload = true;
            }
            if(R==0){
                pos = new Vec3(Player.StandingInChunk);
                chunk = Chunk.FromChunkPos(pos);
                if(chunk == null) {
                    chunk = new Chunk(Main.SaveFolder, (int)pos.X, (int)pos.Y, (int)pos.Z);
                    NeedsMesh[NeedsMeshCount] = chunk;
                    NeedsMeshCount++;
                }
                continue;
            }
            int RY = (int)(R*.5f);
            if(RY < 1){RY = 1;}
            for(int X=(-R);X<R+1;X++){
                if(ShouldClose){break;}
                CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
                if (Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk) == false){
                    Player.StandingInChunk = CurentStandingInChunk;
                    R = -1;
                    Player.ChunkReload = true;
                    break;
                }
                for(int Z=(-R);Z<R+1;Z++){
                    pos = new Vec3(Player.StandingInChunk);
                    pos.X += X;
                    pos.Y += RY;
                    pos.Z += Z;
                    chunk = Chunk.FromChunkPos(pos);
                    if(chunk == null) {
                        chunk = new Chunk(Main.SaveFolder, (int)pos.X, (int)pos.Y, (int)pos.Z);
                        NeedsMesh[NeedsMeshCount] = chunk;
                        NeedsMeshCount++;
                    }
                    pos.Y -= RY+RY;
                    chunk = Chunk.FromChunkPos(pos);
                    if(chunk == null) {
                        chunk = new Chunk(Main.SaveFolder, (int)pos.X, (int)pos.Y, (int)pos.Z);
                        NeedsMesh[NeedsMeshCount] = chunk;
                        NeedsMeshCount++;
                    }
                }
            }
            for(int Y=(-RY)+1;Y<RY;Y++){
                if(ShouldClose){break;}
                CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
                if (Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk) == false){
                    Player.StandingInChunk = CurentStandingInChunk;
                    R = -1;
                    Player.ChunkReload = true;
                    break;
                }
                for(int Z=(-R)+1;Z<R;Z++){
                    pos = new Vec3(Player.StandingInChunk);
                    pos.X += R;
                    pos.Y += Y;
                    pos.Z += Z;
                    chunk = Chunk.FromChunkPos(pos);
                    if(chunk == null) {
                        chunk = new Chunk(Main.SaveFolder, (int)pos.X, (int)pos.Y, (int)pos.Z);
                        NeedsMesh[NeedsMeshCount] = chunk;
                        NeedsMeshCount++;
                    }
                    pos.X -= R+R;
                    chunk = Chunk.FromChunkPos(pos);
                    if(chunk == null) {
                        chunk = new Chunk(Main.SaveFolder, (int)pos.X, (int)pos.Y, (int)pos.Z);
                        NeedsMesh[NeedsMeshCount] = chunk;
                        NeedsMeshCount++;
                    }
                }
            }
            for(int X=(-R);X<R+1;X++) {
                if(ShouldClose){break;}
                CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
                if (Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk) == false){
                    Player.StandingInChunk = CurentStandingInChunk;
                    R = -1;
                    Player.ChunkReload = true;
                    break;
                }
                for(int Y=(-RY)+1;Y<RY;Y++){
                    pos = new Vec3(Player.StandingInChunk);
                    pos.X += X;
                    pos.Y += Y;
                    pos.Z += R;
                    chunk = Chunk.FromChunkPos(pos);
                    if (chunk == null) {
                        chunk = new Chunk(Main.SaveFolder, (int) pos.X, (int) pos.Y, (int) pos.Z);
                        NeedsMesh[NeedsMeshCount] = chunk;
                        NeedsMeshCount++;
                    }
                    pos.Z -= R+R;
                    chunk = Chunk.FromChunkPos(pos);
                    if (chunk == null) {
                        chunk = new Chunk(Main.SaveFolder, (int) pos.X, (int) pos.Y, (int) pos.Z);
                        NeedsMesh[NeedsMeshCount] = chunk;
                        NeedsMeshCount++;
                    }
                }
            }

        }
    }

    class MeshBuilder extends Thread{
        public boolean shouldClose = false;
        public void run(){
            System.out.println("Starting Mesh Builder");
            while(!shouldClose){
                int NMC = NeedsMeshCount;
                if(NMC == 0){continue;}
                Chunk chunk = NeedsMesh[NMC-1];
                if(chunk != null){
                    chunk.buildMesh();
                }
                NeedsMesh[NMC-1] = null;
                if(NeedsMeshCount == NMC){
                    NeedsMeshCount--;
                }
            }
        }
    }
}
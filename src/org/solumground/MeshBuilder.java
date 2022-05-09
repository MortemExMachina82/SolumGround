package org.solumground;

import java.util.ArrayList;
import java.util.List;

public class MeshBuilder extends Thread{
    public static List<Chunk> Genbuffer = new ArrayList<>(100);
    public static List<Chunk> ReGenBuffer = new ArrayList<>(10);
    public static List<Chunk> LightUpdateBuffer = new ArrayList<>(10);

    public boolean shouldClose = false;
    public void kill(){
        shouldClose = true;
    }
    public void Build(){
        Chunk chunk;
        if(Genbuffer.size() > 0) {
            chunk = Genbuffer.remove(0);
            chunk.GetNear();
            chunk.buildMesh();
        }
        if(ReGenBuffer.size() > 0) {
            chunk = ReGenBuffer.remove(0);
            chunk.GetNear();
            chunk.ReBuildMesh();
        }
        if(LightUpdateBuffer.size() > 0) {
            chunk = LightUpdateBuffer.remove(0);
            chunk.LightUpdate();
        }
    }
    public void run(){
        while(!shouldClose){
            try{
                Thread.sleep(1);
            }
            catch(Exception e){
            e.printStackTrace();
            }
            Build();
        }
    }
}
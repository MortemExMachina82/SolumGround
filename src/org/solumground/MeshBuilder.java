package org.solumground;

import java.util.ArrayList;
import java.util.List;

class MeshBuilder extends Thread{
    public static List<Chunk> buffer = new ArrayList<>(1000);

    public boolean shouldClose = false;
    public void close(){
        shouldClose = true;
    }
    public void Build(){
        while(buffer.size() > 0) {
            Chunk chunk = buffer.remove(0);
            chunk.buildMesh();
        }
    }
    public void run(){
        System.out.println("Starting Mesh Builder");
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
package org.solumground;

import java.io.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;

class MeshBuilder extends Thread{
    public static Chunk [] buffer = new Chunk[1000];
    public static int bufferCount = 0;

    public boolean shouldClose = false;
    public void close(){
        shouldClose = false;
    }
    public void Build(){
        while(bufferCount > 0){
            int NMC = bufferCount;
            if(NMC == 0){continue;}
            Chunk chunk = buffer[NMC-1];
            if(chunk != null){
                chunk.buildMesh();
            }
            buffer[NMC-1] = null;
            if(bufferCount == NMC){
                bufferCount--;
            }
        }
    }
    public void run(){
        System.out.println("Starting Mesh Builder");
        while(!shouldClose){
            try{
                Thread.sleep(10);
            }
            catch(Exception e){}
            Build();
        }
    }
}
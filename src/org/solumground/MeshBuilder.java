package org.solumground;

import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class MeshBuilder extends Thread{
    public static List<Chunk> Genbuffer = new ArrayList<>(100);
    public static List<Chunk> ReGenBuffer = new ArrayList<>(10);
    public static List<Chunk> LightUpdateBuffer = new ArrayList<>(10);

    public static int SleepTime = 1;
    public static boolean MAC = false;

    public boolean shouldClose = false;
    public void kill(){
        shouldClose = true;
    }
    public void Build(){
        Chunk chunk;
        if(Genbuffer.size() > 0) {
            chunk = Genbuffer.remove(0);
            if(chunk != null) {
                chunk.GetNear();
                chunk.buildMesh();
            }
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
        if(!Main.PlatformMac) {
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            long SharedContextWindow = glfwCreateWindow(1, 1, "", 0, Main.win);
            glfwMakeContextCurrent(SharedContextWindow);
            GL.createCapabilities();
        }
        while(!shouldClose){
            SleepTime = 100 / (Genbuffer.size()+ReGenBuffer.size()+LightUpdateBuffer.size()+3);
            try{
                Thread.sleep(SleepTime);
                Build();
            }
            catch(Exception e){
            e.printStackTrace();
            }

        }
    }
}
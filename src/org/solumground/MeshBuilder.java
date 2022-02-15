package org.solumground;

class MeshBuilder extends Thread{
    public static int bufferSize = 1000;
    public static Chunk [] buffer = new Chunk[bufferSize];
    public static int bufferCount = 0;

    public boolean shouldClose = false;
    public void close(){
        shouldClose = false;
    }
    public void Build(){
        while(bufferCount > 0){
            int NMC = bufferCount;
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
            catch(Exception e){
                e.printStackTrace();
            }
            Build();
        }
    }
}
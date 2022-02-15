package org.solumground;

public class ChunkLoader extends Thread{
    public boolean ShouldClose = false;

    public void close(){
        ShouldClose = true;
    }
    public ChunkLoader(){
    }

    public void run(){
        System.out.println("Starting Chunkloader");
        while(!ShouldClose) {
            try{
                Thread.sleep(1);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            Vec3 CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
            if (!Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk) | Player.ChunkReload) {
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
            if (!Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk)){
                Player.StandingInChunk = CurentStandingInChunk;
                R = 0;
                Player.ChunkReload = true;
            }
            if(R==0){
                pos = new Vec3(Player.StandingInChunk);
                chunk = Chunk.FromChunkPos(pos);
                if(chunk == null) {
                    chunk = new Chunk(Main.SaveFolder, (int)pos.X, (int)pos.Y, (int)pos.Z);
                    MeshBuilder.buffer[MeshBuilder.bufferCount] = chunk;
                    MeshBuilder.bufferCount++;
                }
                continue;
            }
            int RY = (int)(R*.5f);
            if(RY < 1){RY = 1;}
            for(int X=(-R);X<R+1;X++){
                if(ShouldClose){break;}
                CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
                if (!Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk)){
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
                        MeshBuilder.buffer[MeshBuilder.bufferCount] = chunk;
                        MeshBuilder.bufferCount++;
                    }
                    pos.Y -= RY+RY;
                    chunk = Chunk.FromChunkPos(pos);
                    if(chunk == null) {
                        chunk = new Chunk(Main.SaveFolder, (int)pos.X, (int)pos.Y, (int)pos.Z);
                        MeshBuilder.buffer[MeshBuilder.bufferCount] = chunk;
                        MeshBuilder.bufferCount++;
                    }
                }
            }
            for(int Y=(-RY)+1;Y<RY;Y++){
                if(ShouldClose){break;}
                CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
                if (!Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk)){
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
                        MeshBuilder.buffer[MeshBuilder.bufferCount] = chunk;
                        MeshBuilder.bufferCount++;
                    }
                    pos.X -= R+R;
                    chunk = Chunk.FromChunkPos(pos);
                    if(chunk == null) {
                        chunk = new Chunk(Main.SaveFolder, (int)pos.X, (int)pos.Y, (int)pos.Z);
                        MeshBuilder.buffer[MeshBuilder.bufferCount] = chunk;
                        MeshBuilder.bufferCount++;
                    }
                }
            }
            for(int X=(-R);X<R+1;X++) {
                if(ShouldClose){break;}
                CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
                if (!Vec3.Equal(CurentStandingInChunk, Player.StandingInChunk)){
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
                        MeshBuilder.buffer[MeshBuilder.bufferCount] = chunk;
                        MeshBuilder.bufferCount++;
                    }
                    pos.Z -= R+R;
                    chunk = Chunk.FromChunkPos(pos);
                    if (chunk == null) {
                        chunk = new Chunk(Main.SaveFolder, (int) pos.X, (int) pos.Y, (int) pos.Z);
                        MeshBuilder.buffer[MeshBuilder.bufferCount] = chunk;
                        MeshBuilder.bufferCount++;
                    }
                }
            }

        }
    }
}
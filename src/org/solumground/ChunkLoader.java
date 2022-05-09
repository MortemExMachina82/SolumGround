package org.solumground;

public class ChunkLoader extends Thread{
    public boolean ShouldClose = false;

    public void kill(){
        ShouldClose = true;
    }
    public ChunkLoader(){
    }

    public void run(){
        while(!ShouldClose) {
            try{
                Thread.sleep(1);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            IVec3 CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
            if (!IVec3.Equal(CurentStandingInChunk, Player.StandingInChunk) | Player.ChunkReload) {
                Player.StandingInChunk = CurentStandingInChunk;
                loadAroundPlayer(Main.RenderDistance);
                Player.ChunkReload = false;
            }
        }
    }
    public Chunk makeChunk(IVec3 pos){
        Chunk chunk = Chunk.FromChunkPos(pos);
        if(chunk == null) {
            chunk = new Chunk(Main.jar_folder_path+"/"+Main.SaveFolder, pos);
            if(chunk.status == Chunk.Status.Loaded) {
                MeshBuilder.Genbuffer.add(chunk);
            }
            return chunk;
        }
        return chunk;
    }
    public void loadAroundPlayer(int RenderDist){
        IVec3 pos;
        Chunk chunk;
        for(int R=0;R<RenderDist+1;R++){
            if(ShouldClose){break;}
            IVec3 CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
            if (!IVec3.Equal(CurentStandingInChunk, Player.StandingInChunk)){
                Player.StandingInChunk = CurentStandingInChunk;
                R = 0;
                Player.ChunkReload = true;
            }
            if(R==0){
                chunk = makeChunk(Player.StandingInChunk);
                continue;
            }
            int RY = (int)(R*.5f);
            if(RY < 1){RY = 1;}
            for(int X=(-R);X<R+1;X++){
                if(ShouldClose){break;}
                CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
                if (!IVec3.Equal(CurentStandingInChunk, Player.StandingInChunk)){
                    Player.StandingInChunk = CurentStandingInChunk;
                    R = -1;
                    Player.ChunkReload = true;
                    break;
                }
                for(int Z=(-R);Z<R+1;Z++){
                    pos = new IVec3(Player.StandingInChunk);
                    pos.X += X;
                    pos.Y += RY;
                    pos.Z += Z;
                    chunk = makeChunk(pos);
                    pos.Y -= RY+RY;
                    chunk = makeChunk(pos);
                }
            }
            for(int Y=(-RY)+1;Y<RY;Y++){
                if(ShouldClose){break;}
                CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
                if (!IVec3.Equal(CurentStandingInChunk, Player.StandingInChunk)){
                    Player.StandingInChunk = CurentStandingInChunk;
                    R = -1;
                    Player.ChunkReload = true;
                    break;
                }
                for(int Z=(-R)+1;Z<R;Z++){
                    pos = new IVec3(Player.StandingInChunk);
                    pos.X += R;
                    pos.Y += Y;
                    pos.Z += Z;
                    chunk = makeChunk(pos);
                    pos.X -= R+R;
                    chunk = makeChunk(pos);
                }
            }
            for(int X=(-R);X<R+1;X++) {
                if(ShouldClose){break;}
                CurentStandingInChunk = Chunk.convert_to_chunk_pos(Player.position);
                if (!IVec3.Equal(CurentStandingInChunk, Player.StandingInChunk)){
                    Player.StandingInChunk = CurentStandingInChunk;
                    R = -1;
                    Player.ChunkReload = true;
                    break;
                }
                for(int Y=(-RY)+1;Y<RY;Y++){
                    pos = new IVec3(Player.StandingInChunk);
                    pos.X += X;
                    pos.Y += Y;
                    pos.Z += R;
                    chunk = makeChunk(pos);
                    pos.Z -= R+R;
                    chunk = makeChunk(pos);
                }
            }

        }
    }
}
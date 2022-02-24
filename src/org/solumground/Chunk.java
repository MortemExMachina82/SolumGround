package org.solumground;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Chunk{
    public static int Size = 16;
    public static byte [] noisemap;

    public boolean is_empty;
    public String FilePath;
    private byte [] blocks;
    public IVec3 position;
    public IVec3 chunkPosition;

    public Mesh main_mesh;
    public boolean Exists;

    public Status status = Status.Started;

    enum Status{
        Started,
        Loaded,
        MeshBuilt,
        Complete
    }

    public static void Init(){
        noisemap = PerlinNoise2D.getNoiseMap();
        //noisemap = Noise.MountainGen();
    }
    public Chunk(String Path, IVec3 Position) {
        this.position = new IVec3(Position.X * Size, Position.Y * Size, Position.Z * Size);
        this.chunkPosition = new IVec3(Position);
        this.FilePath = Path + "/world_data_" + (int) this.chunkPosition.X + "_" + (int) this.chunkPosition.Y + "_" + (int) this.chunkPosition.Z + ".dat";


        this.Exists = true;
        for (int X = 0; X < Main.ChunkCount; X++) {
            if (IVec3.Equal(Main.ChunkArray[X].chunkPosition, this.chunkPosition)) {
                this.Exists = false;
                return;
            }
        }
        status = Status.Started;

        blocks = new byte[Size * Size * Size];


        if (Files.exists(Paths.get(this.FilePath))) {
            byte[] data;
            try {
                data = Files.readAllBytes(Paths.get(this.FilePath));
            } catch (Exception e) {
                System.out.println("Failed To Load Chunk Data");
                e.printStackTrace();
                return;
            }
            //System.out.println(Byte.toUnsignedInt(data[0]));
            int blockcount = 0;
            for (int X = 0; X < data.length; X++) {
                if (Byte.toUnsignedInt(data[X]) == 255) {
                    int size = Byte.toUnsignedInt(data[X + 1]);
                    byte blockID = (byte) Byte.toUnsignedInt(data[X + 2]);
                    for (int Y = 0; Y < size; Y++) {
                        blocks[blockcount] = blockID;
                        blockcount++;
                    }
                    X += 2;
                } else {
                    blocks[blockcount] = (byte) Byte.toUnsignedInt(data[X]);
                    blockcount++;
                }
            }
        } else {
            GenChunk();
        }
        status = Status.Loaded;

        this.is_empty = CheckIsEmpty();
        Main.ChunkArray[Main.ChunkCount] = this;
        Main.ChunkCount++;

    }
    public boolean CheckIsEmpty(){
        for (int Y = 0; Y < Size; Y++) {
            for (int X = 0; X < Size; X++) {
                for (int Z = 0; Z < Size; Z++) {
                    int block = GetLocal(new IVec3(X, Y, Z));
                    if (block != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public void buildMesh(){
        if(this.is_empty){
            //System.out.println("Tried to build empty chunk at "+chunkPosition);
            blocks = null;
            return;
        }
        main_mesh = new Mesh(Block.TextureBufferObject);
        main_mesh.position = new Vec3(this.position);

        for(int Y=0;Y<Size;Y++){
            for(int X=0;X<Size;X++){
                for(int Z=0;Z<Size;Z++){
                    int block = GetLocal(new IVec3(X, Y, Z));
                    if(block != 0){
                        Put(block, new IVec3(X, Y, Z));
                    }
                }
            }
        }
        status = Status.MeshBuilt;

        //System.out.println("Building chunk "+this.chunkPosition);

        main_mesh.upload_Vertex_data();
        status = Status.Complete;
    }
    public void ReBuildMesh(){
        status = Status.Loaded;
        this.main_mesh.Number_of_Verts = 0;
        this.main_mesh.Number_of_vtcords = 0;
        this.main_mesh.Number_of_TriFaces = 0;
        this.main_mesh.Number_of_QuadFaces = 0;
        for(int Y=0;Y<Size;Y++){
            for(int X=0;X<Size;X++){
                for(int Z=0;Z<Size;Z++){
                    int block = GetLocal(new IVec3(X, Y, Z));
                    if(block != 0){
                        Put(block, new IVec3(X, Y, Z));
                    }
                }
            }
        }
        status = Status.MeshBuilt;
        main_mesh.upload_Vertex_data();
        status = Status.Complete;
    }
    public static Chunk FromPos(Vec3 pos){
        Chunk selected_chunk = null;
        IVec3 Ipos = convert_to_chunk_pos(pos);
        for(int X=0;X<Main.ChunkCount;X++){
            Chunk chunk = Main.ChunkArray[X];
            if(IVec3.Equal(chunk.chunkPosition, Ipos)){
                selected_chunk = chunk;
                break;
            }
        }
        return selected_chunk;
    }
    public static Chunk FromPos(Vec3 pos, Chunk guess){
        if(guess != null) {
            if (IVec3.Equal(guess.chunkPosition, pos.ToInt())) {
                return guess;
            }
        }
    
        Chunk selected_chunk = null;
        IVec3 Ipos = convert_to_chunk_pos(pos);
        for(int X=0;X<Main.ChunkCount;X++){
            Chunk chunk = Main.ChunkArray[X];
            if(IVec3.Equal(chunk.chunkPosition, Ipos)){
                selected_chunk = chunk;
                break;
            }
        }
        return selected_chunk;
    }
    public static Chunk FromChunkPos(IVec3 Pos){
        Chunk selected_chunk = null;
        for(int X=0;X<Main.ChunkCount;X++){
            Chunk chunk = Main.ChunkArray[X];
            if(IVec3.Equal(chunk.chunkPosition, Pos)){
                selected_chunk = chunk;
                break;
            }
        }
        return selected_chunk;
    }
    public static IVec3 convert_to_chunk_pos(Vec3 pos){
        int X,Y,Z;
        if(pos.X < 0){
            X = (int)(pos.X-(Size-1))/Size;
        }
        else{
            X = (int)(pos.X)/Size;
        }
        if(pos.Y < 0){
            Y = (int)(pos.Y-(Size-1))/Size;
        }
        else{
            Y = (int)(pos.Y)/Size;
        }
        if(pos.Z < 0){
            Z = (int)(pos.Z-(Size-1))/Size;
        }
        else{
            Z = (int)(pos.Z)/Size;
        }

        return new IVec3(X,Y,Z);
    }

    public void Put_side(int cubeID, int side, IVec3 Position){
        Mesh mesh = Block.Blocks[cubeID].Sides[side];
        mesh.position = Position.ToFloat();
        main_mesh.add(mesh);
    }
    public void Put(int cubeID, IVec3 Position){
        if(Block.Blocks[cubeID].Full) {
            if (!Block.Blocks[GetLocal(new IVec3(Position.X - 1, Position.Y, Position.Z))].Full) {
                Put_side(cubeID, 1, Position);
            }
            if (!Block.Blocks[GetLocal(new IVec3(Position.X + 1, Position.Y, Position.Z))].Full) {
                Put_side(cubeID, 0, Position);
            }
            if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y + 1, Position.Z))].Full) {
                Put_side(cubeID, 2, Position);
            }
            if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y - 1, Position.Z))].Full) {
                Put_side(cubeID, 3, Position);
            }
            if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y, Position.Z - 1))].Full) {
                Put_side(cubeID, 4, Position);
            }
            if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y, Position.Z + 1))].Full) {
                Put_side(cubeID, 5, Position);
            }
        }
        else{
            Mesh mesh = Block.Blocks[cubeID].mesh;
            mesh.position = Position.ToFloat();
            main_mesh.add(mesh);
        }
    }

    public boolean Place(int BLockID, IVec3 blockpos){
        IVec3 Lpos = ConvertToLocal(blockpos);
        if(GetLocal(Lpos) == 0) {
            if(this.is_empty){
                blocks = new byte[Size*Size*Size];
                main_mesh = new Mesh(Block.TextureBufferObject);
                main_mesh.position = this.position.ToFloat();
                this.is_empty = false;
            }
            this.blocks[Lpos.Y*Size*Size + Lpos.X*Size + Lpos.Z] = (byte)BLockID;
            ReBuildMesh();
            return true;
        }
        return false;
    }
    public void Delete(IVec3 blockpos){
        IVec3 Lpos = ConvertToLocal(blockpos);
        if(GetLocal(Lpos) != 0) {
            this.blocks[Lpos.Y * Size * Size + Lpos.X * Size + Lpos.Z] = 0;
            ReBuildMesh();
        }
    }

    public boolean validCord(IVec3 Position) {
        return (Position.X >= 0 && Position.X < Size) && (Position.Y >= 0 && Position.Y < Size) && (Position.Z >= 0 && Position.Z < Size);
    }

    public int GetLocal(IVec3 Position){
        if(validCord(Position) && blocks != null){
            return blocks[Position.Y*Size*Size + Position.X*Size + Position.Z];
        }
        return 0;
    }
    public int GetGlobal(Vec3 Gpos){
        IVec3 Lpos = ConvertToLocal(Gpos.ToInt());
        if(validCord(Lpos) && blocks != null){
            return blocks[Lpos.Y*Size*Size + Lpos.X*Size + Lpos.Z];
        }
        return 0;
    }
    public IVec3 ConvertToLocal(IVec3 position){
        return new IVec3(position.X-this.position.X, position.Y-this.position.Y, position.Z-this.position.Z);
    }

    public void GenChunk(){
        //System.out.println("Generating chunk "+this.chunkPosition);
        for (int X = 0; X < Size;X++) {
            for (int Z = 0; Z < Size; Z++) {
                int h;
                try {
                    h = Byte.toUnsignedInt(noisemap[(int) (this.position.X + (PerlinNoise2D.sizeX / 2) + X) * PerlinNoise2D.sizeX + (int) (this.position.Z + (PerlinNoise2D.sizeX / 2) + Z)]);
                }
                catch(Exception e){
                    h = Size;
                }
                //int h = Byte.toUnsignedInt(noisemap[(int)(X)*100 + (int)(Z)]);
                float a = h/255.0f;
                a *= 1f;
                float b = 4;
                int cb = (int)this.chunkPosition.Y;
                h = -1;

                boolean isFull = false;
                if(a > ((cb+1) / b)){
                    h = Size-1;
                    isFull = true;
                }
                if (a > (cb / b) && a < ((cb+1)/b)) {
                    h = (int) (((a - (cb / b)) * b) * Size);
                }




                for(int Y=h;Y!=-1;Y--) {
                    byte blockType = 3;
                    if(Y == h){blockType = 1;}
                    if(isFull){blockType = 2;}
                    blocks[Y * Size * Size + X * Size + Z] = blockType;
                }
            }
        }


    }
    public void Unload(){
        Save();
        int index = -1;
        for(int X=0;X<Main.ChunkCount;X++){
            if(Main.ChunkArray[X] == this){
                index = X;
                break;
            }
        }
        if(index != -1) {
            System.arraycopy(Main.ChunkArray, index + 1, Main.ChunkArray, index, Main.ChunkCount - index);
            Main.ChunkCount--;
        }
        this.blocks = null;
        if(this.main_mesh != null) {
            this.main_mesh.Remove();
        }
    }

    public void Save(){
        if(this.is_empty){
            return;
        }

        try{
            File file = new File(this.FilePath);
            file.createNewFile();
        }
        catch(Exception e){
            System.out.println("Error Saving Chunk");
            e.printStackTrace();
        }
        byte [] data = new byte[Size*Size*Size];
        int datacount = 0;

        for(int X=0;X<blocks.length;X++){
            if(X < blocks.length-3) {
                if (blocks[X] == blocks[X + 1]) {
                    byte curentblock = blocks[X];
                    int start = X;
                    int end = X;
                    for(int Y=X;Y<blocks.length-3;Y++){ // this is odd -Y
                        if(blocks[Y] == curentblock){
                            end++;
                        }
                        else{
                            break;
                        }
                        if((end-start) == 254){
                            break;
                        }
                    }
                    data[datacount] = (byte)255;
                    datacount++;
                    data[datacount] = (byte)(end-start);
                    datacount++;
                    data[datacount] = curentblock;
                    datacount++;
                    X += (end-start)-1;
                }
                else{
                    data[datacount] = blocks[X];
                    datacount++;
                }
            }
            else{
                data[datacount] = blocks[X];
                datacount++;
            }
        }



        try{
            FileOutputStream outputStream = new FileOutputStream(this.FilePath);
            outputStream.write(data, 0, datacount);
            outputStream.close();
        }
        catch(Exception e){
            System.out.println("Error Saving Chunk");
            e.printStackTrace();
        }
    }
}

package org.solumground;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class Chunk{
    public static int Size = 16;
    public static byte [] noisemap;
    public static Random RNG = new Random();
    public static int GenHeight = 64;

    public boolean is_empty;
    public String FilePath;
    private byte [] blocks;
    public IVec3 position;
    public IVec3 chunkPosition;

    public Mesh main_mesh;

    public Status status;
    public Chunk [] NearChunks = new Chunk[27];

    public enum Status{
        Started,
        Loaded,
        MeshBuilt,
        Complete
    }

    public static void Init(){
        noisemap = PerlinNoise2D.getNoiseMap();
        //noisemap = Noise.MountainGen();
    }
    public static Chunk FromPos(Vec3 pos){
        Chunk selected_chunk = null;
        IVec3 Ipos = convert_to_chunk_pos(pos);
        for(int X=0;X<Main.ChunkArray.size();X++){
            Chunk chunk = Main.ChunkArray.get(X);
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
        for(int X=0;X<Main.ChunkArray.size();X++){
            Chunk chunk = Main.ChunkArray.get(X);
            if(IVec3.Equal(chunk.chunkPosition, Ipos)){
                selected_chunk = chunk;
                break;
            }
        }
        return selected_chunk;
    }
    public static Chunk FromChunkPos(IVec3 Pos){
        Chunk selected_chunk = null;
        for(Chunk chunk : Main.ChunkArray){
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

    public Chunk(String Path, IVec3 Position) {
        status = Status.Started;
        this.position = new IVec3(Position.X * Size, Position.Y * Size, Position.Z * Size);
        this.chunkPosition = new IVec3(Position);
        this.FilePath = Path + "/block_data_" + this.chunkPosition.X + "_" + this.chunkPosition.Y + "_" + this.chunkPosition.Z + ".dat";

        GetNear();


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
            int Ypos = 0;
            int Zpos = 0;
            int Xpos = 0;
            for (int X = 0; X < data.length; X++) {
                if (Byte.toUnsignedInt(data[X]) == 255) {
                    int size = Byte.toUnsignedInt(data[X + 1]);
                    byte blockID = (byte) Byte.toUnsignedInt(data[X + 2]);
                    for (int Y = 0; Y < size; Y++) {
                        Set(new IVec3(Xpos, Ypos, Zpos), blockID);
                        Zpos++;
                        if(Zpos == Size){
                            Xpos++;
                            Zpos = 0;
                            if(Xpos == Size){
                                Ypos++;
                                Xpos = 0;
                            }
                        }
                    }
                    X += 2;
                } else {
                    Set(new IVec3(Xpos, Ypos, Zpos), (byte) Byte.toUnsignedInt(data[X]));
                    Zpos++;
                    if(Zpos == Size){
                        Xpos++;
                        Zpos = 0;
                        if(Xpos == Size){
                            Ypos++;
                            Xpos = 0;
                        }
                    }
                }
            }
        } else {
            GenChunk();
        }
        status = Status.Loaded;

        this.is_empty = CheckIsEmpty();
        if(this.is_empty){
            blocks = null;
        }
        Main.ChunkArray.add(this);
    }
    public void GetNear(){
        Chunk NextTo;

        for(int X=-1;X<2;X++){
            for(int Y=-1;Y<2;Y++){
                for(int Z=-1;Z<2;Z++){
                    NextTo = FromChunkPos(new IVec3(this.chunkPosition.X+X, this.chunkPosition.Y+Y, this.chunkPosition.Z+Z));
                    if(NextTo != this) {
                        NearChunks[(Y+1)*9 + (X+1)*3 + (Z+1)] = NextTo;
                    }
                }
            }
        }
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
        if(this.is_empty){return;}
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
    public void Put_side(int cubeID, int side, IVec3 Position){
        Mesh mesh = Block.Blocks[cubeID].Sides[side];
        mesh.position = Position.ToFloat();
        main_mesh.add(mesh);
    }
    public void Put(int BlockID, IVec3 Position){
        if(Block.Blocks[BlockID].Full) {
            if(Position.X == 0){
                Chunk NextChunk = NearChunks[10]; //1*9 + 0*3 + 1
                if(NextChunk != null){
                    if (!Block.Blocks[NextChunk.GetLocal(new IVec3(Size-1, Position.Y, Position.Z))].Full) {
                        Put_side(BlockID, Block.LEFT, Position);
                    }
                }
                if (!Block.Blocks[GetLocal(new IVec3(Position.X + 1, Position.Y, Position.Z))].Full) {
                    Put_side(BlockID, Block.RIGHT, Position);
                }
            }
            else if(Position.X == Size-1){
                Chunk NextChunk = NearChunks[16]; //1*9 + 2*3 + 1
                if(NextChunk != null){
                    if (!Block.Blocks[NextChunk.GetLocal(new IVec3(0, Position.Y, Position.Z))].Full) {
                        Put_side(BlockID, Block.RIGHT, Position);
                    }
                }
                if (!Block.Blocks[GetLocal(new IVec3(Position.X - 1, Position.Y, Position.Z))].Full) {
                    Put_side(BlockID, Block.LEFT, Position);
                }
            }
            else {
                if (!Block.Blocks[GetLocal(new IVec3(Position.X - 1, Position.Y, Position.Z))].Full) {
                    Put_side(BlockID, 1, Position);
                }
                if (!Block.Blocks[GetLocal(new IVec3(Position.X + 1, Position.Y, Position.Z))].Full) {
                    Put_side(BlockID, 0, Position);
                }
            }


            if(Position.Y == 0){
                Chunk NextChunk = NearChunks[4]; //0*9 + 1*3 + 1
                if(NextChunk != null){
                    if (!Block.Blocks[NextChunk.GetLocal(new IVec3(Position.X, Size-1, Position.Z))].Full) {
                        Put_side(BlockID, Block.BOTTOM, Position);
                    }
                }
                if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y + 1, Position.Z))].Full) {
                    Put_side(BlockID, Block.TOP, Position);
                }
            }
            else if(Position.Y == Size-1){
                Chunk NextChunk = NearChunks[22]; //2*9 + 1*3 + 1
                if(NextChunk != null){
                    if (!Block.Blocks[NextChunk.GetLocal(new IVec3(Position.X, 0, Position.Z))].Full) {
                        Put_side(BlockID, Block.TOP, Position);
                    }
                }
                if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y - 1, Position.Z))].Full) {
                    Put_side(BlockID, Block.BOTTOM, Position);
                }
            }
            else {
                if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y - 1, Position.Z))].Full) {
                    Put_side(BlockID, Block.BOTTOM, Position);
                }
                if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y + 1, Position.Z))].Full) {
                    Put_side(BlockID, Block.TOP, Position);
                }
            }


            if(Position.Z == 0){
                Chunk NextChunk = NearChunks[12]; //1*9 + 1*3 + 0
                if(NextChunk != null){
                    if (!Block.Blocks[NextChunk.GetLocal(new IVec3(Position.X, Position.Y, Size-1))].Full) {
                        Put_side(BlockID, Block.FRONT, Position);
                    }
                }
                if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y, Position.Z+1))].Full) {
                    Put_side(BlockID, Block.BACK, Position);
                }
            }
            else if(Position.Z == Size-1){
                Chunk NextChunk = NearChunks[14]; //1*9 + 1*3 + 2
                if(NextChunk != null){
                    if (!Block.Blocks[NextChunk.GetLocal(new IVec3(Position.X, Position.Y, 0))].Full) {
                        Put_side(BlockID, Block.BACK, Position);
                    }
                }
                if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y, Position.Z - 1))].Full) {
                    Put_side(BlockID, Block.FRONT, Position);
                }
            }
            else {
                if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y, Position.Z - 1))].Full) {
                    Put_side(BlockID, Block.FRONT, Position);
                }
                if (!Block.Blocks[GetLocal(new IVec3(Position.X, Position.Y, Position.Z + 1))].Full) {
                    Put_side(BlockID, Block.BACK, Position);
                }
            }

        }


        else{
            Mesh mesh = Block.Blocks[BlockID].mesh;
            mesh.position = Position.ToFloat();
            main_mesh.add(mesh);
        }
    }
    public void Set(IVec3 Position, int BlockID){
        blocks[Position.Y*Size*Size + Position.X*Size + Position.Z] = (byte)BlockID;
        if(Block.Blocks[BlockID].Illuminated){
            new Light(ConvertToGlobalPos(Position).ToFloat(), Block.Blocks[BlockID].LightStrength,
                    Block.Blocks[BlockID].LightColor[0],
                    Block.Blocks[BlockID].LightColor[1],
                    Block.Blocks[BlockID].LightColor[2]);

            for(Chunk NextTo : NearChunks) {
                if(NextTo != null){
                    if(NextTo.main_mesh != null){
                        MeshBuilder.LightUpdateBuffer.add(NextTo);
                    }
                }
            }
        }
    }

    public boolean Place(int BlockID, IVec3 blockpos){
        GetNear();
        IVec3 Lpos = ConvertToLocal(blockpos);
        if(Block.Blocks[GetLocal(Lpos)].Replaceable) {
            if(this.is_empty){
                blocks = new byte[Size*Size*Size];
                main_mesh = new Mesh(Block.TextureBufferObject);
                main_mesh.position = this.position.ToFloat();
                this.is_empty = false;
            }
            Set(Lpos, BlockID);
            Block.Blocks[BlockID].OnPlace.Run();
            //ReBuildMesh();
            return true;
        }
        return false;
    }
    public boolean Break(IVec3 blockpos){
        GetNear();
        IVec3 Lpos = ConvertToLocal(blockpos);
        if(!Block.Blocks[GetLocal(Lpos)].UnBreakable) {
            this.blocks[Lpos.Y * Size * Size + Lpos.X * Size + Lpos.Z] = 0;
            for(int X=0;X<Light.lights.size();X++){
                Light light = Light.lights.get(X);
                if(IVec3.Equal(blockpos, light.position.ToInt())){
                    Light.lights.remove(X);
                    for(Chunk NextTo : NearChunks) {
                        if(NextTo != null){
                            if(NextTo.main_mesh != null){
                                MeshBuilder.LightUpdateBuffer.add(NextTo);
                            }
                        }
                    }
                }
            }
            Block.Blocks[GetLocal(Lpos)].OnBreak.Run();
            //ReBuildMesh();
            return true;
        }
        return false;
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
    public IVec3 ConvertToGlobalPos(IVec3 pos){
        return new IVec3(this.position.X+pos.X, this.position.Y+pos.Y, this.position.Z+pos.Z);
    }

    public void GenChunk(){
        for (int X = 0; X < Size;X++) {
            for (int Z = 0; Z < Size; Z++) {
                int NoiseX = this.position.X + (PerlinNoise2D.sizeX / 2) + X;
                int NoiseZ = this.position.Z + (PerlinNoise2D.sizeY / 2) + Z;
                if(NoiseX >= PerlinNoise2D.sizeX){NoiseX = (PerlinNoise2D.sizeX-1) - (NoiseX % PerlinNoise2D.sizeX);}
                if(NoiseX <= 0){NoiseX = -NoiseX % PerlinNoise2D.sizeX;}
                if(NoiseZ >= PerlinNoise2D.sizeY){NoiseZ = (PerlinNoise2D.sizeY-1) - (NoiseZ % PerlinNoise2D.sizeY);}
                if(NoiseZ <= 0){NoiseZ = -NoiseZ % PerlinNoise2D.sizeY;}
                int NoiseH = Byte.toUnsignedInt(noisemap[NoiseX * PerlinNoise2D.sizeX + NoiseZ]);

                int GH = (int)(NoiseH*GenHeight / 255.0f);
                int LH = -1;

                boolean isFull = false;
                if(GH > this.position.Y){
                    LH = Size-1;
                    isFull = true;
                }
                else {
                    if (GH > this.position.Y - Size) {
                        LH = GH - ((this.position.Y - Size) + 1);
                    }
                }

                for(int Y=LH;Y!=-1;Y--) {
                    byte blockType = 3;
                    if(Y == LH){
                        blockType = 1;
                        float r = RNG.nextFloat();
                        if(r < 0.01){
                            blockType = 6;
                        }
                    }
                    if(isFull){blockType = 2;}
                    Set(new IVec3(X,Y,Z), blockType);
                }
            }
        }


    }
    public void Unload(){
        Save();
        this.blocks = null;
        if(this.main_mesh != null) {
            this.main_mesh.Remove();
        }
    }
    public void LightUpdate(){
        if(!this.is_empty) {
            this.main_mesh.LightUpdate();
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

package org.solumground;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Chunk{
    public static int Size = 10;
    public static byte [] noisemap;

    public boolean is_empty;
    public String FilePath;
    public byte [] blocks;
    public Vec3 position;
    public Vec3 chunkPosition;
    public Vec3 MegaChunkPosition;

    public Mesh main_mesh;
    public boolean Exists;

    public static void Init(){
        noisemap = PerlinNoise2D.getNoiseMap();
        //noisemap = Noise.MountainGen();
    }
    public Chunk(String Path, int Xpos,int Ypos,int Zpos) {
        this.position = new Vec3(Xpos * Size, Ypos * Size, Zpos * Size);
        this.chunkPosition = new Vec3(Xpos, Ypos, Zpos);
        this.MegaChunkPosition = convert_to_Megachunk_pos(this.position);
        this.FilePath = Path + "/world_data_" + (int) this.chunkPosition.X + "_" + (int) this.chunkPosition.Y + "_" + (int) this.chunkPosition.Z + ".dat";


        this.Exists = true;
        for (int X = 0; X < Main.ChunkCount; X++) {
            if (Vec3.Equal(Main.ChunkArray[X].chunkPosition, this.chunkPosition)) {
                this.Exists = false;
                return;
            }
        }

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

        this.is_empty = true;
        for (int Y = 0; Y < Size; Y++) {
            for (int X = 0; X < Size; X++) {
                for (int Z = 0; Z < Size; Z++) {
                    int block = blocks[Y*Size*Size + X*Size + Z];
                    if (block != 0) {
                        this.is_empty = false;
                    }
                }
            }
        }
        Main.ChunkArray[Main.ChunkCount] = this;
        Main.ChunkCount++;

    }
    public void buildMesh(){
        if(this.is_empty){
            System.out.println("Tried to build empty chunk at "+chunkPosition);
            blocks = null;
            return;
        }
        main_mesh = new Mesh(Block.TextureBufferObject);
        main_mesh.position = new Vec3(this.position);

        for(int Y=0;Y<Size;Y++){
            for(int X=0;X<Size;X++){
                for(int Z=0;Z<Size;Z++){
                    int block = Get(X,Y,Z);
                    if(block != 0){
                        Put(block, X,Y,Z);
                    }
                }
            }
        }

        System.out.println("Building chunk "+this.chunkPosition);

        main_mesh.upload_Vertex_data();
    }
    public static Chunk FromPos(Vec3 pos){
        Chunk selected_chunk = null;
        pos = convert_to_chunk_pos(pos);
        for(int X=0;X<Main.ChunkCount;X++){
            Chunk chunk = Main.ChunkArray[X];
            if(Vec3.Equal(chunk.chunkPosition, pos)){
                selected_chunk = chunk;
                break;
            }
        }
        return selected_chunk;
    }
    public static Chunk FromPos(Vec3 pos, Chunk guess){
        if(guess != null) {
            if (Vec3.Equal(guess.chunkPosition, pos)) {
                return guess;
            }
        }
    
        Chunk selected_chunk = null;
        pos = convert_to_chunk_pos(pos);
        for(int X=0;X<Main.ChunkCount;X++){
            Chunk chunk = Main.ChunkArray[X];
            if(Vec3.Equal(chunk.chunkPosition, pos)){
                selected_chunk = chunk;
                break;
            }
        }
        return selected_chunk;
    }
    public static Chunk FromChunkPos(Vec3 Pos){
        Chunk selected_chunk = null;
        for(int X=0;X<Main.ChunkCount;X++){
            Chunk chunk = Main.ChunkArray[X];
            if(Vec3.Equal(chunk.chunkPosition, Pos)){
                selected_chunk = chunk;
                break;
            }
        }
        return selected_chunk;
    }
    public static Vec3 convert_to_chunk_pos(Vec3 pos){
        int X = (int)(pos.X)/Size;
        int Y = (int)(pos.Y)/Size;
        int Z = (int)(pos.Z)/Size;

        return new Vec3(X,Y,Z);
    }
    public static Vec3 convert_to_Megachunk_pos(Vec3 pos){
        int MSize = Size*10;
        int X = (int)pos.X;
        int cX = 0;
        if(X>0) {
            while (X >= MSize) {X -= MSize;cX++;}X = cX;}
        else{
            while(X < 0){X += MSize;cX--;}X = cX;}
        int Y = (int)pos.Y;
        int cY = 0;
        if(Y>0) {
            while (Y >= MSize) {
                Y -= MSize;
                cY++;
            }
            Y = cY;
        }
        else{
            while(Y <= 0){
                Y += MSize;
                cY--;
            }
            Y = cY;
        }
        int Z = (int)pos.Z;
        int cZ = 0;
        if(Z>0) {
            while (Z >= MSize) {
                Z -= MSize;
                cZ++;
            }
            Z = cZ;
        }
        else{
            while(Z < 0){
                Z += MSize;
                cZ--;
            }
            Z = cZ;
        }

        return new Vec3(X,Y,Z);
    }

    public void Put_side(int cubeID, int side, int X,int Y,int Z){
        Mesh mesh = Block.Blocks[cubeID].Sides[side];
        mesh.position.X = X;
        mesh.position.Y = Y;
        mesh.position.Z = Z;
        main_mesh.add(mesh);
    }
    public void Put(int cubeID, int X,int Y,int Z){
        if(Get(X-1,Y,Z) == 0){Put_side(cubeID,1, X,Y,Z);}
        if(Get(X+1,Y,Z) == 0){Put_side(cubeID,0, X,Y,Z);}
        if(Get(X,Y+1,Z) == 0){Put_side(cubeID,2, X,Y,Z);}
        if(Get(X,Y-1,Z) == 0){Put_side(cubeID,3, X,Y,Z);}
        if(Get(X,Y,Z-1) == 0){Put_side(cubeID,4, X,Y,Z);}
        if(Get(X,Y,Z+1) == 0){Put_side(cubeID,5, X,Y,Z);}
    }

    public boolean Place(int cubeID, Vec3 blockpos){
        int X = (int)(blockpos.X-this.position.X);
        int Y = (int)(blockpos.Y-this.position.Y);
        int Z = (int)(blockpos.Z-this.position.Z);

        boolean can_place = false;
        if(X >= 0 && X < Size){
            if(Y >= 0 && Y < Size){
                if(Z >= 0 && Z < Size){
                    if(Get(X,Y,Z) == 0){can_place = true;}
                }
            }
        }
        if(can_place) {
            if(this.is_empty){
                blocks = new byte[Size*Size*Size];
                main_mesh = new Mesh(Block.TextureBufferObject);
                this.is_empty = false;
            }
            this.blocks[Y*Size*Size + X*Size + Z] = (byte)cubeID;

            main_mesh.Number_of_Verts = 0;
            main_mesh.Number_of_vtcords = 0;
            main_mesh.Number_of_TriFaces = 0;
            main_mesh.Number_of_QuadFaces = 0;

            main_mesh.Original_VertexArray = new float[0];
            main_mesh.VTcords_array = new float[0];
            main_mesh.TriFaceArray = new int[0];
            main_mesh.QuadFaceArray = new int[0];

            for(int Yp=0;Yp<Size;Yp++){
                for(int Xp=0;Xp<Size;Xp++){
                    for(int Zp=0;Zp<Size;Zp++){
                        int block = Get(Xp,Yp,Zp);
                        if(block != 0){
                            Put(block, Xp,Yp,Zp);
                        }
                    }
                }
            }
            main_mesh.upload_Vertex_data();
            return true;
        }
        else{return false;}
    }
    public void Delete(Vec3 blockpos){
        int Xp = (int)(blockpos.X-this.position.X);
        int Yp = (int)(blockpos.Y-this.position.Y);
        int Zp = (int)(blockpos.Z-this.position.Z);

        boolean can_break = false;
        if(Xp >= 0 && Xp < Size){
            if(Yp >= 0 && Yp < Size){
                if(Zp >= 0 && Zp < Size){
                    if(Get(Xp,Yp,Zp) != 0){can_break = true;}
                }
            }
        }
        if(can_break) {
            this.blocks[Yp * Size * Size + Xp * Size + Zp] = 0;

            main_mesh.Number_of_Verts = 0;
            main_mesh.Number_of_vtcords = 0;
            main_mesh.Number_of_TriFaces = 0;
            main_mesh.Number_of_QuadFaces = 0;

            main_mesh.Original_VertexArray = new float[0];
            main_mesh.VTcords_array = new float[0];
            main_mesh.TriFaceArray = new int[0];
            main_mesh.QuadFaceArray = new int[0];

            this.is_empty = true;
            for (int Y = 0; Y < Size; Y++) {
                for (int X = 0; X < Size; X++) {
                    for (int Z = 0; Z < Size; Z++) {
                        int block = blocks[Y*Size*Size + X*Size + Z];
                        if (block != 0) {
                            Put(block, X, Y, Z);
                            this.is_empty = false;
                        }
                    }
                }
            }
        }
        main_mesh.upload_Vertex_data();

    }

    public boolean validCoord(int X, int Y, int Z) {
        return (X >= 0 && X < Size) && (Y >= 0 && Y < Size) && (Z >= 0 && Z < Size);
    }

    public int Get(int X,int Y,int Z){
        if(validCoord(X,Y,Z) && !this.is_empty){
            return blocks[Y*Size*Size + X*Size + Z];
        }
        return 0;
    }

    public void GenChunk(){
        System.out.println("Generating chunk "+this.chunkPosition);
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

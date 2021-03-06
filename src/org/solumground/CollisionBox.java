package org.solumground;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL21.*;

public class CollisionBox {
    Vec3 position;

    float BondPX;
    float BondPY;
    float BondPZ;
    float BondNX;
    float BondNY;
    float BondNZ;

    boolean is_player;

    Mesh wiremesh;
    
    public Chunk[] chunks = new Chunk[27];

    public CollisionBox(Vec3 position, float BPX, float BPY, float BPZ, float BNX, float BNY, float BNZ){
        this.position = position;

        this.is_player = false;
        this.BondPX = BPX;
        this.BondPY = BPY;
        this.BondPZ = BPZ;
        this.BondNX = BNX;
        this.BondNY = BNY;
        this.BondNZ = BNZ;

        this.wiremesh = new Mesh(Main.RootDir +"/assets/solumground/models/wireFrame.smobj", "");
        this.wiremesh.setWireFrame();
        wiremesh.setColor(0xFF0000FF);
        this.wiremesh.ScaleData((this.BondPX-this.BondNX)*.501f, (this.BondPY-this.BondNY)*.501f, (this.BondPZ-this.BondNZ)*.501f);
    }

    public boolean detect_collision(CollisionBox box){
        boolean fits_X = false;
        boolean fits_Y = false;
        boolean fits_Z = false;

        if (this.position.X + this.BondPX > (box.position.X + box.BondNX)) {
            if (this.position.X + this.BondNX < (box.position.X + box.BondPX)) {
                fits_X = true;
            }
        }
        if (this.position.Y + this.BondPY > (box.position.Y + box.BondNY)) {
            if (this.position.Y + this.BondNY < (box.position.Y + box.BondPY)) {
                fits_Y = true;
            }
        }
        if (this.position.Z + this.BondPZ > (box.position.Z + box.BondNZ)) {
            if (this.position.Z + this.BondNZ < (box.position.Z + box.BondPZ)) {
                fits_Z = true;
            }
        }
        return fits_X && fits_Z && fits_Y;
    }
    public void set_Is_player(boolean b){
        this.is_player = b;
    }

    public boolean detect_if_ontop(CollisionBox box){

        boolean fits_X = false;
        boolean fits_Z = false;
        boolean ontop = false;

        if (this.position.X + this.BondPX > (box.position.X + box.BondNX)) {
            if (this.position.X + this.BondNX < (box.position.X + box.BondPX)) {
                fits_X = true;
            }
        }
        if (this.position.Z + this.BondPZ > (box.position.Z + box.BondNZ)) {
            if (this.position.Z + this.BondNZ < (box.position.Z + box.BondPZ)) {
                fits_Z = true;
            }
        }
        if(this.position.Y == (box.position.Y + box.BondPY) - this.BondNY + .001f){
            ontop = true;
        }

        return fits_X && fits_Z && ontop;

    }

    public boolean On_Ground(){
        List<Object> VecAndBox = Get_Near();

        CollisionBox box;
        Vec3 vec3;
        for (int X=0;X<VecAndBox.size();X+=2) {
            vec3 = (Vec3) VecAndBox.get(X);
            box = (CollisionBox) VecAndBox.get(X+1);
            if (vec3 != null) {
                box.position = vec3;
                if (detect_if_ontop(box)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Object> Get_Near(){
        chunks[0] = Chunk.FromPos(this.position, chunks[0]);
        chunks[1] = Chunk.FromPos(new Vec3(this.position.X, this.position.Y-Chunk.Size, this.position.Z), chunks[1]);

        chunks[2] = Chunk.FromPos(new Vec3(this.position.X+Chunk.Size, this.position.Y-Chunk.Size, this.position.Z), chunks[2]);
        chunks[3] = Chunk.FromPos(new Vec3(this.position.X-Chunk.Size, this.position.Y-Chunk.Size, this.position.Z), chunks[3]);
        chunks[4] = Chunk.FromPos(new Vec3(this.position.X, this.position.Y-Chunk.Size, this.position.Z+Chunk.Size), chunks[4]);
        chunks[5] = Chunk.FromPos(new Vec3(this.position.X, this.position.Y-Chunk.Size, this.position.Z-Chunk.Size), chunks[5]);
        chunks[6] = Chunk.FromPos(new Vec3(this.position.X+Chunk.Size, this.position.Y-Chunk.Size, this.position.Z+Chunk.Size), chunks[6]);
        chunks[7] = Chunk.FromPos(new Vec3(this.position.X+Chunk.Size, this.position.Y-Chunk.Size, this.position.Z-Chunk.Size), chunks[7]);
        chunks[8] = Chunk.FromPos(new Vec3(this.position.X-Chunk.Size, this.position.Y-Chunk.Size, this.position.Z+Chunk.Size), chunks[8]);
        chunks[9] = Chunk.FromPos(new Vec3(this.position.X-Chunk.Size, this.position.Y-Chunk.Size, this.position.Z-Chunk.Size), chunks[9]);

        chunks[10] = Chunk.FromPos(new Vec3(this.position.X+Chunk.Size, this.position.Y, this.position.Z), chunks[10]);
        chunks[11] = Chunk.FromPos(new Vec3(this.position.X-Chunk.Size, this.position.Y, this.position.Z), chunks[11]);
        chunks[12] = Chunk.FromPos(new Vec3(this.position.X, this.position.Y, this.position.Z+Chunk.Size), chunks[12]);
        chunks[13] = Chunk.FromPos(new Vec3(this.position.X, this.position.Y, this.position.Z-Chunk.Size), chunks[13]);
        chunks[14] = Chunk.FromPos(new Vec3(this.position.X+Chunk.Size, this.position.Y, this.position.Z+Chunk.Size), chunks[14]);
        chunks[15] = Chunk.FromPos(new Vec3(this.position.X+Chunk.Size, this.position.Y, this.position.Z-Chunk.Size), chunks[15]);
        chunks[16] = Chunk.FromPos(new Vec3(this.position.X-Chunk.Size, this.position.Y, this.position.Z+Chunk.Size), chunks[16]);
        chunks[17] = Chunk.FromPos(new Vec3(this.position.X-Chunk.Size, this.position.Y, this.position.Z-Chunk.Size), chunks[17]);

        chunks[18] = Chunk.FromPos(new Vec3(this.position.X, this.position.Y+Chunk.Size, this.position.Z), chunks[18]);
        chunks[19] = Chunk.FromPos(new Vec3(this.position.X+Chunk.Size, this.position.Y+Chunk.Size, this.position.Z), chunks[19]);
        chunks[20] = Chunk.FromPos(new Vec3(this.position.X-Chunk.Size, this.position.Y+Chunk.Size, this.position.Z), chunks[20]);
        chunks[21] = Chunk.FromPos(new Vec3(this.position.X, this.position.Y+Chunk.Size, this.position.Z+Chunk.Size), chunks[21]);
        chunks[22] = Chunk.FromPos(new Vec3(this.position.X, this.position.Y+Chunk.Size, this.position.Z-Chunk.Size), chunks[22]);
        chunks[23] = Chunk.FromPos(new Vec3(this.position.X+Chunk.Size, this.position.Y+Chunk.Size, this.position.Z+Chunk.Size), chunks[23]);
        chunks[24] = Chunk.FromPos(new Vec3(this.position.X+Chunk.Size, this.position.Y+Chunk.Size, this.position.Z-Chunk.Size), chunks[24]);
        chunks[25] = Chunk.FromPos(new Vec3(this.position.X-Chunk.Size, this.position.Y+Chunk.Size, this.position.Z+Chunk.Size), chunks[25]);
        chunks[26] = Chunk.FromPos(new Vec3(this.position.X-Chunk.Size, this.position.Y+Chunk.Size, this.position.Z-Chunk.Size), chunks[26]);



        List<Object> VecAndBox = new ArrayList<>(343*2);

        for(int X=(-3);X<4;X++){
            for(int Y=(-3);Y<4;Y++){
                for(int Z=(-3);Z<4;Z++){
                    for(int C=0;C<27;C++){
                        if(chunks[C] == null){continue;}
                        Vec3 v = new Vec3((int)(this.position.X+X), (int)(this.position.Y+Y), (int)(this.position.Z+Z));
                        Block block = Block.Blocks[chunks[C].GetGlobal(v)];
                        if(block.HasCollisionBox){
                            VecAndBox.add(v);
                            VecAndBox.add(block.collisionBox);
                            break;
                        }
                    }
                }
            }
        }
        return VecAndBox;
    }

    public void active_update(){
        List<Object> VecAndBox = Get_Near();

        CollisionBox box;
        Vec3 vec3;
        for (int X=0;X<VecAndBox.size();X+=2) {
            vec3 = (Vec3) VecAndBox.get(X);
            box = (CollisionBox) VecAndBox.get(X+1);
            if (vec3 != null) {
                box.position = vec3;
                if (Main.showCollisionBox) {
                    box.draw();
                }
                if (detect_collision(box)) {
                    float XPd = (box.position.X + box.BondPX) - (this.position.X + this.BondNX);
                    if (XPd < 0) {
                        XPd *= -1;
                    }
                    float XNd = (box.position.X + box.BondNX) - (this.position.X + this.BondPX);
                    if (XNd < 0) {
                        XNd *= -1;
                    }
                    float YPd = (box.position.Y + box.BondPY) - (this.position.Y + this.BondNY);
                    if (YPd < 0) {
                        YPd *= -1;
                    }
                    float YNd = (box.position.Y + box.BondNY) - (this.position.Y + this.BondPY);
                    if (YNd < 0) {
                        YNd *= -1;
                    }
                    float ZPd = (box.position.Z + box.BondPZ) - (this.position.Z + this.BondNZ);
                    if (ZPd < 0) {
                        ZPd *= -1;
                    }
                    float ZNd = (box.position.Z + box.BondNZ) - (this.position.Z + this.BondPZ);
                    if (ZNd < 0) {
                        ZNd *= -1;
                    }

                    float[] Nearest = new float[6];
                    Nearest[0] = YPd;
                    Nearest[1] = XPd;
                    Nearest[2] = XNd;
                    Nearest[3] = YNd;
                    Nearest[4] = ZPd;
                    Nearest[5] = ZNd;
                    Arrays.sort(Nearest);

                    for (float N : Nearest) {
                        if (detect_collision(box)) {
                            if (N == XPd) {
                                this.position.X = (box.position.X + box.BondPX) - this.BondNX + .001f;
                            }
                            if (N == XNd) {
                                this.position.X = (box.position.X + box.BondNX) - this.BondPX - .001f;
                            }
                            if (N == YPd) {
                                this.position.Y = (box.position.Y + box.BondPY) - this.BondNY + .001f;
                            }
                            if (N == YNd) {
                                this.position.Y = (box.position.Y + box.BondNY) - this.BondPY - .001f;
                            }
                            if (N == ZPd) {
                                this.position.Z = (box.position.Z + box.BondPZ) - this.BondNZ + .001f;
                            }
                            if (N == ZNd) {
                                this.position.Z = (box.position.Z + box.BondNZ) - this.BondPZ - .001f;
                            }

                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public void draw(){
        this.wiremesh.position.X = this.position.X + (this.BondPX + this.BondNX) * .5f;
        this.wiremesh.position.Y = this.position.Y + (this.BondPY + this.BondNY) * .5f;
        this.wiremesh.position.Z = this.position.Z + (this.BondPZ + this.BondNZ) * .5f;
        if(this.is_player){
            glDisable(GL_CULL_FACE);
        }
        this.wiremesh.draw();

        if(this.is_player) {
            glEnable(GL_CULL_FACE);
        }
    }

}

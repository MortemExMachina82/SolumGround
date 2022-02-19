package org.solumground;


public class IVec3 {
    public int X;
    public int Y;
    public int Z;
    public IVec3(float X, float Y, float Z){
        this.X = (int)X;
        this.Y = (int)Y;
        this.Z = (int)Z;
    }
    public IVec3(int X, int Y, int Z) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
    }
    public IVec3(IVec3 pos){
            this.X = pos.X;
            this.Y = pos.Y;
            this.Z = pos.Z;
        }
    public IVec3(Vec3 pos){
        this.X = (int)pos.X;
        this.Y = (int)pos.Y;
        this.Z = (int)pos.Z;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", this.X, this.Y, this.Z);
    }

    public static boolean Equal(IVec3 pos1, IVec3 pos2){
        if(pos1 == null || pos2 == null){return false;}
        if(pos1.X == pos2.X){
            if(pos1.Y == pos2.Y){
                return pos1.Z == pos2.Z;
            }
        }
        return false;
    }
}

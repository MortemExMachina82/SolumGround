package org.solumground;


public class Vec3 {
    public float X;
    public float Y;
    public float Z;
    public Vec3(){
        this.X = 0;
        this.Y = 0;
        this.Z = 0;
    }
    public Vec3(float X, float Y, float Z){
        this.X = X;
        this.Y = Y;
        this.Z = Z;
    }
    public Vec3(int X, int Y, int Z){
        this.X = (float) X;
        this.Y = (float) Y;
        this.Z = (float) Z;
    }
    public Vec3(Vec3 pos){
        this.X = pos.X;
        this.Y = pos.Y;
        this.Z = pos.Z;
    }
    public Vec3(IVec3 pos){
        this.X = (float)pos.X;
        this.Y = (float)pos.Y;
        this.Z = (float)pos.Z;
    }
    public IVec3 ToInt(){
        return new IVec3(this);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", this.X, this.Y, this.Z);
    }

    public static boolean Equal(Vec3 pos1, Vec3 pos2){
        if(pos1 == null || pos2 == null){return false;}
        if(pos1.X == pos2.X){
            if(pos1.Y == pos2.Y){
                return pos1.Z == pos2.Z;
            }
        }
        return false;
    }
}

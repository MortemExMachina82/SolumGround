package org.solumground;


public class Vec4 {
    public float X;
    public float Y;
    public float Z;
    public float W;

    public Vec4(){
        this.X = 0f;
        this.Y = 0f;
        this.Z = 0f;
        this.W = 0f;
    }
    public Vec4(float X, float Y, float Z, float W){
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.W = W;
    }
    public Vec4(int X, int Y, int Z, int W){
        this.X = (float) X;
        this.Y = (float) Y;
        this.Z = (float) Z;
        this.W = (float) W;
    }
    public Vec4(Vec4 pos){
        this.X = pos.X;
        this.Y = pos.Y;
        this.Z = pos.Z;
        this.W = pos.W;
    }
    public Vec4(Vec3 pos){
        this.X = pos.X;
        this.Y = pos.Y;
        this.Z = pos.Z;
        this.W = 0f;
    }
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f, %.2f)", this.X, this.Y, this.Z, this.W);
    }

    public static boolean Equal(Vec4 pos1, Vec4 pos2){
        if(pos1 == null | pos2 == null){return false;}
        if(pos1.X == pos2.X){
            if(pos1.Y == pos2.Y){
                if(pos1.Z == pos2.Z){
                    return pos1.W == pos2.W;
                }
            }
        }
        return false;
    }
}
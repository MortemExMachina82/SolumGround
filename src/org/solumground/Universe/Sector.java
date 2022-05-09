package org.solumground.Universe;

import org.solumground.IVec3;
import org.solumground.Vec3;

import java.util.Random;

public class Sector {
    public static int Size = 1000000;
    public IVec3 position;
    public Star [] stars;
    public static Random RNG = new Random();

    public Sector(int X, int Y, int Z){
        this.position = new IVec3(X,Y,Z);
        int StarCount = 0;
        try {
            StarCount = (int)(50 + 50*RNG.nextFloat());

        }
        catch (Exception e){
            e.printStackTrace();
        }
        stars = new Star[StarCount];
        for(int XC=0;XC<StarCount;XC++){
            float StarX = Size*RNG.nextFloat();
            float StarY = Size*RNG.nextFloat();
            float StarZ = Size*RNG.nextFloat();
            float size = 1000 + 2000*RNG.nextFloat();
            int t = RNG.nextInt(3);
            Star star = new Star(StarX,StarY,StarZ, t,size, this);
            stars[XC] = star;
        }
    }
    public void Draw(Vec3 Playerpos){
        for(Star star : stars){
            float brightness = star.GetBrightness(Playerpos);
            switch(star.type){
                case Red:
                    Star.RedMesh.Scale(star.Size,star.Size,star.Size);
                    Star.RedMesh.position = star.GetGlobalPos();
                    Star.RedMesh.draw();
                case Blue:
                    Star.BlueMesh.Scale(star.Size,star.Size,star.Size);
                    Star.BlueMesh.position = star.GetGlobalPos();
                    Star.BlueMesh.draw();
                case Yello:
                    Star.YelloMesh.Scale(star.Size,star.Size,star.Size);
                    Star.YelloMesh.position = star.GetGlobalPos();
                    Star.YelloMesh.draw();
            }
        }
    }
}

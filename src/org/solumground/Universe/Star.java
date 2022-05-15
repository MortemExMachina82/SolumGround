package org.solumground.Universe;

import org.solumground.Main;
import org.solumground.Mesh;
import org.solumground.Vec3;

public class Star {
    public static Mesh RedMesh;
    public static Mesh BlueMesh;
    public static Mesh YelloMesh;
    public Sector ParentSector;
    public Vec3 SubPosition;
    public enum Type{
        Red,
        Blue,
        Yello,
    }
    public Type type;
    public float Size;

    public static void Init(){
        int RTBO = Main.LoadTexture("", 0x7F0000FF);
        Mesh mesh = new Mesh(Main.RootDir +"/assets/solumground/models/Star.smobj", Mesh.MESH_SMOBJ);
        RedMesh = new Mesh(RTBO);
        RedMesh.addNoUpload(mesh);
        RedMesh.FullLight = true;
        RedMesh.upload_Vertex_data();
        int BTBO = Main.LoadTexture("", 0x00007FFF);
        BlueMesh = new Mesh(BTBO);
        BlueMesh.addNoUpload(mesh);
        BlueMesh.FullLight = true;
        BlueMesh.upload_Vertex_data();
        int YTBO = Main.LoadTexture("", 0x7F7F00FF);
        YelloMesh = new Mesh(YTBO);
        YelloMesh.addNoUpload(mesh);
        YelloMesh.FullLight = true;
        YelloMesh.upload_Vertex_data();
    }

    public Star(float X,float Y, float Z, int type,float size, Sector sector){
        this.SubPosition = new Vec3(X,Y,Z);
        switch(type){
            case 0:this.type = Type.Red;break;
            case 1:this.type = Type.Blue;break;
            case 2:this.type = Type.Yello;break;
        }
        this.Size = size;
        this.ParentSector = sector;
    }

    public Vec3 GetGlobalPos(){
        return new Vec3(
                ParentSector.position.X*Sector.Size + SubPosition.X,
                ParentSector.position.Y*Sector.Size + SubPosition.Y,
                ParentSector.position.Z*Sector.Size + SubPosition.Z
        );
    }
    public float GetBrightness(Vec3 pos){
        Vec3 StarPos = GetGlobalPos();
        float distX = StarPos.X-pos.X;
        float distY = StarPos.Y-pos.Y;
        float distZ = StarPos.Z-pos.Z;
        return 100.0f/(distX*distX + distY*distY + distZ*distZ);
    }
}

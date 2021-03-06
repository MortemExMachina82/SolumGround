package org.solumground;

public class Math3D{
    public static void Matrix44_Multiply(float [] mat1, float [] mat2, float [] mat){
        mat[0] = mat1[0]*mat2[0] + mat1[1]*mat2[4] + mat1[2]*mat2[8] + mat1[3]*mat2[12];
        mat[1] = mat1[0]*mat2[1] + mat1[1]*mat2[5] + mat1[2]*mat2[9] + mat1[3]*mat2[13];
        mat[2] = mat1[0]*mat2[2] + mat1[1]*mat2[6] + mat1[2]*mat2[10] + mat1[3]*mat2[14];
        mat[3] = mat1[0]*mat2[3] + mat1[1]*mat2[7] + mat1[2]*mat2[11] + mat1[3]*mat2[15];

        mat[4] = mat1[4]*mat2[0] + mat1[5]*mat2[4] + mat1[6]*mat2[8] + mat1[7]*mat2[12];
        mat[5] = mat1[4]*mat2[1] + mat1[5]*mat2[5] + mat1[6]*mat2[9] + mat1[7]*mat2[13];
        mat[6] = mat1[4]*mat2[2] + mat1[5]*mat2[6] + mat1[6]*mat2[10] + mat1[7]*mat2[14];
        mat[7] = mat1[4]*mat2[3] + mat1[5]*mat2[7] + mat1[6]*mat2[11] + mat1[7]*mat2[15];

        mat[8] = mat1[8]*mat2[0] + mat1[9]*mat2[4] + mat1[10]*mat2[8] + mat1[11]*mat2[12];
        mat[9] = mat1[8]*mat2[1] + mat1[9]*mat2[5] + mat1[10]*mat2[9] + mat1[11]*mat2[13];
        mat[10] = mat1[8]*mat2[2] + mat1[9]*mat2[6] + mat1[10]*mat2[10] + mat1[11]*mat2[14];
        mat[11] = mat1[8]*mat2[3] + mat1[9]*mat2[7] + mat1[10]*mat2[11] + mat1[11]*mat2[15];

        mat[12] = mat1[12]*mat2[0] + mat1[13]*mat2[4] + mat1[14]*mat2[8] + mat1[15]*mat2[12];
        mat[13] = mat1[12]*mat2[1] + mat1[13]*mat2[5] + mat1[14]*mat2[9] + mat1[15]*mat2[13];
        mat[14] = mat1[12]*mat2[2] + mat1[13]*mat2[6] + mat1[14]*mat2[10] + mat1[15]*mat2[14];
        mat[15] = mat1[12]*mat2[3] + mat1[13]*mat2[7] + mat1[14]*mat2[11] + mat1[15]*mat2[15];


    }
    public static void Make3DRotationMatrix44(Vec3 Rotation, float [] mat){
        //float mat [] = new float[4*4];
        float [] mat_x = new float[4*4];
        float [] mat_y = new float[4*4];
        float [] mat_z = new float[4*4];

        mat_x[0] = 1;
        mat_x[5] = (float)Math.cos(Rotation.X*3.1415/180);
        mat_x[6] = -(float)Math.sin(Rotation.X*3.1415/180);
        mat_x[9] = (float)Math.sin(Rotation.X*3.1415/180);
        mat_x[10] = (float)Math.cos(Rotation.X*3.1415/180);
        mat_x[15] = 1;

        mat_y[0] = (float)Math.cos(Rotation.Y*3.1415/180);
        mat_y[2] = (float)Math.sin(Rotation.Y*3.1415/180);
        mat_y[5] = 1;
        mat_y[8] = -(float)Math.sin(Rotation.Y*3.1415/180);
        mat_y[10] = (float)Math.cos(Rotation.Y*3.1415/180);
        mat_y[15] = 1;

        mat_z[0] = (float)Math.cos(Rotation.Z*3.1415/180);
        mat_z[1] = -(float)Math.sin(Rotation.Z*3.1415/180);
        mat_z[4] = (float)Math.sin(Rotation.Z*3.1415/180);
        mat_z[5] = (float)Math.cos(Rotation.Z*3.1415/180);
        mat_z[10] = 1;
        mat_z[15] = 1;

        Matrix44_Multiply(mat_y, mat_x, mat);
        System.arraycopy(mat, 0, mat_x, 0, 16);
        Matrix44_Multiply(mat_x, mat_z, mat);


    }
    public static Vec3 Vec3X44MatrixMultiply(Vec3 pos, float [] mat){
        Vec3 output = new Vec3(0,0,0);
        output.X = pos.X*mat[4] + pos.Y*mat[1] + pos.Z*mat[2];
        output.Y = pos.X*mat[4] + pos.Y*mat[5] + pos.Z*mat[6];
        output.Z = pos.X*mat[8] + pos.Y*mat[9] + pos.Z*mat[10];

        return output;
    }
}













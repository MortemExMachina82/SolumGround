package org.solumground;

public class Noise {
    public static int Width = 300;
    public static int Hight = 300;
    public static byte [] map = new byte[Width*Hight];
    public static byte [] MountainGen(){
        int NMountain_Pos = 1;
        float [] mountain_pos;
        mountain_pos = new float[NMountain_Pos*2];
        mountain_pos[0] = .5f;
        mountain_pos[1] = .5f;

        for(int X=0;X<NMountain_Pos;X++){
            float H = .5f;
            float DT = .1f;
            float DB = .5f;
            for(int w=0;w<Width;w++){
                for(int h=0;h<Hight;h++){
                    float dist = (mountain_pos[X*2]*Width-w)*(mountain_pos[X*2]*Width-w) + (mountain_pos[X*2 + 1]*Hight-h)*(mountain_pos[X*2 + 1]*Hight-h);
                    dist = (float)Math.sqrt(dist)*1/Width;
                    if(dist < DB) {
                        byte fdist = (byte) (int) (127 - dist * (Width / 255.0f));
                        if (map[h * Width + w] < fdist) {
                            map[h * Width + w] = fdist;
                        }
                    }
                    else{
                        byte fdist = 50;
                        if (map[h * Width + w] < fdist) {
                            map[h * Width + w] = fdist;
                        }
                    }
                }
            }
        }
        return map;
    }
}
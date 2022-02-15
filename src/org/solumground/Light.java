package org.solumground;

class Light{
    public Vec3 position;
    public float Strength;
    public float Red;
    public float Green;
    public float Blue;

    public static int maxLights = 100;
    public static Light [] lights = new Light[maxLights];
    public static int lightCount = 0;

    public Light(Vec3 pos, float s, float r,float g,float b){
        this.position = new Vec3(pos);
        this.Strength = s;
        this.Red = r;
        this.Green = g;
        this.Blue = b;

        lights[lightCount] = this;
        lightCount++;
    }

    public static Vec4 getLight(Vec3 pos){
        Vec4 l = new Vec4();
        l.X = .2f;
        l.Y = .2f;
        l.Z = .2f;
        l.W = .3f;
        for(int X=0;X<lightCount;X++) {
            Light light = lights[X];
            float dist = (light.position.X - pos.X) * (light.position.X - pos.X) +
                    (light.position.Y - pos.Y) * (light.position.Y - pos.Y) +
                    (light.position.Z - pos.Z) * (light.position.Z - pos.Z);
            dist = (float)Math.sqrt(dist);
            dist /= 10;
            //dist = dist/(5*5);
            if(dist < 1){
                dist = 1;
            }
            if (dist > 0) {
                float S = light.Strength / dist;
                if(l.W < S){
                    l.W = S;
                }
                l.X += light.Red*S;
                l.Y += light.Green*S;
                l.Z += light.Blue*S;
            } else {
                l.W = light.Strength;
            }
        }

        return l;
    }
}
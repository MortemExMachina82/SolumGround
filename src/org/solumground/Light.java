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

    public static int MaxDistance = 250;

    public Light(Vec3 pos, float s, float r,float g,float b){
        this.position = new Vec3(pos);
        this.Strength = s;
        this.Red = r;
        this.Green = g;
        this.Blue = b;

        lights[lightCount] = this;
        lightCount++;
    }

    public static Vec3 getLight(Vec3 pos){
        Vec3 l = new Vec3(.7f,.7f,.7f);
        float LightStrength = .6f;
        for(int X=0;X<lightCount;X++) {
            Light light = lights[X];
            float dist = (light.position.X - pos.X) * (light.position.X - pos.X) +
                    (light.position.Y - pos.Y) * (light.position.Y - pos.Y) +
                    (light.position.Z - pos.Z) * (light.position.Z - pos.Z);
            if(dist > MaxDistance){
                continue;
            }
            //dist = (float)Math.sqrt(dist);
            //dist /= 10;
            float LocalStrength = 1-(dist/MaxDistance);
            if (LocalStrength > 0) {
                float S = light.Strength * LocalStrength;
                if (LightStrength < S) {
                    LightStrength = S;
                }
                l.X += light.Red * S;
                l.Y += light.Green * S;
                l.Z += light.Blue * S;
            }
        }
        l.X *= LightStrength;
        l.Y *= LightStrength;
        l.Z *= LightStrength;

        return l;
    }
}
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

    public static Vec3 getLight(Vec3 pos){
        Vec3 l = new Vec3(.2f,.2f,.2f);
        float lStrength = .3f;
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
                if(lStrength < S){
                    lStrength = S;
                }
                l.X += light.Red*S;
                l.Y += light.Green*S;
                l.Z += light.Blue*S;
            } else {
                lStrength = light.Strength;
            }
        }
        l.X *= lStrength;
        l.Y *= lStrength;
        l.Z *= lStrength;

        return l;
    }
}
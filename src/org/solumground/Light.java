package org.solumground;

import java.util.ArrayList;
import java.util.List;

class Light{
    public Vec3 position;
    public float Strength;
    public float Red;
    public float Green;
    public float Blue;

    public static List<Light> lights = new ArrayList<>(100);

    public static int MaxDistance = 250;

    public Light(Vec3 pos, float s, float r,float g,float b){
        this.position = new Vec3(pos);
        this.Strength = s;
        this.Red = r;
        this.Green = g;
        this.Blue = b;

        lights.add(this);
    }

    public static Vec3 getLight(Vec3 pos){
        Vec3 l = new Vec3(.2f,.2f,.2f);
        for(Light light : lights) {
            float dist = (light.position.X - pos.X) * (light.position.X - pos.X) +
                    (light.position.Y - pos.Y) * (light.position.Y - pos.Y) +
                    (light.position.Z - pos.Z) * (light.position.Z - pos.Z);
            if(dist > MaxDistance*light.Strength){
                continue;
            }
            float LocalStrength = 1-(dist/(MaxDistance*light.Strength));
            if (LocalStrength > 0) {
                float S = light.Strength * LocalStrength;
                if(light.Red*S > l.X) {
                    l.X = light.Red*S;
                }
                if(light.Green*S > l.Y) {
                    l.Y = light.Green*S;
                }
                if(light.Blue*S > l.Z) {
                    l.Z = light.Blue*S;
                }
            }
        }

        return l;
    }
}
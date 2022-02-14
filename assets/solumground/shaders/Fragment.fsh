#version 110

varying vec2 texcord;
varying vec3 light;

uniform int doubleDraw; 
uniform float scean;
uniform sampler2D tex;

void main(){
    if(doubleDraw == 1){
        if(mod(((gl_FragCoord.x-.5)+(gl_FragCoord.y-.5)), 2.0) == scean){
            discard;
        }
    }
    vec4 sample = texture2D(tex, texcord);

    if(sample.a == 0.0){
        discard;
    }
    sample.r = sample.r*light.r;
    sample.g = sample.g*light.g;
    sample.b = sample.b*light.b;
    

    gl_FragColor = sample;
}

#version 110

varying vec2 texcord;

uniform sampler2D tex;
uniform float Alpha;

void main(){
    vec4 sample = texture2D(tex, texcord);
    sample.a *= Alpha;

    if(sample.a == 0.0){
        discard;
    }
    gl_FragColor = sample;
}

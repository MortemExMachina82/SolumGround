#version 110

varying vec2 texcord;

uniform sampler2D tex;

void main(){
    vec4 sample = texture2D(tex, texcord);

    if(sample.a == 0.0){
        discard;
    }

    gl_FragColor = sample;
}

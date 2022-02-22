#version 110

attribute vec2 a_position;
attribute vec2 a_texcord;

varying vec2 texcord;

void main(){
    texcord = a_texcord;
    gl_Position = vec4(a_position, -0.5, 1.0);
}

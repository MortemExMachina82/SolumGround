#version 110

attribute vec2 a_position;
attribute vec2 a_texcord;

uniform vec2 TextPos;

varying vec2 texcord;

void main(){
    texcord = a_texcord;
    gl_Position = vec4(a_position.x+TextPos.x, a_position.y+TextPos.y, -0.5, 1.0);
}

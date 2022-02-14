#version 110

attribute vec3 a_position;
attribute vec2 a_texcord;
attribute vec3 a_light;


varying vec2 texcord;
varying vec3 light;


uniform mat4 rotation_l;
uniform mat4 rotation_g;
uniform mat4 translation;
uniform mat4 projection;

void main(){
    texcord = a_texcord;
    light = a_light;
    gl_Position = (projection*(rotation_g*(translation*(rotation_l*vec4(a_position, 1.0)))));
}

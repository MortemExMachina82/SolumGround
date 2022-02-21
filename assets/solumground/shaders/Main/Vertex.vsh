#version 110

attribute vec3 a_position;
attribute vec2 a_texcord;
attribute vec3 a_light;


varying vec2 texcord;
varying vec3 light;


uniform mat4 ModelMat;
uniform mat4 WorldMat;
uniform mat4 ProjectionMat;

void main(){
    texcord = a_texcord;
    light = a_light;
    gl_Position = (ProjectionMat*(WorldMat*(ModelMat*vec4(a_position, 1.0))));
}

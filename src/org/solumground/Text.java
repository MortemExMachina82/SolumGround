package org.solumground;

import static org.lwjgl.opengl.GL21.*;

public class Text {
    public Font font;
    public Vec3 position;
    public String SText;
    public byte [] text;
    public float Size;
    public float [] VertexArray;
    public int VertexBufferObject;

    public void BuildAndUploadVertexData(){
        this.VertexArray = new float[this.text.length*4*9];

        float offset = 0;
        for(int X=0;X<this.text.length;X++){
            float width = ((this.font.getWidth(this.text[X])/(float)this.font.Texture_width)*128*this.Size);
            float texWidth1 = this.font.getAcumWidth(this.text[X]-1)/(float)this.font.Texture_width;
            float texWidth2 = this.font.getAcumWidth(this.text[X])/(float)this.font.Texture_width;

            int arrayStart = X*4*9;
            this.VertexArray[arrayStart] = offset;
            this.VertexArray[arrayStart + 1] = 0;
            this.VertexArray[arrayStart + 2] = 0;
            this.VertexArray[arrayStart + 3] = texWidth1;
            this.VertexArray[arrayStart + 4] = 1;
            this.VertexArray[arrayStart + 5] = 1.0f;
            this.VertexArray[arrayStart + 6] = 1.0f;
            this.VertexArray[arrayStart + 7] = 1.0f;
            this.VertexArray[arrayStart + 8] = 1.0f;

            this.VertexArray[arrayStart + 9] = offset;
            this.VertexArray[arrayStart + 10] = this.Size;
            this.VertexArray[arrayStart + 11] = 0;
            this.VertexArray[arrayStart + 12] = texWidth1;
            this.VertexArray[arrayStart + 13] = 0;
            this.VertexArray[arrayStart + 14] = 1.0f;
            this.VertexArray[arrayStart + 15] = 1.0f;
            this.VertexArray[arrayStart + 16] = 1.0f;
            this.VertexArray[arrayStart + 17] = 1.0f;

            this.VertexArray[arrayStart + 18] = (offset + width);
            this.VertexArray[arrayStart + 19] = this.Size;
            this.VertexArray[arrayStart + 20] = 0;
            this.VertexArray[arrayStart + 21] = texWidth2;
            this.VertexArray[arrayStart + 22] = 0;
            this.VertexArray[arrayStart + 23] = 1.0f;
            this.VertexArray[arrayStart + 24] = 1.0f;
            this.VertexArray[arrayStart + 25] = 1.0f;
            this.VertexArray[arrayStart + 26] = 1.0f;

            this.VertexArray[arrayStart + 27] = (offset + width);
            this.VertexArray[arrayStart + 28] = 0;
            this.VertexArray[arrayStart + 29] = 0;
            this.VertexArray[arrayStart + 30] = texWidth2;
            this.VertexArray[arrayStart + 31] = 1;
            this.VertexArray[arrayStart + 32] = 1.0f;
            this.VertexArray[arrayStart + 33] = 1.0f;
            this.VertexArray[arrayStart + 34] = 1.0f;
            this.VertexArray[arrayStart + 35] = 1.0f;

            offset += width;
        }
        glBindBuffer(GL_ARRAY_BUFFER, this.VertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, this.VertexArray, GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(Main.shader_vertex_position);
        glEnableVertexAttribArray(Main.shader_vtcord_position);
    }

    public Text(String newtext, Font font, float Size, Vec3 pos){
        this.text = newtext.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.SText = newtext;
        this.font = font;
        this.Size = Size*2;
        this.position = pos;

        this.VertexBufferObject = glGenBuffers();

        BuildAndUploadVertexData();
    }
    public void updateText(String newText){
        if(this.SText.equals(newText)){
            return;
        }
        this.SText = newText;
        byte [] newTextByte = newText.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if(newTextByte == this.text){
            return;
        }
        this.text = newTextByte;
        BuildAndUploadVertexData();
    }
    public void render(){
        float[] mat = new float[4 * 4];
        mat[0] = 1;
        mat[5] = 1;
        mat[10] = 1;
        mat[15] = 1;

        mat[12] = this.position.X*Main.aspectRatio;
        mat[13] = this.position.Y - this.Size;
        mat[14] = -1;


        glUniformMatrix4fv(Main.shader_translation_position, false, mat);

        mat[12] = 0;
        mat[13] = 0;
        mat[14] = 0;
        glUniformMatrix4fv(Main.shader_rotation_g_position, false, mat);
        glUniformMatrix4fv(Main.shader_rotation_l_position, false, mat);



        glBindBuffer(GL_ARRAY_BUFFER, this.VertexBufferObject);
        glBindTexture(GL_TEXTURE_2D, font.Texture_Buffer_Object);

        glVertexAttribPointer(Main.shader_vertex_position, 3, GL_FLOAT, false, 4 * 9, 0);
        glVertexAttribPointer(Main.shader_vtcord_position, 2, GL_FLOAT, false, 4 * 9, 4 * 3);
        glVertexAttribPointer(Main.shader_light_position, 4, GL_FLOAT, false, 9 * 4, 3*4 + 2*4);

        glDisable(GL_DEPTH_TEST);
        glDrawArrays(GL_QUADS, 0, this.text.length*4);
        glEnable(GL_DEPTH_TEST);

        glUniformMatrix4fv(Main.shader_rotation_g_position, false, Player.RotationMatrix);
    }
}









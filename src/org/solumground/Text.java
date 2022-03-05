package org.solumground;

import static org.lwjgl.opengl.GL21.*;

public class Text {
    public Font font;
    public Vec3 position;
    public String SText;
    public byte [] text;
    public float Size;
    public float Alpha = 1;
    public float [] VertexArray;
    public int VertexBufferObject;

    public void BuildAndUploadVertexData(){
        this.VertexArray = new float[this.text.length*16];

        float offsetX = 0;
        float offsetY = 0;
        for(int X=0;X<this.text.length;X++){
            if(this.text[X] == '\n'){
                offsetY -= Size;
                offsetX = 0;
                continue;
            }
            if(this.text[X] == '\r'){
                offsetX = 0;
            }
            float width = ((this.font.getWidth(this.text[X])/(float)this.font.Texture_width)*128*this.Size)/Main.aspectRatio;
            float texWidth1 = this.font.getAcumWidth(this.text[X]-1)/(float)this.font.Texture_width;
            float texWidth2 = this.font.getAcumWidth(this.text[X])/(float)this.font.Texture_width;

            int arrayStart = X*16;
            this.VertexArray[arrayStart] = offsetX;
            this.VertexArray[arrayStart + 1] = offsetY - this.Size;
            this.VertexArray[arrayStart + 2] = texWidth1;
            this.VertexArray[arrayStart + 3] = 1;

            this.VertexArray[arrayStart + 4] = offsetX;
            this.VertexArray[arrayStart + 5] = offsetY;
            this.VertexArray[arrayStart + 6] = texWidth1;
            this.VertexArray[arrayStart + 7] = 0;

            this.VertexArray[arrayStart + 8] = offsetX + width;
            this.VertexArray[arrayStart + 9] = offsetY;
            this.VertexArray[arrayStart + 10] = texWidth2;
            this.VertexArray[arrayStart + 11] = 0;

            this.VertexArray[arrayStart + 12] = offsetX + width;
            this.VertexArray[arrayStart + 13] = offsetY - this.Size;
            this.VertexArray[arrayStart + 14] = texWidth2;
            this.VertexArray[arrayStart + 15] = 1;

            offsetX += width;
        }
        glBindBuffer(GL_ARRAY_BUFFER, this.VertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, this.VertexArray, GL_DYNAMIC_DRAW);
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
        this.text = newText.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        BuildAndUploadVertexData();
    }
    public void render(){
        glBindBuffer(GL_ARRAY_BUFFER, this.VertexBufferObject);
        glBindTexture(GL_TEXTURE_2D, font.Texture_Buffer_Object);

        glVertexAttribPointer(Main.TextShader_Vertex, 2, GL_FLOAT, false, 16, 0);
        glVertexAttribPointer(Main.TextShader_TextureCords, 2, GL_FLOAT, false, 16, 8);

        glUniform2f(Main.TextShader_TextPos, -this.position.X, this.position.Y);
        glUniform1f(Main.TextShader_Alpha, this.Alpha);

        glDrawArrays(GL_QUADS, 0, this.text.length*4);
    }
}









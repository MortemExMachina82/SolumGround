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
    public float NormalizedTotalHight;
    public float NormalizedTotalWidth;

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
        NormalizedTotalHight = offsetY;
        NormalizedTotalWidth = offsetX;

        if(Thread.currentThread().getName().equals("main")){
            glBindBuffer(GL_ARRAY_BUFFER, this.VertexBufferObject);
            glBufferData(GL_ARRAY_BUFFER, VertexArray, GL_DYNAMIC_DRAW);
        }
        else{

            while(Main.glBufferDataStatus != Main.GLStatus.Done){
                try {
                    Thread.sleep(1);
                }
                catch(Exception e1){
                    e1.printStackTrace();
                    return;
                }
            }
            Main.glBindBuffer_In1 = GL_ARRAY_BUFFER;
            Main.glBindBuffer_In2 = this.VertexBufferObject;

            Main.glBufferData_In1 = GL_ARRAY_BUFFER;
            Main.glBufferData_In2 = VertexArray;
            Main.glBufferData_In3 = GL_STATIC_DRAW;
            Main.glBufferDataStatus = Main.GLStatus.Ready;
            while(Main.glBufferDataStatus != Main.GLStatus.Done){
                try {
                    Thread.sleep(1);
                }
                catch(Exception e1){
                    e1.printStackTrace();
                    return;
                }
            }
        }
    }

    public Text(String newtext, Font font, float Size, Vec3 pos){
        this.text = newtext.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.SText = newtext;
        this.font = font;
        this.Size = Size*2;
        this.position = pos;

        if(Thread.currentThread().getName().equals("main")) {
            this.VertexBufferObject = glGenBuffers();
        }
        else {
            while (Main.glGenBuffersStatus != Main.GLStatus.Done) {
                try {
                    Thread.sleep(1);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return;
                }
            }
            Main.glGenBuffersStatus = Main.GLStatus.Ready;
            while (Main.glGenBuffersStatus != Main.GLStatus.Done) {
                try {
                    Thread.sleep(1);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return;
                }
            }
            this.VertexBufferObject = Main.glGenBuffers_Out;
        }


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

        glVertexAttribPointer(Main.TwoDShader_Vertex, 2, GL_FLOAT, false, 16, 0);
        glVertexAttribPointer(Main.TwoDShader_TextureCords, 2, GL_FLOAT, false, 16, 8);

        glUniform2f(Main.TwoDShader_TextPos, -this.position.X, this.position.Y);
        glUniform1f(Main.TwoDShader_Alpha, this.Alpha);

        glDrawArrays(GL_QUADS, 0, this.text.length*4);
    }
}









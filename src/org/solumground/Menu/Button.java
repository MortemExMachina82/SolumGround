package org.solumground.Menu;

import org.solumground.*;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Button {
    public float PosX;
    public float PosY;
    public float SizeX;
    public float SizeY;
    public Text text;
    public int OnPress;
    public String BackGroundTexture;
    public int TextureBufferObject;
    public static int VertexBufferObject = glGenBuffers();



    public Button(float X, float Y, float Sx, float Sy, String BackTex, String text, int OnPress){
        this.PosX = X;
        this.PosY = Y;
        this.SizeX = Sx*2;
        this.SizeY = Sy*2;
        this.text = new Text(text, Main.DefaultFont, this.SizeY*.15f, new Vec3(0, 0, 0));
        this.text.position.X = this.PosX + (this.text.NormalizedTotalWidth/2);
        this.text.position.Y = this.PosY + this.SizeY*.1f;
        this.OnPress = OnPress;
        this.BackGroundTexture = BackTex;
        this.TextureBufferObject = Main.LoadTexture(this.BackGroundTexture, 0x3F3F3FFF);
    }
    public boolean TestOver(){
        float NormalizedX = (float)((Main.win_X*.5 - Main.mouse_past_X)/Main.win_X*2);
        float NormalizedY = (float)((Main.win_Y*.5 - Main.mouse_past_Y)/Main.win_Y*2);
        if(NormalizedX > this.PosX - (this.SizeX/2)){
            if(NormalizedX < this.PosX + (this.SizeX/2)){
                if(NormalizedY > this.PosY - (this.SizeY/2)){
                    if(NormalizedY < this.PosY + (this.SizeY/2)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void Draw(){
        glUseProgram(Main.TwoDShaderProgram);
        float [] VertexArray = new float[16];
        VertexArray[0] = this.SizeX;
        VertexArray[1] = this.SizeY;
        VertexArray[2] = 0;
        VertexArray[3] = 0;

        VertexArray[4] = this.SizeX;
        VertexArray[5] = 0;
        VertexArray[6] = 1;
        VertexArray[7] = 0;

        VertexArray[8] = 0;
        VertexArray[9] = 0;
        VertexArray[10] = 1;
        VertexArray[11] = 1;

        VertexArray[12] = 0;
        VertexArray[13] = this.SizeY;
        VertexArray[14] = 0;
        VertexArray[15] = 1;
        glBindBuffer(GL_ARRAY_BUFFER, VertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, VertexArray, GL_DYNAMIC_DRAW);

        glBindTexture(GL_TEXTURE_2D, this.TextureBufferObject);


        glVertexAttribPointer(Main.TwoDShader_Vertex, 2, GL_FLOAT, false, 16, 0);
        glVertexAttribPointer(Main.TwoDShader_TextureCords, 2, GL_FLOAT, false, 16, 8);

        glUniform2f(Main.TwoDShader_TextPos, this.PosX-(this.SizeX/2), this.PosY-(this.SizeY/2));
        glUniform1f(Main.TwoDShader_Alpha, 1);

        glDrawArrays(GL_QUADS, 0, 4);

        text.render();
    }
}

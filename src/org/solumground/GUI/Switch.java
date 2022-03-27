package org.solumground.GUI;

import org.solumground.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform1f;

public class Switch {
    public float PosX;
    public float PosY;
    public float SizeX;
    public float SizeY;
    public Text TrueText;
    public Text FalseText;
    public Script OnSetTrue;
    public Script OnSetFalse;
    public int TextureBufferObject;
    public static int VertexBufferObject = glGenBuffers();

    public boolean Value;

    public Switch(float Xp,float Yp, float Xs,float Ys, String OnText, String OffText, String Texture, String OnTrueScript, String OnFalseScript){
        this.PosX = Xp;
        this.PosY = Yp;
        this.SizeX = Xs*2;
        this.SizeY = Ys*2;
        this.TrueText = new Text(OnText, Main.DefaultFont, this.SizeY*.15f, new Vec3(0, 0, 0));
        this.TrueText.position.X = this.PosX + (this.TrueText.NormalizedTotalWidth/2);
        this.TrueText.position.Y = this.PosY + this.SizeY*.1f;
        this.FalseText = new Text(OffText, Main.DefaultFont, this.SizeY*.15f, new Vec3(0, 0, 0));
        this.FalseText.position.X = this.PosX + (this.FalseText.NormalizedTotalWidth/2);
        this.FalseText.position.Y = this.PosY + this.SizeY*.1f;
        this.OnSetTrue = new Script(OnTrueScript);
        this.OnSetFalse = new Script(OnFalseScript);
        this.TextureBufferObject = Main.LoadTexture(Main.jar_folder_path+"/"+Texture, 0x3F3F3FFF);
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
    public void Toggle(){
        if(this.Value){
            this.Value = false;
            this.OnSetFalse.Run();
        }
        else{
            this.Value = true;
            this.OnSetTrue.Run();
        }
    }
    public void Draw(){
        glUseProgram(Main.TwoDShaderProgram);
        float [] VertexArray = new float[16];
        VertexArray[0] = this.SizeX;
        VertexArray[1] = this.SizeY;
        VertexArray[4] = this.SizeX;
        VertexArray[6] = 1;
        VertexArray[10] = 1;
        VertexArray[11] = 1;
        VertexArray[13] = this.SizeY;
        VertexArray[15] = 1;
        glBindBuffer(GL_ARRAY_BUFFER, VertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, VertexArray, GL_DYNAMIC_DRAW);
        glBindTexture(GL_TEXTURE_2D, this.TextureBufferObject);

        glVertexAttribPointer(Main.TwoDShader_Vertex, 2, GL_FLOAT, false, 16, 0);
        glVertexAttribPointer(Main.TwoDShader_TextureCords, 2, GL_FLOAT, false, 16, 8);

        glUniform2f(Main.TwoDShader_TextPos, (-this.PosX)-(this.SizeX/2), this.PosY-(this.SizeY/2));
        glUniform1f(Main.TwoDShader_Alpha, 1);

        glDrawArrays(GL_QUADS, 0, 4);

        if(Value){
            this.TrueText.render();
        }
        else{
            this.FalseText.render();
        }
    }
}

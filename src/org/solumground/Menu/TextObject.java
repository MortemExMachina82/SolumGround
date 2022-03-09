package org.solumground.Menu;

import org.solumground.Main;
import org.solumground.Text;
import org.solumground.Vec3;

public class TextObject {
    public float PosX;
    public float PosY;
    public float Size;
    public Text text;

    public TextObject(float X, float Y,float S,  String Value){
        this.PosX = X;
        this.PosY = Y;
        this.Size = S;
        this.text = new Text(Value, Main.DefaultFont, this.Size, new Vec3(this.PosX, this.PosY, 0));
        this.text.position.X = this.PosX + (this.text.NormalizedTotalWidth/2);
        this.text.position.Y = this.PosY + (this.text.NormalizedTotalHight/2);
    }

    public void Draw(){
        this.text.render();
    }
}

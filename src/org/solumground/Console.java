package org.solumground;

import java.util.ArrayList;
import java.util.List;


public class Console {
    public static Font DefaultFont;
    public static int BufferSize = 10;
    public static float DeSpawnTime = 10.0f;
    public static float TextSize = 0.06f;
    public static List<Text> TextBuffer = new ArrayList<>(BufferSize+1);
    public static List<Float> TimeBuffer = new ArrayList<>(BufferSize+1);

    public static void Init(){
        DefaultFont = new Font(Main.jar_folder_path+"/"+Main.FontPath);
    }

    public static void Print(String text){
        for(Text text1 : TextBuffer){
            text1.position.Y += TextSize+0.02f;
        }
        Text T = new Text(text, DefaultFont, TextSize/2, new Vec3(1-0.05f, -1+0.1f+TextSize, 0));
        TextBuffer.add(T);
        TimeBuffer.add(Main.Time);
        if(TextBuffer.size() > BufferSize){
            TextBuffer.remove(0);
            TimeBuffer.remove(0);
        }
    }

    public static void Add(String text){
        Text T = TextBuffer.get(TextBuffer.size()-1);
        T.updateText(T.SText+text);
        TimeBuffer.set(TimeBuffer.size()-1, Main.Time);
    }

    public static void Draw(){
        for(int X=0;X<TextBuffer.size();X++){
            Text text = TextBuffer.get(X);
            float Ttime = TimeBuffer.get(X);
            float Ctime = Main.Time;
            float TimeElapsed = Ctime-Ttime;
            if(TimeElapsed <= .5f){
                text.Alpha = TimeElapsed*2;
            }
            if(TimeElapsed >= DeSpawnTime-.5f){
                text.Alpha = (DeSpawnTime-TimeElapsed)*2;
            }
            if(Ctime-Ttime > DeSpawnTime){
                TextBuffer.remove(X);
                TimeBuffer.remove(X);
                X -= 1;
                continue;
            }
            text.render();
        }
    }
}

package org.solumground;

import org.solumground.GUI.Page;

import java.lang.*;
import java.util.ArrayList;
import java.util.List;

public class Script {
    public List<Object> Lines = new ArrayList<>(0);
    public static void SetVar(String name, Object value) {
        switch (name) {
            case "Main.jar_folder_path":
                Main.RootDir = (String) value;
                break;
            case "Main.SaveFolder":
                Main.SaveFolder = (String) value;
                break;
            case "Main.FontPath":
                Main.FontPath = (String) value;
                break;
            case "Main.FOV":
                Main.FOV = Float.parseFloat((String)value);
                break;
            case "Main.nearPlane":
                Main.nearPlane = Float.parseFloat((String)value);
                break;
            case "Main.farPlane":
                Main.farPlane = Float.parseFloat((String)value);
                break;
            case "Main.win_X":
                Main.win_X = Integer.parseInt((String)value);
                break;
            case "Main.win_Y":
                Main.win_Y = Integer.parseInt((String)value);
                break;
            case "Main.OriginalWinX":
                Main.OriginalWinX = Integer.parseInt((String)value);
                break;
            case "Main.OriginalWinY":
                Main.OriginalWinY = Integer.parseInt((String)value);
                break;
            case "Main.aspectRatio":
                Main.aspectRatio = Float.parseFloat((String)value);
                break;
            case "Main.FullScreen":
                Main.FullScreen = Boolean.parseBoolean((String)value);
                break;
            case "Main.Time":
                Main.Time = Float.parseFloat((String)value);
                break;
            case "Main.TimeElapsed":
                Main.TimeElapsed = Float.parseFloat((String)value);
                break;
            case "Main.AvgTimeElapsed":
                Main.AvgTimeElapsed = Float.parseFloat((String)value);
                break;
            case "Main.fps":
                Main.fps = Float.parseFloat((String)value);
                break;
            case "Main.RenderDistance":
                Main.RenderDistance = Integer.parseInt((String)value);
                break;
            case "Main.showCollisionBox":
                Main.showCollisionBox = Boolean.parseBoolean((String)value);
                break;
            case "Main.DrawSkyBox":
                Main.DrawSkyBox = Boolean.parseBoolean((String)value);
                break;
            case "Main.playerMoveSpeed":
                Main.playerMoveSpeed = Float.parseFloat((String)value);
                break;
            case "Main.playerMoveSpeedSprint":
                Main.playerMoveSpeedSprint = Float.parseFloat((String)value);
                break;
            case "Main.playerRotateSpeedKey":
                Main.playerRotateSpeedKey = Float.parseFloat((String)value);
                break;
            case "Main.playerRotateSpeedMouse":
                Main.playerRotateSpeedMouse = Float.parseFloat((String)value);
                break;
            default:
                System.out.println("Variable Not Found: "+name);
        }
    }
    public static Object GetVar(String name){
        switch (name) {
            case "Main.jar_folder_path":
                return Main.RootDir;
            case "Main.SaveFolder":
                return Main.SaveFolder;
            case "Main.FontPath":
                return Main.FontPath;
            case "Main.FOV":
                return Main.FOV;
            case "Main.nearPlane":
                return Main.nearPlane;
            case "Main.farPlane":
                return Main.farPlane;
            case "Main.win_X":
                return Main.win_X;
            case "Main.win_Y":
                return Main.win_Y;
            case "Main.OriginalWinX":
                return Main.OriginalWinX;
            case "Main.OriginalWinY":
                return Main.OriginalWinY;
            case "Main.aspectRatio":
                return Main.aspectRatio;
            case "Main.FullScreen":
                return Main.FullScreen;
            case "Main.Time":
                return Main.Time;
            case "Main.TimeElapsed":
                return Main.TimeElapsed;
            case "Main.AvgTimeElapsed":
                return Main.AvgTimeElapsed;
            case "Main.fps":
                return Main.fps;
            case "Main.RenderDistance":
                return Main.RenderDistance;
            case "Main.showCollisionBox":
                return Main.showCollisionBox;
            case "Main.DrawSkyBox":
                return Main.DrawSkyBox;
            case "Main.playerMoveSpeed":
                return Main.playerMoveSpeed;
            case "Main.playerMoveSpeedSprint":
                return Main.playerMoveSpeedSprint;
            case "Main.playerRotateSpeedKey":
                return Main.playerRotateSpeedKey;
            case "Main.playerRotateSpeedMouse":
                return Main.playerRotateSpeedMouse;
            default:
                System.out.println("Variable Not Found: " + name);
        }
        return null;
    }
    public static void CallFunc(String name, String [] params){
        switch(name){
            case "Main.update_fullscreen()":
                Main.update_fullscreen();
                break;
            case "Main.LoadSettings()":
                Main.LoadSettings();
                break;
            case "Main.SaveSettings()":
                Main.SaveSettings();
                break;
            case "Main.MakeGLCalls()":
                Main.MakeGLCalls();
                break;
            case "Main.TakeScreenShot()":
                Main.TakeScreenShot();
                break;
            case "Page.SetPage()":
                Page.SetPage(Integer.parseInt(params[0]));
                break;
            case "Main.UnloadWorld()":
                Main.UnloadWorld();
                break;
            case "Console.Print()":
                Console.Print(params[0]);
            default:
                System.out.println("Function Not Found: "+name);
        }
    }
    public Script(String sc){
        String [] lines = sc.split(";");
        for(String Line : lines){
            String first = "";
            boolean VarSet = false;
            boolean FuncSet = false;
            int offset = 0;
            for(int X=0;X<Line.length();X++) {
                char C = Line.charAt(X);
                if(C != ' ' && C != '\n' && C != '\t'){
                    if(C == '='){
                        VarSet = true;
                        offset = X+1;
                        break;
                    }
                    if(C == '('){
                        FuncSet = true;
                        offset = X+1;
                        first += "()";
                        break;
                    }
                    first += C;
                }
            }
            if(VarSet){
                String second = "";
                for(int X=offset;X<Line.length();X++) {
                    char C = Line.charAt(X);
                    if(C != ' ' && C != '\n' && C != '\t'){
                        second += C;
                    }
                }
                Lines.add(first);
                Lines.add(second);
                Lines.add("VarSet");
            }
            if(FuncSet){
                String [] array = new String[10];
                int arrayCount = 0;
                array[arrayCount] = "";
                for(int X=offset;X<Line.length();X++) {
                    char C = Line.charAt(X);
                    if(C == ','){
                        arrayCount++;
                        array[arrayCount] = "";
                        continue;
                    }
                    if(C != ' ' && C != '\n' && C != '\t'){
                        if(C == ')'){
                            break;
                        }
                        array[arrayCount] += C;
                    }
                }
                Lines.add(first);
                Lines.add(array);
                Lines.add("FuncCall");
            }
        }
    }
    public void Run(){
        for(int X = 0; X< Lines.size(); X+=3){
            String first = (String)Lines.get(X);
            if(Lines.get(X + 2).equals("VarSet")) {
                SetVar(first, Lines.get(X + 1));
            }
            else{
                CallFunc(first, (String[])Lines.get(X + 1));
            }
        }
    }
}
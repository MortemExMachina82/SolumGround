package org.solumground;

import org.solumground.GUI.Page;

import java.lang.*;
import java.util.ArrayList;
import java.util.List;

public class Script {
    public List<String> Lines = new ArrayList<>(0);
    public static void SetVar(String name, Object value) {
        switch (name) {
            case "Main.jar_folder_path":
                Main.jar_folder_path = (String) value;
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
    public static void CallFunc(String name, Object... params){
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
                Page.SetPage(Integer.parseInt((String)params[0]));
                break;
        }
    }
    public Script(String sc){
        String [] lines = sc.split(";");
        for(String Line : lines){
            String first = "";
            String second = "";
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
                for(int X=offset;X<Line.length();X++) {
                    char C = Line.charAt(X);
                    if(C != ' ' && C != '\n' && C != '\t'){
                        second += C;
                    }
                }
            }
            if(FuncSet){
                for(int X=offset;X<Line.length();X++) {
                    char C = Line.charAt(X);
                    if(C != ' ' && C != '\n' && C != '\t'){
                        if(C == ')'){
                            break;
                        }
                        second += C;
                    }
                }
            }
            if(VarSet){
                Lines.add(first);
                Lines.add(second);
                Lines.add("VarSet");
            }
            else{
                Lines.add(first);
                Lines.add(second);
                Lines.add("FuncCall");
            }
        }
    }
    public void Run(){
        for(int X = 0; X< Lines.size(); X+=3){
            String first = Lines.get(X);
            String second = Lines.get(X + 1);
            if(Lines.get(X + 2).equals("VarSet")) {
                SetVar(first, second);
            }
            else{
                CallFunc(first, second);
            }
        }
    }
}
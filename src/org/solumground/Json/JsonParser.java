package org.solumground.Json;

import java.io.*;

public class JsonParser {
    public String FilePath;
    public JsonObject mainJsonObject;

    public JsonItem ItemNull = new JsonItem(null);

    public static int SkipWhiteSpace(FileInputStream In, JsonObject O) throws Exception{
        for (int X = 0; X < 100; X++) {
            int Char = In.read();
            if (Char != ' ' && Char != '\t' && Char != '\n' && Char != '\r') {
                if(Char == -1){
                    throw new Exception("Reached EOF: "+O.jsonParser.FilePath+"."+O.GetParentTree());
                }
                else {
                    return Char;
                }
            }
        }
        throw new Exception(O.GetParentTree());
    }
    public static int ToHex(int HexValue, JsonObject O) throws Exception{
        if(HexValue >= '0' && HexValue <= '9'){
            return HexValue;
        }
        else{
            if(HexValue >= 'a' && HexValue <= 'f'){
                return HexValue-87;
            }
            if(HexValue >= 'A' && HexValue <= 'F'){
                return HexValue-55;
            }
            throw new Exception("Cant Parse Hex Value: "+O.jsonParser.FilePath+"."+O.GetParentTree());
        }
    }
    public static String ParseString(FileInputStream In, JsonObject O) throws Exception{
        String Out = "";
        for(int X=0;X<500;X++){
            int Char = In.read();
            if(Char == '"'){
                return Out;
            }
            if(Char == '\\'){
                int NextChar = In.read();
                switch(NextChar){
                    case '"':
                        Out += "\"";
                    case '\\':
                        Out += '\\';
                    case '/':
                        Out += '/';
                    case 'b':
                        Out += '\b';
                    case 'f':
                        Out += '\f';
                    case 'n':
                        Out += '\n';
                    case 'r':
                        Out += '\r';
                    case 't':
                        Out += '\t';
                    case 'u':
                        int V = 0;
                        NextChar = In.read();
                        V += ToHex(NextChar, O)<<24;
                        NextChar = In.read();
                        V += ToHex(NextChar, O)<<16;
                        NextChar = In.read();
                        V += ToHex(NextChar, O)<<8;
                        NextChar = In.read();
                        V += ToHex(NextChar, O);
                        Out += String.format("%c", V);
                    default:
                        throw new Exception("Invalid Escape Character At: "+O.jsonParser.FilePath+"."+O.GetParentTree());
                }
            }
            else{
                Out += String.format("%c", Char);
            }

        }
        return Out;
    }
    public static boolean ParseValue(FileInputStream In, JsonItem I, JsonObject O) throws Exception{
        boolean ReturnStatus = false;
        int Char = JsonParser.SkipWhiteSpace(In, O);
        if(Char == '"') {
            I.type = JsonItem.Type.String;
            I.ValueString = ParseString(In, O);
            Char = SkipWhiteSpace(In, O);
            return Char == ']' || Char == '}';
        }
        if(Char == '{'){
            I.type = JsonItem.Type.Object;
            I.valueJsonObject = new JsonObject(O.jsonParser, In, I.Parent, I.Name);
            return false;
        }
        if(Char == '['){
            I.type = JsonItem.Type.Array;
            I.valueJsonArray = new JsonArray(In, I.Parent, I.Name);
            return false;
        }
        if(Char == 't'){
            boolean IsTrue = false;
            if(In.read() == 'r'){
                if(In.read() == 'u'){
                    if(In.read() == 'e'){
                        IsTrue = true;
                    }
                }
            }
            if(IsTrue){
                I.type = JsonItem.Type.Bool;
                I.ValueBoolean = true;
                return false;
            }
            else{
                throw new Exception("Invalid Syntax: "+O.jsonParser.FilePath+"."+O.GetParentTree());
            }
        }
        if(Char == 'f'){
            boolean IsFalse = false;
            if(In.read() == 'a'){
                if(In.read() == 'l'){
                    if(In.read() == 's'){
                        if(In.read() == 'e'){
                            IsFalse = true;
                        }
                    }
                }
            }
            if(IsFalse){
                I.type = JsonItem.Type.Bool;
                I.ValueBoolean = false;
                return false;
            }
            else{
                throw new Exception("Invalid Syntax: "+O.jsonParser.FilePath+"."+O.GetParentTree());
            }
        }
        if(Char == 'n'){
            boolean IsNull = false;
            if(In.read() == 'u'){
                if(In.read() == 'l'){
                    if(In.read() == 'l'){
                        IsNull = true;
                    }
                }
            }
            if(IsNull){
                I.type = JsonItem.Type.Null;
                I.ValueNull = true;
                return false;
            }
            else{
                throw new Exception("Invalid Syntax: "+O.jsonParser.FilePath+"."+O.GetParentTree());
            }
        }
        if(Char == ']'){
            I.Exists = false;
            return true;
        }
        if(Char == ','){
            I.Exists = false;
            return false;
        }

        String value = "";
        value += String.format("%c", Char);
        boolean IsFloat = false;
        for(int X=0;X<100;X++){
            Char = SkipWhiteSpace(In, O);
            if(Char == ','){
                break;
            }
            if(Char == '.'){
                IsFloat = true;
            }
            if(Char == '}'){
                ReturnStatus = true;
                break;
            }
            if(Char == ']'){
                ReturnStatus = true;
                break;
            }

            value += String.format("%c", Char);

        }
        if(value.length() > 0) {
            if (IsFloat) {
                I.type = JsonItem.Type.Float;
                I.ValueFloat = Float.parseFloat(value);
            } else {
                I.type = JsonItem.Type.Long;
                I.ValueLong = Long.parseLong(value);
            }
        }
        return ReturnStatus;

    }

    public JsonParser(String FilePath) throws Exception{
        this.FilePath = FilePath;
        FileInputStream In;
        In = new FileInputStream(FilePath);
        int First = SkipWhiteSpace(In, null);
        if(First != '{'){
            throw new Exception("Cant Read File Contents: "+FilePath);
        }
        mainJsonObject = new JsonObject(this, In, null, "");

    }
}

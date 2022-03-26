package org.solumground.Json;


import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class JsonItem {
    public String Name;
    public JsonObject Parent;

    public enum Type{
        String,
        Object,
        Float,
        Long,
        Array,
        Bool,
        Null,
    }
    public Type type = Type.Null;

    public String ValueString;
    public JsonObject valueJsonObject;
    public Float ValueFloat;
    public long ValueLong;
    public JsonArray valueJsonArray;
    public boolean ValueBoolean;
    public boolean ValueNull;

    public boolean Exists = true;

    public String GetString(){
        switch(this.type){
            case String:
                return this.ValueString;
            case Float:
                return String.valueOf(this.ValueFloat);
            case Long:
                return String.valueOf(this.ValueLong);
            case Bool:
                return String.valueOf(this.ValueBoolean);
            case Null:
                return "";
            case Object:
                return "O_"+this.valueJsonObject.Name;
            case Array:
                return "A_"+this.valueJsonArray.Name;
        }
        return null;
    }
    public Float GetFloat() throws Exception{
        if(this.type == Type.Float){
            return this.ValueFloat;
        }
        if(this.type == Type.Long){
            return (float)this.ValueLong;
        }
        if(this.type == Type.String){
            return Float.parseFloat(this.ValueString);
        }
        throw new Exception("Tried To Get Float But Incompatible Type Found: "+this.Parent.Parent.jsonParser.FilePath+"."+this.Parent.GetParentTree());
    }
    public int GetInt() throws Exception{
        if(this.type == Type.Long){
            return (int)this.ValueLong;
        }
        if(this.type == Type.Float){
            return this.ValueFloat.intValue();
        }
        if(this.type == Type.String){
            return Integer.parseInt(this.ValueString);
        }
        throw new Exception("Tried To Get Int But Incompatible Type Found: "+this.Parent.Parent.jsonParser.FilePath+"."+this.Parent.GetParentTree());

    }
    public boolean GetBoolean() throws Exception{
        switch (this.type){
            case Bool:
                return this.ValueBoolean;
            case String:
                return Boolean.parseBoolean(this.ValueString);
            case Null:
                return false;
            default:
                throw new Exception("Tried To Get Boolean But Incompatible Type Found: "+this.Parent.Parent.jsonParser.FilePath+"."+this.Parent.GetParentTree());
        }
    }
    public JsonArray GetArray(){
        return this.valueJsonArray;
    }
    public JsonObject GetObject(){
            return this.valueJsonObject;
    }

    public JsonItem(){}
    public JsonItem(String s){
        this.type = Type.String;
        this.ValueString = s;
    }
    public JsonItem(float f){
        this.type = Type.Float;
        this.ValueFloat = f;
    }
    public JsonItem(int i){
        this.type = Type.Long;
        this.ValueLong = i;
    }
    public JsonItem(boolean b){
        this.type = Type.Bool;
        this.ValueBoolean = b;
    }
    public JsonItem(JsonArray arr){
        this.type = Type.Array;
        this.valueJsonArray = arr;
    }
    public JsonItem(JsonObject obj){
        this.type = Type.Object;
        this.valueJsonObject = obj;
    }
    public JsonItem(String name, String s){
        this.Name = name;
        this.type = Type.String;
        this.ValueString = s;
    }
    public JsonItem(String name, float f){
        this.Name = name;
        this.type = Type.Float;
        this.ValueFloat = f;
    }
    public JsonItem(String name, int i){
        this.Name = name;
        this.type = Type.Long;
        this.ValueLong = i;
    }
    public JsonItem(String name, boolean b){
        this.Name = name;
        this.type = Type.Bool;
        this.ValueBoolean = b;
    }
    public JsonItem(String name, JsonArray arr){
        this.Name = name;
        this.type = Type.Array;
        this.valueJsonArray = arr;
    }
    public JsonItem(String name, JsonObject obj){
        this.Name = name;
        this.type = Type.Object;
        this.valueJsonObject = obj;
    }

    public void write(FileOutputStream Out) throws Exception{
        if(this.Name != null) {
            Out.write('"');
            Out.write(this.Name.getBytes(StandardCharsets.UTF_8));
            Out.write('"');
            Out.write(':');
        }
        switch(this.type){
            case String:
                Out.write('"');
                Out.write(this.ValueString.getBytes(StandardCharsets.UTF_8));
                Out.write('"');
                break;
            case Float:
                Out.write(this.ValueFloat.toString().getBytes(StandardCharsets.UTF_8));
                break;
            case Long:
                Out.write(String.valueOf(ValueLong).getBytes(StandardCharsets.UTF_8));
                break;
            case Bool:
                Out.write(String.valueOf(this.ValueBoolean).getBytes(StandardCharsets.UTF_8));
                break;
            case Array:
                this.valueJsonArray.write(Out);
                break;
            case Object:
                this.valueJsonObject.write(Out);
                break;
        }
    }
}

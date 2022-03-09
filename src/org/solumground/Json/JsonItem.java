package org.solumground.Json;


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
        if(this.type == Type.Bool){
            return this.ValueBoolean;
        }
        if(this.type == Type.String){
            return Boolean.parseBoolean(this.ValueString);
        }
        throw new Exception("Tried To Get Boolean But Incompatible Type Found: "+this.Parent.Parent.jsonParser.FilePath+"."+this.Parent.GetParentTree());
    }
    public JsonArray GetArray(){
        return this.valueJsonArray;
    }
    public JsonObject GetObject(){
            return this.valueJsonObject;
    }

    public JsonItem(JsonObject parent){
        this.Parent = parent;
    }
}

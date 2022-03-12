package org.solumground.Json;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonObject {
    public JsonParser jsonParser;
    public String Name;
    public JsonObject Parent;



    public List<JsonItem> jsonItems = new ArrayList<>(10);

    public String GetParentTree() {
        return GetParentRecursive(this.Parent, this.Name);
    }
    private String GetParentRecursive(JsonObject O, String S) {
        if (O.Parent != null) {
            return GetParentRecursive(O.Parent, O.Name + "." + S);
        } else {
            return "."+S;
        }
    }

    public JsonItem Get(String ItemName){
        for(JsonItem I : this.jsonItems){
            if(I.Name.equals(ItemName)){
                return I;
            }
        }
        return this.jsonParser.ItemNull;
    }
    public void Add(JsonItem item){
        jsonItems.add(item);
    }

    public JsonObject Load(JsonParser json, FileInputStream In, JsonObject Parent, String Name) throws Exception {
        this.jsonParser = json;
        this.Name = Name;
        this.Parent = Parent;

        while(true) {
            JsonItem I = new JsonItem();
            I.Parent = this;
            int Char = JsonParser.SkipWhiteSpace(In, this);
            switch (Char) {
                case '"':
                    I.Name = JsonParser.ParseString(In, this);
                    Char = JsonParser.SkipWhiteSpace(In, this);
                    if(Char != ':'){
                        throw new Exception("Invalid Syntax: "+this.jsonParser.FilePath+"."+this.GetParentTree());
                    }
                    boolean EOO = JsonParser.ParseValue(In, I, this);
                    jsonItems.add(I);
                    if(EOO){return this;}
                case ',':
                    continue;
                case '}':
                    return this;
                default:
                    return this;
            }

        }



    }

    public void write(FileOutputStream Out) throws  Exception{
        Out.write('{');
        Out.write('\n');
        for(int X=0;X<jsonItems.size();X++){
            jsonItems.get(X).write(Out);
            if(X < jsonItems.size()-1) {
                Out.write(',');
                Out.write('\n');
            }
        }
        Out.write('\n');
        Out.write('}');
    }
}

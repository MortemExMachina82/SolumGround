package org.solumground.Json;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonArray {
    public String Name;
    public JsonObject Parent;

    public List<JsonItem> jsonItems = new ArrayList<>(10);
    public int Size = 0;

    public JsonItem Get(int Index){
        if(Index < jsonItems.size()) {
            return jsonItems.get(Index);
        }
        return this.Parent.jsonParser.ItemNull;
    }
    public void Add(JsonItem item){
        item.Name = null;
        jsonItems.add(item);
    }

    public JsonArray Load(FileInputStream In, JsonObject P, String N) throws Exception{
        this.Name = N;
        this.Parent = P;

        while(true) {
            JsonItem I = new JsonItem();
            I.Parent = this.Parent;
            I.Name = null;
            boolean EOA = JsonParser.ParseValue(In, I, this.Parent);
            if(I.Exists) {
                jsonItems.add(I);
                Size++;
            }
            if(EOA){
                return this;
            }
        }
    }
    public void write(FileOutputStream Out) throws Exception{
        Out.write('[');
        Out.write('\n');
        for(int X=0;X<jsonItems.size();X++){
            jsonItems.get(X).write(Out);
            if(X < jsonItems.size()-1) {
                Out.write(',');
                Out.write('\n');
            }
        }
        Out.write('\n');
        Out.write(']');
    }
}

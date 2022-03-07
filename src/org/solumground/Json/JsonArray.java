package org.solumground.Json;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonArray {
    public String Name;
    public JsonObject Parent;

    public List<JsonItem> jsonItems = new ArrayList<>(10);
    public int Size = 0;

    public JsonItem Get(int Index){
        return jsonItems.get(Index);
    }

    public JsonArray(FileInputStream In, JsonObject P, String N) throws Exception{
        this.Name = N;
        this.Parent = P;

        while(true) {
            JsonItem I = new JsonItem(this.Parent);
            I.Name = null;
            boolean EOA = JsonParser.ParseValue(In, I, this.Parent);
            if(I.Exists) {
                jsonItems.add(I);
                Size++;
            }
            if(EOA){
                return;
            }
        }
    }
}

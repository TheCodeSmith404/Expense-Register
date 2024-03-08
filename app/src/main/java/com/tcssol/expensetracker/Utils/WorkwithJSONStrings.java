package com.tcssol.expensetracker.Utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WorkwithJSONStrings {
    private String JSONString;
    private JSONObject jsonObject;
    public WorkwithJSONStrings(String JSONString){
        this.JSONString=JSONString;
        try {
//            Log.d("CheckingString","Creating jsonObject");
            jsonObject=new JSONObject(JSONString);
//            Log.d("CheckingString","Created jsonObject");
        } catch (JSONException e) {
//            Log.d("CheckingString","Failed to create JSON object");
            throw new RuntimeException(e);
        }
    }
    public List<String> getList(String name){
        JSONArray jsonArray;
        List<String> list=new ArrayList<>();
        Log.d("CheckingString","Inside get Categories for "+name);
        try {
            jsonArray= jsonObject.getJSONArray(name);
            Log.d("CheckingString","Array Inside get Categories for "+name);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        for(int i = 0; i < jsonArray.length(); i++){
            try {
                list.add((String) jsonArray.get(i));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }


        }
        Log.d("CheckingString","List Done Inside get Categories for "+name);
        return list;
    }
    public void updateElementList(String id,List<String> newList) {
        try {
            JSONArray jsonArray = new JSONArray(newList);
            jsonObject.put(id, jsonArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public void removeKey(String key) {
        jsonObject.remove(key);
        JSONString = jsonObject.toString();
    }
    public String getJSONString(){
        return jsonObject.toString();
    }
}

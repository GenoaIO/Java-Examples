package com.genoa.moonsail;

import com.google.gson.JsonObject;

public interface MoonsailPerform {

    public JsonObject describe();
	/*
	//Must be declared in class
	Example:
	JsonObject obj = new JsonObject();
	obj.add("methods", null);        
    return obj;
    */  

    public JsonObject perform(String method_name, JsonObject args, int accl_key) throws Exception;
	/*
	//Must be declared in class
	Example:
	if (method_name.equals("_describe_")) {
		return describe();
	}
	Check if method is valid/allowed, execute and return value as an JssonObject
	else if (method_name.equals("myFunc")) { 
		Method method = this.getClass().getMethod("myFunc", String.class);
		Object returnValue = method.invoke(null, "parameter-value1");        
	}
	else {
		throw new Exception("No such method " + method_name);
	}
	*/

	
	
	
	
}

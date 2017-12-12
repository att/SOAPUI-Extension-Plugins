/**Copyright (c) 2017, AT&T Intellectual Property.  All other rights reserved.**/
package com.att.jsoneditor;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


public class JSONJTreeNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = 1L;

	public enum DataType {ARRAY, OBJECT, VALUE};
	final DataType dataType;
	final int index;
	String fieldName;
	final String value;
	
	
	public JSONJTreeNode(String fieldName, int index, JsonElement jsonElement) {
		this.index = index;
		this.fieldName = fieldName;
		if(jsonElement.isJsonArray()) {
			this.dataType = DataType.ARRAY;
			this.value = jsonElement.toString();
			populateChildren(jsonElement);
		} else if(jsonElement.isJsonObject()) {
			this.dataType = DataType.OBJECT;
			this.value = jsonElement.toString();
			populateChildren(jsonElement);
		} else if(jsonElement.isJsonPrimitive()) {
			this.dataType = DataType.VALUE;
			this.value = jsonElement.toString();
		} else if(jsonElement.isJsonNull()) {
			this.dataType = DataType.VALUE;
			this.value = jsonElement.toString();
		} else {
			throw new IllegalArgumentException("jsonElement is an unknown element type.");
		}
		
	}
	
	private void populateChildren(JsonElement myJsonElement) {
		switch(dataType) {
		case ARRAY:
			int index = 0;
			Iterator<JsonElement> it = myJsonElement.getAsJsonArray().iterator();
			while(it.hasNext()) {
				JsonElement element = it.next();
				JSONJTreeNode childNode = new JSONJTreeNode(null, index, element);
				this.add(childNode);
				index++;
			}
			break;
		case OBJECT:
			for(Entry<String,JsonElement> entry : myJsonElement.getAsJsonObject().entrySet()) {
				JSONJTreeNode childNode = new JSONJTreeNode(entry.getKey(), -1, entry.getValue());
				this.add(childNode);
			}
			break;
		default:
			throw new IllegalStateException("Internal coding error this should never happen.");
		}
	}
	
	public JsonElement asJsonElement() {
		StringBuilder sb = new StringBuilder();
		buildJsonString(sb);
		String json = sb.toString().trim();
		if(json.startsWith("{") || json.startsWith("["))		
			return new JsonParser().parse(sb.toString());
		else {
			
			String testValue = "{" + json + "}";
			try {
				JsonElement wrapperElt = new JsonParser().parse(testValue);
				JsonObject obj = (JsonObject) wrapperElt;
				Iterator<Entry<String,JsonElement>> it = obj.entrySet().iterator();
				Entry<String,JsonElement> entry = it.next();
				return entry.getValue();
			} catch(JsonSyntaxException jse) {
				JsonElement rawElement = new JsonParser().parse(json);
				return rawElement;
			}			
		}
	}
	
	@SuppressWarnings("unchecked")
	private void buildJsonString(StringBuilder sb) {
		if(!StringUtils.isEmpty(this.fieldName)) {
			sb.append("\"" + this.fieldName + "\":");
		}
		Enumeration children;
		switch(dataType) {
		case ARRAY:
			sb.append("[");
			children = this.children();
			while(children.hasMoreElements()) {
				JSONJTreeNode child = (JSONJTreeNode) children.nextElement();
				child.buildJsonString(sb);
				if(children.hasMoreElements())
					sb.append(",");
			}
			sb.append("]");
			break;
		case OBJECT:
			sb.append("{");
			children = this.children();
			while(children.hasMoreElements()) {
				JSONJTreeNode child = (JSONJTreeNode) children.nextElement();
				child.buildJsonString(sb);
				if(children.hasMoreElements())
					sb.append(",");
			}
			sb.append("}");
			break;			
		default: {
				
				JsonElement elt = new JsonParser().parse(this.value);
				sb.append(elt.toString());
			}
		}
	}
	
	@Override
	public String toString() {
		switch(dataType) {
		case ARRAY:
		case OBJECT:
			if(index >= 0) {
				return String.format("[%d] (%s)", index, dataType.name());
			} else if(fieldName != null) {
				return String.format("%s (%s)", fieldName, dataType.name());
			} else {
				return String.format("(%s)", dataType.name());
			}
		default:
			if(index >= 0) {
				return String.format("[%d] %s", index, value);
			} else if(fieldName != null) {
				return String.format("%s: %s", fieldName, value);
			} else {
				return String.format("%s", value);
			}
			
		}
	}
}

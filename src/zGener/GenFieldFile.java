package zGener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenFieldFile {
	private ArrayList<FieldStruct> allFieldList;
	private Map<String, String> typeMap;
	private final int ALIGSIZE = 4;
	
	public GenFieldFile(DataParser parser){
		allFieldList = parser.getAllFieldList();
		typeMap = new HashMap<String, String>();
		setTypeTransform(typeMap);
	}

	public void genFieldCode(String filePath){
        for(FieldStruct field: allFieldList){
        	genOneField(filePath, field);
        }
	}

	private void genOneField(String filePath, FieldStruct field){	
		String fieldName = field.fieldName;
		BufferedWriter writer = getFieldWriter(filePath, field.fieldName);
		StringBuffer fieldBuffer = new StringBuffer();
		setFieldHeasStr(fieldBuffer, field.fieldName);

		genPublicFieldData(field, fieldBuffer);
		
		genFromFTDFieldFun(field, fieldBuffer);
		genToFTDFieldFun(field, fieldBuffer);
    	genToStringFun(field, fieldBuffer);

    	genJsonFun(field, fieldBuffer);
    	genSqlTxtFun(field, fieldBuffer);	    	
    	genFieldValueList(field, fieldBuffer);
    	genFieldTypeList(field, fieldBuffer);
    	
//		if (fieldName.compareTo("RtnObjectAttr") == 0 
//				|| fieldName.compareTo("RtnWarningEvent") == 0) {
//		}
		
    	fieldBuffer.append("}\n\n");		        	
    	try {
			writer.write(fieldBuffer.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private BufferedWriter getFieldWriter(String filePath, String fieldName){
    	String className = CommonUtils.getFieldName(fieldName);
    	String fileName = filePath + className + ".java";
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}
	
	private void setFieldHeasStr(StringBuffer fieldBuffer, String fieldName){
		fieldBuffer.append("package com.shfe.sfit.monitor.server.userapi.fields;\n\n");
		fieldBuffer.append("import java.nio.ByteBuffer;\n");
		fieldBuffer.append("import java.io.BufferedReader;\n");
		fieldBuffer.append("import java.io.BufferedWriter;\n");
		fieldBuffer.append("import java.io.StringReader;\n");
		fieldBuffer.append("import java.util.ArrayList;\n");
		fieldBuffer.append("import java.nio.ByteBuffer;\n");
		fieldBuffer.append("import org.json.JSONObject;\n");
		fieldBuffer.append("import org.json.JSONStringer;\n");
		fieldBuffer.append("import com.shfe.sfit.monitor.server.userapi.utils.BufferUtils;\n\n");
		fieldBuffer.append("public class " + CommonUtils.getFieldName(fieldName) + " implements FtdcField{\n\n");
	}
	
	private void genPublicFieldData(FieldStruct field, StringBuffer buffer){
    	for(FieldItem item: field.fieldItems){
    		String typeName = item.typeName;
    		String typeLen = item.typeLen;
    		String itemName = item.itemName;
    		if(typeMap.containsKey(typeName)){
    			buffer.append("    // " + item.desc + "  length[:" + typeLen + ", baseType: " + typeName + "]\n");
    			buffer.append("    public " + typeMap.get(typeName) + "  " + item.itemName + ";\n\n");	
    		}
    	}
		if (field.fieldName.compareTo("RtnObjectAttr") == 0){
			buffer.append("    public int _id;\n\n");
		}
		
		if (field.fieldName.compareTo("RtnWarningEvent") == 0){
			buffer.append("    public int _id;\n\n");
			buffer.append("    public int readFlag;\n\n");
		}
	}
	
	private void genFromFTDFieldFun(FieldStruct field, StringBuffer fieldBuffer){
		fieldBuffer.append("    public boolean fromFTDField(ByteBuffer field){\n");
		fieldBuffer.append("        try{\n");
    	int curSize = 0;
    	for(FieldItem item: field.fieldItems){
    		String typeName = item.typeName;
    		String typeLen = item.typeLen;
    		String itemName = item.itemName;
    		if(typeMap.containsKey(typeName)){
    			String javaType = typeMap.get(typeName);
    			
    			if(javaType == "int"){
    				if(curSize%ALIGSIZE != 0){
    					curSize = (curSize/ALIGSIZE + 1)*ALIGSIZE;
    				}
    				fieldBuffer.append("            " + String.format("%-15s", itemName) + " = " 
    				+ "BufferUtils.getBufferInt(field, " + curSize + ");\n");
    				typeLen = "4";
    			}
    			else if(javaType == "long"){
    				if(curSize%ALIGSIZE != 0){
    					curSize = (curSize/ALIGSIZE + 1)*ALIGSIZE;
    				}
    				fieldBuffer.append("            " + String.format("%-15s", itemName) + " = " 
    				+ "BufferUtils.getBufferLong(field, " + curSize + ");\n");
    				typeLen = "8";
    			}
    			else if(javaType == "String"){
    				fieldBuffer.append("            " + String.format("%-15s", itemName) + " = " 
    				+ "BufferUtils.getBufferStr(field, " + curSize + ", " + typeLen + ");\n");
    				curSize += 1;
    			}
				curSize += Integer.parseInt(typeLen);
    		}	        		
    	}
    	fieldBuffer.append("            return true;\n");
    	fieldBuffer.append("        }\n");
    	fieldBuffer.append("        catch(Exception e){\n");
    	fieldBuffer.append("            e.printStackTrace();\n");
    	fieldBuffer.append("        }\n");
    	fieldBuffer.append("        return false;\n");
    	fieldBuffer.append("    }\n\n");    	
	}

	private void genToFTDFieldFun(FieldStruct field, StringBuffer fieldBuffer){
		fieldBuffer.append("    public ByteBuffer toFTDField(){\n");
		fieldBuffer.append("        ByteBuffer content = ByteBuffer.allocateDirect(@SIZE);\n");
    	int replaceStartPos = fieldBuffer.length() - 8;
    	fieldBuffer.append("        try{\n");
    	
    	int curSize = 0;
    	for(FieldItem item: field.fieldItems){
    		String typeName = item.typeName;
    		String typeLen = item.typeLen;
    		String itemName = item.itemName;
    		if(typeMap.containsKey(typeName)){
    			String javaType = typeMap.get(typeName);
    			fieldBuffer.append("            content.position(" + curSize + ");\n");
    			
    			if(javaType == "int"){
    				if(curSize%ALIGSIZE != 0){
    					curSize = (curSize/ALIGSIZE + 1)*ALIGSIZE;
    				}
    				fieldBuffer.append("            content.putInt(" + itemName + ");\n");
    				typeLen = "4";
    			}
    			else if(javaType == "long"){
    				if(curSize%ALIGSIZE != 0){
    					curSize = (curSize/ALIGSIZE + 1)*ALIGSIZE;
    				}
    				fieldBuffer.append("            content.putLong(" + itemName + ");\n");
    				typeLen = "8";
    			}
    			else if(javaType == "String"){
    				fieldBuffer.append("            if(" + itemName + " != null){\n");
    				fieldBuffer.append("                content.put(" + itemName + ".getBytes());\n");
    				fieldBuffer.append("            }\n");
    				curSize += 1;
    			}
				curSize += Integer.parseInt(typeLen);
    		}	        		
    	}
    	fieldBuffer.append("        }\n");
    	fieldBuffer.append("        catch(Exception e){\n");
    	fieldBuffer.append("            e.printStackTrace();\n");
    	fieldBuffer.append("        }\n");
    	fieldBuffer.append("        return content;\n");
    	fieldBuffer.append("    }\n\n");
    	fieldBuffer.replace(replaceStartPos, replaceStartPos + 5, String.valueOf(curSize));    	
	}
	private void genFieldValueList(FieldStruct field, StringBuffer fieldBuffer){
		fieldBuffer.append("    public ArrayList<KeyValue> getFieldValueList() {\n");
		fieldBuffer.append("        ArrayList<KeyValue> fieldValueList = new ArrayList<KeyValue>();\n");
		for (FieldItem item : field.fieldItems) {
			fieldBuffer.append("        fieldValueList.add(new KeyValue(\"" + typeMap.get(item.typeName) 
					+ "\", this." + item.itemName + "));\n");
		}
		if (field.fieldName.compareTo("RtnObjectAttr") == 0){
			fieldBuffer.append("        fieldValueList.add(new KeyValue(\"int\", null));\n");
		}
		
		if (field.fieldName.compareTo("RtnWarningEvent") == 0){
			fieldBuffer.append("        fieldValueList.add(new KeyValue(\"int\", null));\n");
			fieldBuffer.append("        fieldValueList.add(new KeyValue(\"int\", 0));\n");
		}
		fieldBuffer.append("        return fieldValueList;\n");
		fieldBuffer.append("    }\n\n");
	}
	
	private void genFieldTypeList(FieldStruct field, StringBuffer fieldBuffer){
		fieldBuffer.append("    public ArrayList<KeyValue> getFieldTypeList() {\n");
		fieldBuffer.append("        ArrayList<KeyValue> fieldTypeList = new ArrayList<KeyValue>();\n");
		for (FieldItem item : field.fieldItems) {
			fieldBuffer.append("        fieldTypeList.add(new KeyValue(\"" + item.itemName + "\", \"" 
							+ typeMap.get(item.typeName) + "\"));\n");
		}
		if (field.fieldName.compareTo("RtnObjectAttr") == 0){
			fieldBuffer.append("        fieldTypeList.add(new KeyValue(\"_id\", \"int\"));\n");
		}
		
		if (field.fieldName.compareTo("RtnWarningEvent") == 0){
			fieldBuffer.append("        fieldTypeList.add(new KeyValue(\"_id\", \"int\"));\n");
			fieldBuffer.append("        fieldTypeList.add(new KeyValue(\"readFlag\", \"int\"));\n");
		}
		fieldBuffer.append("        return fieldTypeList;\n");
		fieldBuffer.append("    }\n\n");
	}
	
	private void genJsonFun(FieldStruct field, StringBuffer fieldBuffer){
    	StringBuffer toJsonBuffer = new StringBuffer();
    	StringBuffer fromJsonBuffer = new StringBuffer();
    	
    	toJsonBuffer.append("    public JSONObject toJson(){\n");
    	toJsonBuffer.append("    	 JSONObject jsonObject=new JSONObject();\n");
    	toJsonBuffer.append("    	 try {\n");
    	

    	for(FieldItem item: field.fieldItems){
    		String typeName = item.typeName;
    		String itemName = item.itemName;
    		if(typeMap.containsKey(typeName)){
    			toJsonBuffer.append("    	 jsonObject.put(\"" + itemName + "\", this." + itemName + ");\n");
    		}    		
    	}
		toJsonBuffer.append("    	} catch (Exception e) {\n");
		toJsonBuffer.append("    	     e.printStackTrace();\n");
		toJsonBuffer.append("    	 }\n");
		toJsonBuffer.append("    	return jsonObject;\n");
		toJsonBuffer.append("   }\n");
		
		fieldBuffer.append(toJsonBuffer.toString());
		
		fromJsonBuffer.append("    public void fromJson(JSONObject json){\n");

    	for(FieldItem item: field.fieldItems){
    		String typeName = item.typeName;
    		String itemName = item.itemName;
    		if(typeMap.containsKey(typeName)){
    			String javaType = typeMap.get(typeName);
    			if(javaType == "String"){
    				fromJsonBuffer.append("    	 this." + itemName + " = json.optString(\"" + itemName + "\");\n");
    			}
    			else if(javaType == "int"){
    				fromJsonBuffer.append("    	 this." + itemName + " = json.optInt(\"" + itemName + "\");\n");
    			}
    			else if(javaType == "long"){
    				fromJsonBuffer.append("    	 this." + itemName + " = json.optLong(\"" + itemName + "\");\n");
    			}
    		}    		
    	}
    	fromJsonBuffer.append("    }\n"); 
    	
		fieldBuffer.append(fromJsonBuffer.toString());
	}
	
	private void genToStringFun(FieldStruct field, StringBuffer fieldBuffer){
		StringBuffer toStringBuffer = new StringBuffer();
		toStringBuffer.append("\n    public void printField(){\n");
		toStringBuffer.append("        System.out.println( \" \" ");

    	for(FieldItem item: field.fieldItems){
    		String typeName = item.typeName;
    		String itemName = item.itemName;
    		if(typeMap.containsKey(typeName)){
    			toStringBuffer.append("\n             + this." + itemName + " + \", \" ");
    		}
    	}
		toStringBuffer.append(");\n");
		toStringBuffer.append("    }\n\n");
		
		fieldBuffer.append(toStringBuffer.toString());		
	}

	private void genSqlTxtFun(FieldStruct field, StringBuffer fieldBuffer){
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("\n    public String getInsertSqlTxt(){\n");
		sqlBuffer.append("         StringBuffer insertSqlTxt = new StringBuffer();\n");

		int itemNum = 0;
    	for(FieldItem item: field.fieldItems){
    		itemNum += 1;
    		String typeName = item.typeName;
    		String itemName = item.itemName;
    		if(typeMap.containsKey(typeName)){
    			String javaType = typeMap.get(typeName);
    			if(javaType == "String"){
        			sqlBuffer.append("         insertSqlTxt.append(\"'\" + this." + itemName + "+ \"'\"");    				
    			}
    			else{
    				sqlBuffer.append("         insertSqlTxt.append(this." + itemName); 
    			}
    			if(itemNum != field.fieldItems.size()){
    				sqlBuffer.append(" + \", \");\n");
    			} 
    			else{
    				sqlBuffer.append(");\n");
    			}
    		}
    	}
    	sqlBuffer.append("         return insertSqlTxt.toString();\n");
    	sqlBuffer.append("    }\n\n");
		
		fieldBuffer.append(sqlBuffer.toString());		
	}
	
	private void setTypeTransform(Map<String, String> typeMap){
		typeMap.put("Int", "int");
		typeMap.put("Float", "float");
		typeMap.put("EnumChar", "int");
		typeMap.put("Word", "int");
		typeMap.put("QWord", "long");
		typeMap.put("VString", "String");
		typeMap.put("String", "String");
		typeMap.put("Char", "char");
		typeMap.put("RangeInt", "int");		
	}
	
}

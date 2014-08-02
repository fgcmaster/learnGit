package zGener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


class FieldItem{
	public String itemName;
	public String typeName;
	public String typeLen;
	public String desc;
}

class FieldStruct{	
	public FieldStruct(String fieldName){
		this.fieldName = fieldName;
		fieldItems = new ArrayList<FieldItem>();
	}
	
	public void addItem(FieldItem item){
		fieldItems.add(item);
	}
	
	public String fieldName;
	public ArrayList<FieldItem> fieldItems;	
}

public class DataParser {

	private Map<String, String> typeMap;
	private ArrayList<FieldStruct> allFieldList;
	private Map<String, ArrayList<String>> packageMap;
	
	private Set<String> curPackageSet = null;

	private Set<String> curFieldSet = null;
	
	public DataParser(){
		typeMap = new HashMap<String, String>();
		packageMap = new HashMap<String, ArrayList<String>>();
		allFieldList = new ArrayList<FieldStruct>();
		curPackageSet = new HashSet<String>();

		curPackageSet.add("RspError");
		curPackageSet.add("ReqUserLogin");
		curPackageSet.add("RspUserLogin");
		curPackageSet.add("RtnWarningEventTopic");
		curPackageSet.add("ReqQrySubscriberTopic");
		curPackageSet.add("RspQrySubscriberTopic");
		curPackageSet.add("RtnObjectAttrTopic");
		curPackageSet.add("ReqQrySysUserLoginTopic");
		curPackageSet.add("RspQrySysUserLoginTopic");
		curPackageSet.add("ReqUserPasswordUpdate");
		curPackageSet.add("RspUserPasswordUpdate");
		curPackageSet.add("ReqFeedback");
		curPackageSet.add("RspFeedback");
		curPackageSet.add("ReqQryGetFileTopic");
		curPackageSet.add("RspQryGetFileTopic");
		curPackageSet.add("ReqUpdateCheck");
		curPackageSet.add("RspUpdateCheck");
		curPackageSet.add("ReqUserLogout");
		curPackageSet.add("RspUserLogout");
				
		curFieldSet = new HashSet<String>();
		curFieldSet.add("RspInfo");
		curFieldSet.add("ReqUserLogin");
		curFieldSet.add("RspUserLogin");
		curFieldSet.add("RtnWarningEvent");
		curFieldSet.add("ReqQrySubscriber");
		curFieldSet.add("RspQrySubscriber");
		curFieldSet.add("RtnObjectAttr");
		curFieldSet.add("ReqQrySysUserLogin");
		curFieldSet.add("RspQrySysUserLogin");
		curFieldSet.add("UserPasswordUpdate");
		curFieldSet.add("ReqFeedbackInfo");
		curFieldSet.add("RspFeedbackInfo");
		curFieldSet.add("ReqQryGetFile");
//		curFieldSet.add("RspQryGetFile");
		curFieldSet.add("ReqUpdateCheck");
		curFieldSet.add("RspUpdateCheck");
		curFieldSet.add("ReqUserLogout");
		curFieldSet.add("RspUserLogout");
	}
		
	public void parseFtd(String ftdXmlFile){
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        Document document=db.parse(new File(ftdXmlFile));
	        Element root=document.getDocumentElement();
	        getAllType(root.getChildNodes());
	        getFieldInfo(root.getChildNodes());
	        getAllPackage(root.getChildNodes());
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<FieldStruct> getAllFieldList(){
		return allFieldList;
	}
	
	public Map<String, ArrayList<String>> getPackageMap(){
		return packageMap;
	}
	
	private void getAllType(NodeList ftdNodeList){
		Set<String> typeSet = new HashSet<String>();
		
		for(int i = 0; i < ftdNodeList.getLength(); i++){
			Node tmpNode = ftdNodeList.item(i);
			if(tmpNode.getNodeName() == "types"){
				NodeList typeNodeList = tmpNode.getChildNodes();
				for(int j = 0; j < typeNodeList.getLength(); j++){
					Node typeRootNode = typeNodeList.item(j);
					if(typeRootNode.getNodeType() == typeRootNode.ELEMENT_NODE){
						NamedNodeMap allAttr = typeNodeList.item(j).getAttributes();
						Node typeNameNode = allAttr.getNamedItem("typename");
						String typeName = "C" + typeNameNode.getNodeValue() + "Type";
						Node lengthNode = allAttr.getNamedItem("length");
						typeSet.add(typeRootNode.getNodeName());
						if(typeNameNode != null && lengthNode != null){
							typeMap.put(typeName, typeRootNode.getNodeName() + "," + lengthNode.getNodeValue());
						}						
						else if(typeRootNode.getNodeName() == "EnumChar" || typeRootNode.getNodeName() == "Word"){
							typeMap.put(typeName, typeRootNode.getNodeName() + ",4");
						}
					}
				}
			}
		}
		Iterator<String> iter = typeSet.iterator();
		while(iter.hasNext()){
			System.out.println(iter.next());
		}
	}

	private void getFieldInfo(NodeList ftdNodeList){
		for(int i = 0; i < ftdNodeList.getLength(); i++){
			Node tmpNode = ftdNodeList.item(i);
			if(tmpNode.getNodeName() == "fields"){
				NodeList fieldNodeList = tmpNode.getChildNodes();
				for(int j = 0; j < fieldNodeList.getLength(); j++){
					Node fieldRootNode = fieldNodeList.item(j);
					if(fieldRootNode.getNodeType() == fieldRootNode.ELEMENT_NODE){
						if(fieldRootNode.getNodeName() == "fieldDefine"){
							String fieldDefineName = getAttrValue(fieldRootNode, "name");
							FieldStruct fieldStruct = new FieldStruct(fieldDefineName);
							if(curFieldSet.contains(fieldDefineName)){
								allFieldList.add(fieldStruct);
							}
							NodeList fieldItems = fieldRootNode.getChildNodes();
							for(int k = 0; k < fieldItems.getLength(); k++){
								Node fieldItem = fieldItems.item(k);
								if(fieldItem.getNodeType() == fieldItem.ELEMENT_NODE){
									NamedNodeMap allAttr = fieldItem.getAttributes();
									Node fieldNameNode = allAttr.getNamedItem("name");
									Node fieldTypeNode = allAttr.getNamedItem("type");
									Node descNode = allAttr.getNamedItem("description");
									
									String fieldName = fieldNameNode.getNodeValue();
									String fieldType = fieldTypeNode.getNodeValue();
									
									if(typeMap.containsKey(fieldType)){
										String typeInfo = typeMap.get(fieldType);

										FieldItem item = new FieldItem();
										item.itemName = fieldName;
										item.typeName = typeInfo.split(",")[0];
										item.typeLen = typeInfo.split(",")[1];
										item.desc = descNode.getNodeValue();
										fieldStruct.addItem(item);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void getAllPackage(NodeList ftdNodeList){
		for(int i = 0; i < ftdNodeList.getLength(); i++){
			Node tmpNode = ftdNodeList.item(i);
			if(tmpNode.getNodeName() == "packages"){
				NodeList packageNodeList = tmpNode.getChildNodes();
				for(int j = 0; j < packageNodeList.getLength(); j++){
					Node packageRootNode = packageNodeList.item(j);
					if(packageRootNode.getNodeType() == packageRootNode.ELEMENT_NODE){
						if(packageRootNode.getNodeName() == "package"){
							String packageName = getAttrValue(packageRootNode, "name");
							
							if(packageName.compareTo("ReqUserLogin") == 0){
								System.out.println("");
							}
							ArrayList<String> packageFields = new ArrayList<String>();
							
							NodeList packageItems = packageRootNode.getChildNodes();
							for(int k = 0; k < packageItems.getLength(); k++){
								Node packageItem = packageItems.item(k);
								if(packageItem.getNodeType() == packageItem.ELEMENT_NODE){
									NamedNodeMap allAttr = packageItem.getAttributes();
									if(packageName.compareTo("ReqUserLogin") == 0 
											&& allAttr.getNamedItem("name").getNodeValue().compareTo("Dissemination") == 0 )
									{
										continue;
									}
									packageFields.add(allAttr.getNamedItem("name").getNodeValue());
								}
							}
							if(curPackageSet.contains(packageName)){
								packageMap.put(packageName, packageFields);
							}
						}
					}
				}
			}
		}
	}
	private Node getTheNode(NodeList nodeList, String nodeName){
		for(int i = 0; i < nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			if(node.getNodeName().compareTo(nodeName) == 0){
				return node;
			}
		}
		return null;
	}
	
	private String getAttrValue(Node node, String attrName){
		NamedNodeMap allAttr = node.getAttributes();
		Node attrNode= allAttr.getNamedItem(attrName);
		if(attrNode != null){
			return attrNode.getNodeValue();
		}
		return "";
	}
}

package zGener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class GenTestFile {
	private Map<String, ArrayList<String>> packageMap;
	
	public GenTestFile(DataParser parser){		
		packageMap = parser.getPackageMap();
	}

	public void genTestFile(String filePath){		
		genTestSpiImp(filePath);
	}
	
	private void genTestSpiImp(String filePath){

		String fileName = filePath + "\\test\\FtdcUserSpiTest.java";
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
			
			StringBuffer spiImpBuffer = new StringBuffer();
			setSpiImpBufferHeader(spiImpBuffer);
			
			Iterator it=packageMap.keySet().iterator();    
			while(it.hasNext()){
				String packageName = it.next().toString();
				
				if(CommonUtils.isSpiPackage(packageName)){
					spiImpBuffer.append("    public  void On" + packageName + "(");
					
					ArrayList<String> fieldList = packageMap.get(packageName);
					for(String fieldName: fieldList){
						spiImpBuffer.append(CommonUtils.getFieldName(fieldName) + " ftdc" + fieldName + ", ");
					}
					if(packageName.startsWith("Rsp")){
						spiImpBuffer.append("int nRequestID, boolean bIsLast){\n");
					}
					else{
						spiImpBuffer.delete(spiImpBuffer.length()-2, spiImpBuffer.length());
						spiImpBuffer.append("){\n");
					}

					if(packageName.startsWith("Rsp")){
					
						spiImpBuffer.append("        FtdcUserSpi spi = getDispatcher().getRequestSpi(nRequestID);\n");
						spiImpBuffer.append("        if(spi != null){\n");
						spiImpBuffer.append("            spi.On" + packageName + "(");
	
						for(String fieldName: fieldList){
							spiImpBuffer.append("ftdc" + fieldName + ", ");
						}
						spiImpBuffer.append("nRequestID, bIsLast);\n");
						spiImpBuffer.append("            if(bIsLast){\n");
						spiImpBuffer.append("                getDispatcher().removeRequest(nRequestID);\n");
						spiImpBuffer.append("            }\n");
						spiImpBuffer.append("        }\n");
						spiImpBuffer.append("    }\n\n");
					}
					else{
						spiImpBuffer.append("		ArrayList<FtdcUserSpi> spiList = getDispatcher().getSpi();\n");
						spiImpBuffer.append("		if(spiList != null){\n");
						spiImpBuffer.append("			for(FtdcUserSpi spi: spiList){\n");
						spiImpBuffer.append("            spi.On" + packageName + "(");
	
						for(String fieldName: fieldList){
							spiImpBuffer.append("ftdc" + fieldName + ", ");						
							}
						spiImpBuffer.delete(spiImpBuffer.length()-2, spiImpBuffer.length());
						spiImpBuffer.append(");\n");
						spiImpBuffer.append("			}\n");
						spiImpBuffer.append("		}\n");
						spiImpBuffer.append("	}\n");
					}
				}				
			}   
			spiImpBuffer.append("}\n");
			writer.write(spiImpBuffer.toString());
	        writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setSpiImpBufferHeader(StringBuffer spiImpBuffer){
		spiImpBuffer.append("package com.sfit.userapi.test;\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("import java.nio.ByteBuffer;\n");
		spiImpBuffer.append("import java.util.ArrayList;\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("import com.sfit.userapi.ApiDataDispatcher;\n");
		spiImpBuffer.append("import com.sfit.userapi.ApiFactory;\n");
		spiImpBuffer.append("import com.sfit.userapi.FtdcUserSpi;\n");
		spiImpBuffer.append("import com.sfit.userapi.fields.*;\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("public class FtdcUserSpiTest {\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("	public FtdcUserSpiTest(){}\n");
		spiImpBuffer.append("	\n");
		spiImpBuffer.append("	private ApiDataDispatcher getDispatcher(){\n");
		spiImpBuffer.append("		return ApiFactory.getInstance().getDispatcher();\n");
		spiImpBuffer.append("	}\n");
		spiImpBuffer.append("	\n");
		spiImpBuffer.append("	public  void OnFrontConnected(){\n");
		spiImpBuffer.append("		ArrayList<FtdcUserSpi> spiList = getDispatcher().getSpi();\n");
		spiImpBuffer.append("		if(spiList != null){\n");
		spiImpBuffer.append("			for(FtdcUserSpi spi: spiList){\n");
		spiImpBuffer.append("				spi.OnFrontConnected();\n");
		spiImpBuffer.append("			}\n");
		spiImpBuffer.append("		}\n");
		spiImpBuffer.append("	}\n");
		spiImpBuffer.append("	\n");
		spiImpBuffer.append("	public  void OnFrontDisconnected(int nReason){\n");
		spiImpBuffer.append("		ArrayList<FtdcUserSpi> spiList = getDispatcher().getSpi();\n");
		spiImpBuffer.append("		if(spiList != null){\n");
		spiImpBuffer.append("			for(FtdcUserSpi spi: spiList){\n");
		spiImpBuffer.append("				spi.OnFrontDisconnected(nReason);\n");
		spiImpBuffer.append("			}\n");
		spiImpBuffer.append("		}\n");
		spiImpBuffer.append("	}\n");
		spiImpBuffer.append("	\n");
		spiImpBuffer.append("	public void OnHeartBeatWarning(int nTimeLapse) {\n");
		spiImpBuffer.append("		ArrayList<FtdcUserSpi> spiList = getDispatcher().getSpi();\n");
		spiImpBuffer.append("		if(spiList != null){\n");
		spiImpBuffer.append("			for(FtdcUserSpi spi: spiList){\n");
		spiImpBuffer.append("				spi.OnHeartBeatWarning(nTimeLapse);\n");
		spiImpBuffer.append("			}\n");
		spiImpBuffer.append("		}\n");
		spiImpBuffer.append("	}\n\n");
	}
	
}

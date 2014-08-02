package zGener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class GenSpiFile {
	private Map<String, ArrayList<String>> packageMap;
	
	public GenSpiFile(DataParser parser){		
		packageMap = parser.getPackageMap();
	}

	public void genSpiFile(String filePath){		
		genSpiInterface(filePath);
		genSpiImpFile(filePath);
		genJNISpiFile(filePath);		
		genAdapterFile(filePath);
	}
	
	private void genJNISpiFile(String filePath){
		String fileName = filePath + "..\\..\\..\\..\\..\\..\\..\\jni\\ApiJni\\SysUserSpiJNI_Auto.h";
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
			
			StringBuffer spiJniBuffer = new StringBuffer();
			setSpiHeader(spiJniBuffer);
			
			Iterator it=packageMap.keySet().iterator();    
			while(it.hasNext()){
				String packageName = it.next().toString();

				if(isSpiPackage(packageName)){
					spiJniBuffer.append("    void On" + packageName + "(");
					
					ArrayList<String> fieldList = packageMap.get(packageName);
					for(String fieldName: fieldList){
						String ftdcFieldName = CommonUtils.getFieldName(fieldName);
						spiJniBuffer.append(ftdcFieldName + "* p" + fieldName + ", ");
					}
					if(packageName.startsWith("Rsp")){
						spiJniBuffer.append("int nRequestID, bool bIsLast){\n");
					}
					else{
						spiJniBuffer.delete(spiJniBuffer.length()-2, spiJniBuffer.length());
						spiJniBuffer.append("){\n");
					}
					//spiJniBuffer.append("    {\n");
					spiJniBuffer.append("        LOG(\" " + packageName + " \");\n");
					
					
					for(String fieldName: fieldList){
						spiJniBuffer.append("        jobject rsp" + fieldName + " = m_spiUtils.getJavaManagerBuffer(p" + fieldName + ");\n");
					}
					spiJniBuffer.append("\n");
					
					spiJniBuffer.append("        if(jmethodMap.find(\"" + packageName + "\") == jmethodMap.end())\n");
					spiJniBuffer.append("        {\n");
					spiJniBuffer.append("            jmethodID method = m_spiUtils.getJavaMethod(\"On" + packageName + "\",\n");
					spiJniBuffer.append("            \"(");

					for(String fieldName: fieldList){
						spiJniBuffer.append("Ljava/nio/ByteBuffer;");
					}
					if(packageName.startsWith("Rsp")){
						spiJniBuffer.append("IZ)V\");\n");
					}
					else{
						spiJniBuffer.append(")V\");\n");
					}
					
					spiJniBuffer.append("            jmethodMap[\"" + packageName + "\"] = method;\n");
					spiJniBuffer.append("        }\n");
					
					spiJniBuffer.append("        JNIEnv* env = getJNIEnv();\n");
					spiJniBuffer.append("        env->CallVoidMethod(m_javaSpiInstance, jmethodMap[\"" + packageName + "\"] ,"); 

					for(String fieldName: fieldList){
						spiJniBuffer.append("rsp" + fieldName + ", ");
					}
					if(packageName.startsWith("Rsp")){
						spiJniBuffer.append("nRequestID, bIsLast);\n");
					}
					else{
						spiJniBuffer.delete(spiJniBuffer.length()-2, spiJniBuffer.length());
						spiJniBuffer.append(");\n");
					}

					for(String fieldName: fieldList){
			            spiJniBuffer.append("    env->DeleteLocalRef(" + "rsp" + fieldName + ");\n");
					}
		            
					spiJniBuffer.append("    }\n\n");
				}				
			}   

			spiJniBuffer.append("private:\n");
			spiJniBuffer.append("    jobject m_javaSpiInstance;\n");
			spiJniBuffer.append("    SpiUtils m_spiUtils;\n");
			spiJniBuffer.append("    std::map<std::string, jmethodID> jmethodMap;\n");
			spiJniBuffer.append("};\n\n");
			spiJniBuffer.append("#endif\n\n");
			writer.write(spiJniBuffer.toString());
	        writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setSpiHeader(StringBuffer spiImpBuffer){
		spiImpBuffer.append("#ifndef SYSUSERSPIJNI_H\n");
		spiImpBuffer.append("#define SYSUSERSPIJNI_H\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("#include \"SpiUtils.h\"\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("class SysUserSpiJNI: public CShfeFtdcUserSpi\n");
		spiImpBuffer.append("{\n");
		spiImpBuffer.append("public:\n");
		spiImpBuffer.append("	SysUserSpiJNI(jobject spiInstance)\n");
		spiImpBuffer.append("		:m_javaSpiInstance(spiInstance), m_spiUtils(m_javaSpiInstance)\n");
		spiImpBuffer.append("	{\n");
		spiImpBuffer.append("	}\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("	virtual void OnFrontConnected()\n");
		spiImpBuffer.append("	{\n");
		spiImpBuffer.append("		LOG(\"OnFrontConnected\");\n");
		spiImpBuffer.append("		JNIEnv* env = getJNIEnv();\n");
		spiImpBuffer.append("		jmethodID method = m_spiUtils.getJavaMethod(\"OnFrontConnected\", \"()V\");\n");
		spiImpBuffer.append("		env->CallVoidMethod(m_javaSpiInstance, method);\n");
		spiImpBuffer.append("	}\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("	virtual void OnFrontDisconnected(int nReason)\n");
		spiImpBuffer.append("	{\n");
		spiImpBuffer.append("		LOG(\"OnFrontDisconnected\");\n");
		spiImpBuffer.append("		JNIEnv* env = getJNIEnv();\n");
		spiImpBuffer.append("		jmethodID method = m_spiUtils.getJavaMethod(\"OnFrontDisconnected\", \"(I)V\");\n");
		spiImpBuffer.append("		env->CallVoidMethod(m_javaSpiInstance, method, nReason);\n");
		spiImpBuffer.append("	};\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("	virtual void OnHeartBeatWarning(int nTimeLapse)\n");
		spiImpBuffer.append("	{\n");
		spiImpBuffer.append("		LOG(\"OnHeartBeatWarning\");\n");
		spiImpBuffer.append("		JNIEnv* env = getJNIEnv();\n");
		spiImpBuffer.append("		jmethodID method = m_spiUtils.getJavaMethod(\"OnHeartBeatWarning\", \"(I)V\");\n");
		spiImpBuffer.append("		env->CallVoidMethod(m_javaSpiInstance, method, nTimeLapse);\n");
		spiImpBuffer.append("	};\n\n");
	}
	
	private void genSpiImpFile(String filePath){

		String fileName = filePath + "\\implement\\FtdcUserSpiImpl.java";
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
			
			StringBuffer spiImpBuffer = new StringBuffer();
			setSpiImpBufferHeader(spiImpBuffer);
			
			Iterator it=packageMap.keySet().iterator();    
			while(it.hasNext()){
				String packageName = it.next().toString();
				
				if(isSpiPackage(packageName)){
					spiImpBuffer.append("    public  void On" + packageName + "(");
					
					ArrayList<String> fieldList = packageMap.get(packageName);
					for(String fieldName: fieldList){
						spiImpBuffer.append("ByteBuffer " + fieldName + ", ");
					}
					if(packageName.startsWith("Rsp")){
						spiImpBuffer.append("int nRequestID, boolean bIsLast){\n");
					}
					else{
						spiImpBuffer.delete(spiImpBuffer.length()-2, spiImpBuffer.length());
						spiImpBuffer.append("){\n");
					}

					for(String fieldName: fieldList){
						String ftdcFieldName = CommonUtils.getFieldName(fieldName);
						spiImpBuffer.append("        " + ftdcFieldName + " ftdc" + fieldName + " = new " + ftdcFieldName + "();\n");
						spiImpBuffer.append("        ftdc" + fieldName + ".fromFTDField(" + fieldName + ");\n\n");
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
						if (packageName.compareTo("RtnObjectAttrTopic") == 0) {
							spiImpBuffer.append("		Collection<FtdcUserSpi> spiList = getDispatcher().getAttrObjSpi(ftdcRtnObjectAttr);\n");
							spiImpBuffer.append("		if(spiList != null){\n");
							spiImpBuffer.append("			for(FtdcUserSpi spi: spiList){\n");
							spiImpBuffer.append("            spi.On" + packageName + "(");
						}
						else{
							spiImpBuffer.append("		Set<FtdcUserSpi> spiSet = getDispatcher().getSpi();\n");
							spiImpBuffer.append("		if(!spiSet.isEmpty()){\n");
							spiImpBuffer.append("			Iterator<FtdcUserSpi> iter = spiSet.iterator();\n");
							spiImpBuffer.append("            while(iter.hasNext()){\n");
							spiImpBuffer.append("                iter.next().On" + packageName + "(");							
						}
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
		spiImpBuffer.append("package com.shfe.sfit.monitor.server.userapi.implement;\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("import java.nio.ByteBuffer;\n");
		spiImpBuffer.append("import java.util.ArrayList;\n");
		spiImpBuffer.append("import java.util.Set;\n");		
		spiImpBuffer.append("import java.util.Iterator;\n");
		spiImpBuffer.append("import java.util.Collection;\n");		
		spiImpBuffer.append("\n");
		spiImpBuffer.append("import com.shfe.sfit.monitor.server.userapi.ApiDataDispatcher;\n");
		spiImpBuffer.append("import com.shfe.sfit.monitor.server.userapi.FtdcUserSpi;\n");
		spiImpBuffer.append("import com.shfe.sfit.monitor.server.userapi.fields.*;\n");
		spiImpBuffer.append("import com.shfe.sfit.monitor.server.ServerMgr;\n");		
		spiImpBuffer.append("\n");
		spiImpBuffer.append("public class FtdcUserSpiImpl {\n");
		spiImpBuffer.append("\n");
		spiImpBuffer.append("	public FtdcUserSpiImpl(){}\n");
		spiImpBuffer.append("	\n");
		spiImpBuffer.append("	private ApiDataDispatcher getDispatcher(){\n");
		spiImpBuffer.append("		return ServerMgr.getInstance().getDispatcher();\n");
		spiImpBuffer.append("	}\n");
		spiImpBuffer.append("	\n");
		spiImpBuffer.append("	public  void OnFrontConnected(){\n");
		spiImpBuffer.append("		Set<FtdcUserSpi> spiSet = getDispatcher().getSpi();\n");
		spiImpBuffer.append("		if(!spiSet.isEmpty()){\n");
		spiImpBuffer.append("		    Iterator<FtdcUserSpi> iter = spiSet.iterator();\n");
		spiImpBuffer.append("			while(iter.hasNext()){\n");
		spiImpBuffer.append("				iter.next().OnFrontConnected();\n");
		spiImpBuffer.append("			}\n");
		spiImpBuffer.append("		}\n");
		spiImpBuffer.append("	}\n");
		spiImpBuffer.append("	\n");
		spiImpBuffer.append("	public  void OnFrontDisconnected(int nReason){\n");
		spiImpBuffer.append("		Set<FtdcUserSpi> spiSet = getDispatcher().getSpi();\n");
		spiImpBuffer.append("		if(!spiSet.isEmpty()){\n");
		spiImpBuffer.append("		    Iterator<FtdcUserSpi> iter = spiSet.iterator();\n");
		spiImpBuffer.append("			while(iter.hasNext()){\n");
		spiImpBuffer.append("				iter.next().OnFrontDisconnected(nReason);\n");
		spiImpBuffer.append("			}\n");
		spiImpBuffer.append("		}\n");
		spiImpBuffer.append("	}\n");
		spiImpBuffer.append("	\n");
		spiImpBuffer.append("	public void OnHeartBeatWarning(int nTimeLapse) {\n");
		spiImpBuffer.append("		Set<FtdcUserSpi> spiSet = getDispatcher().getSpi();\n");
		spiImpBuffer.append("		if(!spiSet.isEmpty()){\n");
		spiImpBuffer.append("		    Iterator<FtdcUserSpi> iter = spiSet.iterator();\n");
		spiImpBuffer.append("			while(iter.hasNext()){\n");
		spiImpBuffer.append("				iter.next().OnHeartBeatWarning(nTimeLapse);\n");
		spiImpBuffer.append("			}\n");
		spiImpBuffer.append("		}\n");
		spiImpBuffer.append("	}\n");
		spiImpBuffer.append("	\n");
	}
	
	private boolean isSpiPackage(String packageName){
		return CommonUtils.isSpiPackage(packageName);
	}

	private void genSpiInterface(String filePath){
    	String fileName = filePath + "FtdcUserSpi.java";
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
			
			StringBuffer spiItfBuffer = new StringBuffer();
			spiItfBuffer.append("package com.shfe.sfit.monitor.server.userapi;\n\n");
			spiItfBuffer.append("import com.shfe.sfit.monitor.server.userapi.fields.*;\n\n");
							
			spiItfBuffer.append("public interface FtdcUserSpi {\n\n");
			spiItfBuffer.append("    public void OnFrontConnected();\n\n");
			spiItfBuffer.append("    public void OnFrontDisconnected(int nReason);\n\n");
			spiItfBuffer.append("    public void OnHeartBeatWarning(int nTimeLapse);\n\n");
			
			Iterator it=packageMap.keySet().iterator();    
			while(it.hasNext()){
				String packageName = it.next().toString();
				if(isSpiPackage(packageName)){
					spiItfBuffer.append("    public void On" + packageName + "(");	
					ArrayList<String> fieldList = packageMap.get(packageName);
					for(String fieldName: fieldList){
						spiItfBuffer.append(CommonUtils.getFieldName(fieldName) + " " + fieldName + ", ");
					}
					if(packageName.startsWith("Rsp")){
						spiItfBuffer.append("int nRequestID, boolean bIsLast);\n\n");
					}
					else{
						spiItfBuffer.delete(spiItfBuffer.length()-2, spiItfBuffer.length());
						spiItfBuffer.append(");\n\n");
					}
				}				
			}   
			spiItfBuffer.append("}\n");
			writer.write(spiItfBuffer.toString());
	        writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void genAdapterFile(String filePath){
    	String fileName = filePath + "UserApiEntryAdapter.java";
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
			
			StringBuffer adapterBuffer = new StringBuffer();
			adapterBuffer.append("package com.shfe.sfit.monitor.server.userapi;\n\n");
			adapterBuffer.append("import com.shfe.sfit.monitor.server.userapi.fields.*;\n\n");
			adapterBuffer.append("import com.shfe.sfit.monitor.server.ServerMgr;\n");
							
			adapterBuffer.append("public class UserApiEntryAdapter implements FtdcUserApi, FtdcUserSpi  {\n\n");
			adapterBuffer.append("    private FtdcUserApi getUserApi(){\n");
			adapterBuffer.append("        return ServerMgr.getInstance().getUserApi();\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public ApiDataDispatcher getDispatch(){\n");
			adapterBuffer.append("        return ServerMgr.getInstance().getDispatcher();\n");
			adapterBuffer.append("    }\n\n");
			

			adapterBuffer.append("    public void OnFrontConnected() {\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public void OnFrontDisconnected(int nReason) {\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public void OnHeartBeatWarning(int nTimeLapse) {\n");
			adapterBuffer.append("    }\n\n");
			
			Iterator it=packageMap.keySet().iterator();    
			while(it.hasNext()){
				String packageName = it.next().toString();
				if(isSpiPackage(packageName)){
					adapterBuffer.append("    public void On" + packageName + "(");	
					ArrayList<String> fieldList = packageMap.get(packageName);
					for(String fieldName: fieldList){
						adapterBuffer.append(CommonUtils.getFieldName(fieldName) + " " + fieldName + " , ");
					}
					if(packageName.startsWith("Rsp")){
						adapterBuffer.append("int nRequestID, boolean bIsLast){\n");
					}
					else{
						adapterBuffer.delete(adapterBuffer.length()-2, adapterBuffer.length());
						adapterBuffer.append("){\n");
					}
					adapterBuffer.append("    }\n\n");
				}				
			}   
					
			
			adapterBuffer.append("    public final void Release() {\n");;
			adapterBuffer.append("        getUserApi().Release();\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public final void Init() {\n");;
			adapterBuffer.append("        getUserApi().Init();\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public final void RegisterNameServer(String pszNsAddress) {\n");;
			adapterBuffer.append("        getUserApi().RegisterNameServer(pszNsAddress);\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public final void SubscribePrivateTopic(TE_RESUME_TYPE nResumeType) {\n");;
			adapterBuffer.append("        getUserApi().SubscribePrivateTopic(nResumeType);\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public final void SubscribePublicTopic(TE_RESUME_TYPE nResumeType) {\n");;
			adapterBuffer.append("        getUserApi().SubscribePublicTopic(nResumeType);\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public final void SetHeartbeatTimeout(int timeout) {\n");;
			adapterBuffer.append("        getUserApi().SetHeartbeatTimeout(timeout);\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public void RegisterSpi(FtdcUserSpi spi) {\n");;
			adapterBuffer.append("        getDispatch().registSpi(spi);\n");
			adapterBuffer.append("    }\n\n");
			adapterBuffer.append("    public void RegisterFront(String pszFrontAddress) {\n");;
			adapterBuffer.append("        getUserApi().RegisterFront(pszFrontAddress);\n");
			adapterBuffer.append("    }\n\n");
			

			it=packageMap.keySet().iterator();    
			while(it.hasNext()){
				String packageName = it.next().toString();
				if(packageName.startsWith("Req")){
					adapterBuffer.append("    public final int " + packageName + "(");	
					ArrayList<String> fieldList = packageMap.get(packageName);
					for(String fieldName: fieldList){
						adapterBuffer.append(CommonUtils.getFieldName(fieldName) + " " + fieldName + ", ");
					}
					adapterBuffer.append("int nRequestID){\n");
					adapterBuffer.append("        getDispatch().addRequest(nRequestID, this);\n");
					
					if (packageName.compareTo("ReqQrySubscriberTopic") == 0) {
						adapterBuffer.append("        getDispatch().addRtnSubscribe(ReqQrySubscriber.ObjectID, this);\n");						
					}
					adapterBuffer.append("        return getUserApi()." + packageName + "(");
					for(String fieldName: fieldList){
						adapterBuffer.append(fieldName +  ", ");
					}
					adapterBuffer.append("nRequestID);\n");
					adapterBuffer.append("    }\n\n");
				}				
			}
			
			adapterBuffer.append("}\n\n");			
			
			writer.write(adapterBuffer.toString());			
	        writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package zGener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GenApiFile {
	private Map<String, ArrayList<String>> packageMap;
	
	public GenApiFile(DataParser parser){
		packageMap = parser.getPackageMap();
		
	}
	
	public void genApiFile(String filePath){		
		genApiInterface(filePath);
		genApiImpFile(filePath);
		genJNIApiFile(filePath);
	}
	
	private void genApiInterface(String filePath){
    	String fileName = filePath + "FtdcUserApi.java";
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
			
			StringBuffer apiItfBuffer = new StringBuffer();
			apiItfBuffer.append("package com.shfe.sfit.monitor.server.userapi;\n\n");
			apiItfBuffer.append("import com.shfe.sfit.monitor.server.userapi.fields.*;\n\n");
							
			apiItfBuffer.append("public interface FtdcUserApi {\n\n");
			apiItfBuffer.append("    public void Release();\n\n");
			apiItfBuffer.append("    public void Init();\n\n");
			apiItfBuffer.append("    public void RegisterFront(String frontAddress);\n\n");
			apiItfBuffer.append("    public void RegisterNameServer(String nameServerAddr);\n\n");
			apiItfBuffer.append("    public void RegisterSpi(FtdcUserSpi spi);\n\n");
			apiItfBuffer.append("    public void SubscribePrivateTopic(TE_RESUME_TYPE nResumeType);\n\n");
			apiItfBuffer.append("    public void SubscribePublicTopic(TE_RESUME_TYPE nResumeType);\n\n");
			apiItfBuffer.append("    public void SetHeartbeatTimeout(int timeout);\n\n");
			//apiItfBuffer.append("    public int ReqQrySysUserLoginTopic(CShfeFtdcReqQrySysUserLoginField pReqUserLoginField, int nRequestID);\n\n");

			Iterator it=packageMap.keySet().iterator();    
			while(it.hasNext()){
				String packageName = it.next().toString();
				if(packageName.startsWith("Req")){
					apiItfBuffer.append("    public int " + packageName + "(");	
					ArrayList<String> fieldList = packageMap.get(packageName);
					for(String fieldName: fieldList){
						apiItfBuffer.append(CommonUtils.getFieldName(fieldName) + " " + fieldName + ", ");
					}
					apiItfBuffer.append("int nRequestID);\n\n");
				}
				
			}   
			apiItfBuffer.append("}\n");
			writer.write(apiItfBuffer.toString());
	        writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void genApiImpFile(String filePath){
		String fileName = filePath + "\\implement\\FtdcUserApiImpl.java";
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
			
			StringBuffer apiImpBuffer = new StringBuffer();
			setApiImpBufferHeader(apiImpBuffer);
			
			Iterator it=packageMap.keySet().iterator();    
			while(it.hasNext()){
				String packageName = it.next().toString();
				
				if(packageName.startsWith("Req")){
					apiImpBuffer.append("    private native int n" + packageName + "(");

					ArrayList<String> fieldList = packageMap.get(packageName);
					for(String fieldName: fieldList){
						apiImpBuffer.append("ByteBuffer " + fieldName + "Buffer, ");
					}
					apiImpBuffer.append("int nRequestID);\n\n");
					
					apiImpBuffer.append("    public int " + packageName + "(");	
					for(String fieldName: fieldList){
						apiImpBuffer.append(CommonUtils.getFieldName(fieldName) + " " + fieldName + ", ");
					}
					apiImpBuffer.append("int nRequestID){\n");
					
					apiImpBuffer.append("            return n" + packageName + "(");
					for(String fieldName: fieldList){
						apiImpBuffer.append(fieldName + ".toFTDField(), ");
					}
					apiImpBuffer.append("nRequestID);\n");
					apiImpBuffer.append("    }\n\n");
				}				
			}   
			apiImpBuffer.append("}\n");
			writer.write(apiImpBuffer.toString());
	        writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setApiImpBufferHeader(StringBuffer apiImpBuffer){
		apiImpBuffer.append("package com.shfe.sfit.monitor.server.userapi.implement;\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("import java.nio.ByteBuffer;\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("import com.shfe.sfit.monitor.server.userapi.FtdcUserApi;\n");
		apiImpBuffer.append("import com.shfe.sfit.monitor.server.userapi.FtdcUserSpi;\n");
		apiImpBuffer.append("import com.shfe.sfit.monitor.server.userapi.fields.*;\n");
		apiImpBuffer.append("import com.shfe.sfit.monitor.server.ServerMgr;\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("public class FtdcUserApiImpl implements FtdcUserApi{	\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	public static FtdcUserApi getInstance(String flowPath){\n");
		apiImpBuffer.append("		if(m_Instance == null){\n");
		apiImpBuffer.append("			m_Instance = new FtdcUserApiImpl(flowPath);\n");
		apiImpBuffer.append("		}\n");
		apiImpBuffer.append("		return m_Instance;		\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	private FtdcUserApiImpl(String pszFlowPath)\n");
		apiImpBuffer.append("	{\n");
		apiImpBuffer.append("		nCreateFtdcSysUserApi(pszFlowPath);\n");
		apiImpBuffer.append("		m_ftdcUserSpi = new FtdcUserSpiImpl();\n");
		apiImpBuffer.append("		nRegisterSpi(m_ftdcUserSpi);\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	private static FtdcUserApiImpl m_Instance;\n");
		apiImpBuffer.append("	private FtdcUserSpiImpl m_ftdcUserSpi;\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	static {\n");
		apiImpBuffer.append("		System.loadLibrary( " + "\"" + "UserApiJNI" + "\"" + ");\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	private native void nCreateFtdcSysUserApi(String pszFlowPath);\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	private native void nRelease();\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	private native void nInit();\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	private native void nJoin();\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	private native String nGetTradingDay();\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	private native void nRegisterFront(String pszFrontAddress);\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	private native void nSetHeartbeatTimeout(int timeout);\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	private native void nRegisterNameServer(String pszNsAddress);\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	private native void nRegisterSpi(FtdcUserSpiImpl ftpcUserSpi);\n");
		apiImpBuffer.append("			\n");
		apiImpBuffer.append("	private native void nSubscribePrivateTopic(int nResumeType);\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	private native void nSubscribePublicTopic(int nResumeType);\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("	public void Release() {\n");
		apiImpBuffer.append("		nRelease();\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	public void Init() {\n");
		apiImpBuffer.append("		nInit();\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	public void RegisterNameServer(String pszNsAddress) {\n");
		apiImpBuffer.append("		nRegisterNameServer(pszNsAddress);\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	public void SubscribePrivateTopic(TE_RESUME_TYPE nResumeType) {\n");
		apiImpBuffer.append("		nSubscribePrivateTopic(nResumeType.ordinal());\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	public void SubscribePublicTopic(TE_RESUME_TYPE nResumeType) {\n");
		apiImpBuffer.append("		nSubscribePublicTopic(nResumeType.ordinal());\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	public void SetHeartbeatTimeout(int timeout) {\n");
		apiImpBuffer.append("		nSetHeartbeatTimeout(timeout);\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("	\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	public void RegisterSpi(FtdcUserSpi spi) {\n");
		apiImpBuffer.append("		ServerMgr.getInstance().getDispatcher().registSpi(spi);\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("\n");
		apiImpBuffer.append("	public void RegisterFront(String pszFrontAddress) {\n");
		apiImpBuffer.append("		nRegisterFront(pszFrontAddress);\n");
		apiImpBuffer.append("	}\n");
		apiImpBuffer.append("\n");
//		apiImpBuffer.append("	private native int nReqQrySysUserLoginTopic(ByteBuffer reqUserLoginField, int nRequestID);\n");
//		apiImpBuffer.append("\n");
//		apiImpBuffer.append("	public int ReqQrySysUserLoginTopic(CShfeFtdcReqQrySysUserLoginField pReqUserLoginField,\n");
//		apiImpBuffer.append("			int nRequestID) {\n");
//		apiImpBuffer.append("		return nReqQrySysUserLoginTopic(pReqUserLoginField.toFTDField(), nRequestID);\n");
//		apiImpBuffer.append("	}\n\n");
	}

	private void genJNIApiFile(String filePath){
		String fileName = filePath + "..\\..\\..\\..\\..\\..\\..\\jni\\ApiJni\\FtdcUserApiImpl_Auto.cpp";
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
			
			StringBuffer apiJniBuffer = new StringBuffer();
			apiJniBuffer.append("#include \"FtdcUserApiImpl.h\"\n\n");
			apiJniBuffer.append("#ifdef __cplusplus\n");
			apiJniBuffer.append("extern \"C\" {\n");
			apiJniBuffer.append("#endif\n\n");
			
			Iterator it=packageMap.keySet().iterator();    
			while(it.hasNext()){
				String packageName = it.next().toString();

				if(packageName.startsWith("Req")){
					apiJniBuffer.append("JNIEXPORT int JNICALL Java_com_shfe_sfit_monitor_server_userapi_implement_FtdcUserApiImpl_n" + packageName + "\n");
					apiJniBuffer.append("(JNIEnv *env, jobject obj, ");
					
					ArrayList<String> fieldList = packageMap.get(packageName);
					for(String fieldName: fieldList){
						apiJniBuffer.append("jobject " + fieldName + ", ");
					}
					apiJniBuffer.append("jint requestID)\n");
					apiJniBuffer.append("{\n");

					for(String fieldName: fieldList){
						String ftdcFieldName = CommonUtils.getFieldName(fieldName);
						apiJniBuffer.append("    " + ftdcFieldName + "*  req" + fieldName + " = (" + ftdcFieldName + "*)\n");
						apiJniBuffer.append("        env->GetDirectBufferAddress(" + fieldName + ");\n");
					}
					apiJniBuffer.append("    int rstID = requestID;\n");
					apiJniBuffer.append("    return g_FtdcSysUserApi->" + packageName + "(");

					for(String fieldName: fieldList){
						apiJniBuffer.append("req" + fieldName + ", ");
					}
					apiJniBuffer.append("rstID);\n");
					apiJniBuffer.append("}\n\n");
				}				
			}   

			apiJniBuffer.append("#ifdef __cplusplus\n");
			apiJniBuffer.append("}\n");
			apiJniBuffer.append("#endif\n\n");
			writer.write(apiJniBuffer.toString());
	        writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

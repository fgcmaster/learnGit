package zGener;

public class CommonUtils {

	public static String getFieldName(String fieldName){
		return "CShfeFtdc" + fieldName + "Field";
	}
	
	public static boolean isSpiPackage(String packageName){
		if(packageName.startsWith("Rsp") || packageName.startsWith("Rtn") || packageName.startsWith("ErrRtn")){
			return true;
		}
		return false;
	}
}

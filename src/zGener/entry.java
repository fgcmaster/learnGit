package zGener;

import java.util.Iterator;
import java.util.TreeSet;

class GenCode{
	public void gen(){		
		String ftdXmlFile = "D:\\work\\code\\AndroidMonitor\\01Server\\DataModel\\envGenerated\\FTD_UserApi_OK.xml";
		DataParser parse = new DataParser();
		parse.parseFtd(ftdXmlFile);
		
		String userApiPath = "d:\\work\\code\\AndroidMonitor\\02Client\\mytest\\src\\com\\shfe\\sfit\\monitor\\server\\userapi\\";
		String fieldFileName = userApiPath + "fields\\";
		GenFieldFile genFieldFile = new GenFieldFile(parse);
		genFieldFile.genFieldCode(fieldFileName);
		
		GenApiFile genApiFile = new GenApiFile(parse);
		genApiFile.genApiFile(userApiPath);
		
		GenSpiFile genSpiFile = new GenSpiFile(parse);
		genSpiFile.genSpiFile(userApiPath);

	}
}

public class entry {		
	public static void main(String[] args){
		System.out.println("hello");
		
		new GenCode().gen();		
	}
}

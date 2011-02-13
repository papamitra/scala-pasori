package org.papamitra.pasori;
import com.sun.jna.*;

public class tag_felica extends Structure
{
//	public libpafe.tag_pasori.ByReference p;
	public Pointer p;
	public short systemcode;
	/// C type : uint8[8]
	public byte[] IDm = new byte[(8)];
	/// C type : uint8[8]
	public byte[] PMm = new byte[(8)];
	public short area_num;
	/// C type : felica_area[256]
	public felica_area[] area = new felica_area[(256)];
	public short service_num;
	/// C type : felica_area[256]
	public felica_area[] service = new felica_area[(256)];
	/// C type : tag_felica*
	//public tag_felica.ByReference next;
	public Pointer next;

	public tag_felica(){
		super();
	}
	
	public tag_felica(Pointer p){
		super();
		useMemory(p, 0);
		read();
	}
	
	public static class ByReference extends tag_felica implements Structure.ByReference{
	};

	public static class ByValue extends tag_felica implements Structure.ByValue{
	};	
}

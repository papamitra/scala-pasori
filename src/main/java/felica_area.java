package org.papamitra.pasori;
import com.sun.jna.*;

public class felica_area extends Structure
{
	public short code;
	public short attr;
	public short bin;
	/// C type : _ferica_area*
//	public felica_area.ByReference next;
	public Pointer next;

	public felica_area(){
		super();
	}
	
	public felica_area(Pointer p){
		super();
		useMemory(p, 0);
		read();
	}
	
	public static class ByReference extends felica_area implements Structure.ByReference{
	};

	public static class ByValue extends felica_area implements Structure.ByValue{
	};	
}

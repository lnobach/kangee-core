package de.roo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Tuple<Ta,Tb> {

	public Ta getA() {
		return a;
	}

	public void setA(Ta a) {
		this.a = a;
	}

	public Tb getB() {
		return b;
	}

	public void setB(Tb b) {
		this.b = b;
	}

	private Ta a;
	private Tb b;

	public Tuple(Ta a, Tb b) {
		this.a = a;
		this.b = b;
	}
	
	public static <Ta, Tb> List<Tuple<Ta, Tb>> getTupleListFromMap(Map<Ta, Tb> map) {
		List<Tuple<Ta, Tb>> result = new ArrayList<Tuple<Ta,Tb>>();
		
		for (Entry<Ta,Tb> e : map.entrySet()) {
			result.add(new Tuple<Ta, Tb>(e.getKey(), e.getValue()));
		}
		
		return result;
	}

	public static <Ta, Tb> Map<Ta, Tb> getMapFromTupleList(List<Tuple<Ta, Tb>> tuples) {
		Map<Ta, Tb> result = new HashMap<Ta, Tb>();
		for (Tuple<Ta, Tb> t : tuples) {
			result.put(t.getA(), t.getB());
		}
		return result;
	}
	
}

package gov.nga.utils;

public class MutableInt implements Comparable<MutableInt> {

	private int value = 0;

	public MutableInt(int value) {
		this.value = value;
	}

	public void inc () { 
		++value;
	}

	public int  get () { 
		return value; 
	}

	public int compareTo(MutableInt o) {
		return Integer.valueOf(get()).compareTo(Integer.valueOf(o.get()));
	}

}

package controllers.thesaurus.struct;

public class Counter {

	private int count;
	
	public Counter(int i) {
		count = i;
	}
	
	public void increase() {
		count++;
	}

	public void increase(int c) {
		count += c;
	}

	public int increaseUse() {
		count++;
		return count - 1;
	}

	public void decrease() {
		count--;
	}
	
	public int getValue() {
		return count;
	}
	
	public int hashCode() {
		return count;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Counter)) {
			return false;
		}
		
		return count == ((Counter)obj).count;
	}
	
	public String toString() {
		return count + "";
	}
	
}

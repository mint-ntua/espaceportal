package sources.core;

import java.util.Comparator;

public class ValueCount {
	public String value;
	public int count = 0;

	public ValueCount() {
		super();
	}

	public ValueCount(String value, int count) {
		super();
		this.value = value;
		this.count = count;
	}

	public ValueCount(String value) {
		this(value, 1);
	}

	public void add(int c) {
		count += c;
	}

	@Override
	public String toString() {
		return "" + value + "(" + count + ")";
	}

	public static Comparator<ValueCount> comparator() {
		return new Comparator<ValueCount>() {
			@Override
			public int compare(ValueCount o1, ValueCount o2) {
				int val = (int) -Math.signum(o1.count - o2.count);
				if (val == 0)
					return o1.value.compareTo(o2.value);
				return val;
			}
		};
	}
}

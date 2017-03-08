package model.quality;

public class WeightedSum {
	
	private boolean normalizeWeights;
	private double sum;
	private double weightsSum;
	
	public WeightedSum() {
		super();
		init();
	}

	public WeightedSum init() {
		return init(true);
	}
	
	public WeightedSum init(boolean normalize) {
		sum = 0;
		weightsSum = 0;
		setNormalizeWeights(normalize);
		return this;
	}
	
	public boolean isNormalizeWeights() {
		return normalizeWeights;
	}

	public void setNormalizeWeights(boolean normalizeWeights) {
		this.normalizeWeights = normalizeWeights;
	}

	public void add(double weight, double value){
		sum+=(weight*value);
		weightsSum+=weight;
	}
	
	public void add(double weight, boolean value){
		add(weight,value?1:0);
	}
	
	public void add(boolean value){
		add(value?1:0);
	}
	public void add(double value){
		add(1, value);
	}
	
	public double computeWeight(){
		return sum/ (normalizeWeights?weightsSum:1);
	}
	

}

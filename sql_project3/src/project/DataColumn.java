package project;

public class DataColumn {
	private String name;
	private String type;
	private boolean canNull;
	private boolean isPrime;
	private boolean hasIndex;
	
	public DataColumn(String name, String type, 
			boolean canNull, boolean isPrime, boolean hasIndex) {
		this.name = name;
		this.type = type;
		this.canNull = canNull;
		this.isPrime = isPrime;
		this.hasIndex = hasIndex;
	}
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean getNull() {
		return canNull;
	}
	
	public boolean getIndex() {
		return hasIndex;
	}
	
	public boolean getPrime() {
		return isPrime;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setNull(String canNull) {
		this.canNull = Boolean.parseBoolean(canNull);
	}
	
	public void setIndex(String hasIndex) {
		this.hasIndex = Boolean.parseBoolean(hasIndex);
	}
	
	public void setPrime(String isPrime) {
		this.isPrime = Boolean.parseBoolean(isPrime);
	}
}

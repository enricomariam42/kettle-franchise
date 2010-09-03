package be.kjube.plugins.decoder;

public enum DataType {
	A("String"), 
	N("Number"), 
	D("Date"), 
	T("Time")
	;
	
	private String	description;

	private DataType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
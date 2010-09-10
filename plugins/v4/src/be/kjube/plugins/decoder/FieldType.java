package be.kjube.plugins.decoder;

public enum FieldType {
	K("Key"), 
	F("TBD"), 
	E("Element"), 
	A("Appendix");
	
	private String	description;

	private FieldType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}

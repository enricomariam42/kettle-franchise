package be.kjube.util;

public class Kjube {

	public static final String DEFAULT_CUSTOMER_PARAMETER = "KFF_CUSTOMER";
	public static final String DEFAULT_APPLICATION_PARAMETER = "KFF_APPLICATION";
	public static final String DEFAULT_LIFECYCLE_PARAMETER = "KFF_LIFECYCLE";
	
	public static final String DEFAULT_CONFIG_FILE_PATH = "/kff/projects/${KFF_CUSTOMER}/${KFF_APPLICATION}/config/configuration_${KFF_LIFECYCLE}.properties";
	
	public static final String DEFAULT_BATCH_ID_CONNECTION = "${KFF_BATCH_ID_CONNECTION}";
	public static final String DEFAULT_BATCH_ID_SCHEMA = "${KFF_BATCH_ID_SCHEMA}";
	public static final String DEFAULT_BATCH_ID_TABLE  = "${KFF_BATCH_ID_TABLE}";

	public static final String DEFAULT_BATCH_LOGGING_CONNECTION = "${KFF_BATCH_LOGGING_CONNECTION}";
	public static final String DEFAULT_BATCH_LOGGING_SCHEMA = "${KFF_BATCH_LOGGING_SCHEMA}";
	public static final String DEFAULT_BATCH_LOGGING_TABLE  = "${KFF_BATCH_LOGGING_TABLE}";

	public static final String DEFAULT_REJECTS_SCHEMA = "${KFF_REJECTS_SCHEMA}";
	public static final String DEFAULT_REJECTS_TABLE  = "${KFF_REJECTS_TABLE}";

	public static final String DEFAULT_BATCH_ID_VARIABLE_NAME = "${KFF_BATCH_ID}";

	public static final String DEFAULT_ERROR_COUNT_VARIABLE_NAME = "${KFF_ERROR_COUNT_FIELD}";
	public static final String DEFAULT_ERROR_DESCRIPTIONS_VARIABLE_NAME = "${KFF_ERROR_DESCRIPTIONS_FIELD}";
	public static final String DEFAULT_ERROR_FIELDS_VARIABLE_NAME = "${KFF_ERROR_FIELDS_FIELD}";
	public static final String DEFAULT_ERROR_CODES_VARIABLE_NAME = "${KFF_ERROR_CODES_FIELD}";
	
	
	/**
	 * Determines whether or not a character is considered a space.
	 * A character is considered a space in Kettle if it is a space, a tab, a newline or a cariage return.
	 * @param c The character to verify if it is a space.
	 * @return true if the character is a space. false otherwise. 
	 */
	public static final boolean isSpace(char c)
	{
		return c == ' ' || c == '\t' || c == '\r' || c == '\n' || Character.isWhitespace(c);
	}
	
	/**
	 * Left trim: remove spaces to the left of a String.
	 * @param str The String to left trim
	 * @return The left trimmed String
	 */
    public static String ltrim(String source) {
        if (source==null) return null;
		int from = 0;
		while (from < source.length() && isSpace(source.charAt(from)))
			from++;

		return source.substring(from);    	
    }

    /**
	 * Right trim: remove spaces to the right of a string
	 * @param str The string to right trim
	 * @return The trimmed string.
	 */
    public static String rtrim(String source) {
		if (source==null) return null;
		
		int max = source.length();
		while (max > 0 && isSpace(source.charAt(max - 1)))
			max--;

		return source.substring(0, max);
    }

	/**
	 * Trims a string: removes the leading and trailing spaces of a String.
	 * @param str The string to trim
	 * @return The trimmed string.
	 */
	public static final String trim(String str)
	{
		if (str==null) return null;
		
		int max = str.length() - 1;
		int min = 0;	

		while (min <= max && isSpace(str.charAt(min)))
			min++;
		while (max >= 0 && isSpace(str.charAt(max)))
			max--;

		if (max < min)
			return "";

		return str.substring(min, max + 1);
	}

}

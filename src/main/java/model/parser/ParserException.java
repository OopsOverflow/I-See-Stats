package model.parser;

public class ParserException extends Exception {
	private static final long serialVersionUID = 2804740549723047963L;
	
	public enum Type {
		FILE_NOT_FOUND,
		JSON_MALFORMED,
		JSON_INVALID,
		NETWORK_ERROR
	}

	public ParserException(Type type) {
		super(getMessage(type));
	}
	
	public ParserException(Type type, Throwable cause) {
		super(getMessage(type), cause);
	}
	
	private static String getMessage(Type type) {
		String str;
		
		if(type == Type.FILE_NOT_FOUND)
			str = "file not found or unreadable";
		else
			str = "unknown parser exception";
		
		return str;
	}
	
}

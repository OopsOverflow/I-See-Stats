package model.parser;

public class ParserException extends Exception {
	private static final long serialVersionUID = 2804740549723047963L;
	
	public enum Type {
		FILE_NOT_FOUND,
		JSON_MALFORMED,
		JSON_PARSE_ERROR,
		NETWORK_ERROR
	}
	
	private Type type;

	public ParserException(Type type) {
		super(getMessage(type));
		this.type = type;
	}
	
	public ParserException(Type type, Throwable cause) {
		super(getMessage(type), cause);
		this.type = type;
	}
	
	private static String getMessage(Type type) {
		String str;
		
		if(type == Type.FILE_NOT_FOUND)
			str = "file not found or unreadable";
		else if(type == Type.JSON_MALFORMED)
			str = "The JSON data is not compatible with the intended api";
		else if(type == Type.JSON_PARSE_ERROR)
			str = "the data failed to be parsed tu JSON";
		else if(type == Type.NETWORK_ERROR)
			str = "network error";
		else
			str = "unknown parser exception";
		
		return str;
	}
	
	public Type getType() {
		return type;
	}
	
}

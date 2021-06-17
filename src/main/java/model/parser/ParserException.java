package model.parser;

public class ParserException extends Exception {
	private static final long serialVersionUID = 2804740549723047963L;
	
	public ParserException(String msg) {
		super(msg);
	}
	
	public ParserException(String msg, Exception e) {
		super(msg, e);
	}
}

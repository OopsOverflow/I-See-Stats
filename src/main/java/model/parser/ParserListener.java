package model.parser;
import java.util.EventListener;


public interface ParserListener<T> extends EventListener {
	
	void onSuccess(T result);
	
	void onError(ParserException e);
}

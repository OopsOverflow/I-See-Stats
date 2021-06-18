package model.parser;
import java.util.EventListener;

/**
 * A listener interface for success / errors on parser {@link ParserQuery queries}
 *
 * @param <T> - the type returned by the query
 */
public interface ParserListener<T> extends EventListener {
	
	void onSuccess(T result);
	
	void onError(ParserException e);
}

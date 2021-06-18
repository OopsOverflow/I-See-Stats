package model.parser;

import java.util.ArrayList;

public class ParserQuery<T> {
	
	private ArrayList<ParserListener<T>> listeners;
	private T res;
	private ParserException err;
	
	ParserQuery() {
		listeners = new ArrayList<ParserListener<T>>();
	}
	
	public void addEventListener(ParserListener<T> listener) {
		listeners.add(listener);
		if(res != null) listener.onSuccess(res);
		if(err != null) listener.onError(err);
	}
	
	ParserQuery<T> fireSuccess(T result) {
		for(ParserListener<T> listener : listeners) {
			listener.onSuccess(result);
		}
		
		res = result;
		return this;
	}
	
	ParserQuery<T> fireError(ParserException e) {
		for(ParserListener<T> listener : listeners) {
			listener.onError(e);
		}
		
		err = e;
		return this;
	}
}

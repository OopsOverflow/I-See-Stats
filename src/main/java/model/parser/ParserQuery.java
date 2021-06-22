package model.parser;

import gui.AlertBaker;

import java.util.ArrayList;

/**
 * A asynchronous wrapper around {@link Parser} queries.
 *
 * @param <T> - the type of the async result
 */
public class ParserQuery<T> {

	private ArrayList<ParserListener<T>> listeners;
	private T res;
	private ParserException err;

	ParserQuery() {
		listeners = new ArrayList<ParserListener<T>>();
	}

	/**
	 * Adds an event listener called in case of success / error.
	 * If the query was already resolved, the listener will be fired immediatly.
	 * @param listener - the success / error event listener
	 */
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
		if (err.getType() != ParserException.Type.FILE_NOT_FOUND)
			AlertBaker.bakeError(err.getType());

		return this;
	}
}

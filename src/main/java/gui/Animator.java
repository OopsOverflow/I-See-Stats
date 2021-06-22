package gui;

import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import model.parser.ParserException;
import model.parser.ParserListener;
import model.species.SpeciesData;

public class Animator extends AnimationTimer implements ParserListener<ArrayList<SpeciesData>> {
	
	private DoubleProperty progress; // between 0 and 100
	private DoubleProperty duration; // seconds from start to finish
	private BooleanProperty loaded;
	
	private long timerStart;
	private boolean isStarted;
	private ArrayList<ParserListener<SpeciesData>> listeners;
	private SpeciesData curFrameData;
	private ArrayList<SpeciesData> data;
	
	public Animator() {
		progress = new SimpleDoubleProperty(0.0);
		duration = new SimpleDoubleProperty(10.0);
		loaded = new SimpleBooleanProperty(false);
		progress.addListener((_1) -> updateCurrentFrame());
		listeners = new ArrayList<ParserListener<SpeciesData>>();
	}

	@Override
	public void onSuccess(ArrayList<SpeciesData> result) {
		data = result;
		loaded.set(true);
	}

	@Override
	public void onError(ParserException e) {
		AlertBaker.bakeError(e.getType());
		loaded.set(false);
	}
	
	@Override
	public void stop() {
		super.stop();
		isStarted = false;
	}
	
	public void unload() {
		stop();
		data = null;
		curFrameData = null;
		loaded.set(false);
	}
	
	public ReadOnlyBooleanProperty loadedProperty() {
		return loaded;
	}
	
	public DoubleProperty progressProperty() {
		return progress;
	}
	
	public DoubleProperty durationProperty() {
		return duration;
	}
	
	public void addListener(ParserListener<SpeciesData> listener) {
		listeners.add(listener);
	}
	
	private double timestampToSlider(long t) {
		return (double)t / 1e9 / duration.get() * 100.0;
	}
	
	private long sliderToTimestamp(double t) {
		return (long)(t * 1e9 * duration.get() / 100.0);
	}
	
	@Override
	public void handle(long now) {
		if(!isStarted) {
			isStarted = true;
			timerStart = now - sliderToTimestamp(progress.get());
		}
		
		double t = timestampToSlider(now - timerStart);
		
		if(t > 100.0) {
			t = 100.0;
			stop();
		}
		
		progress.set(t);	
	}
	
	private void fire() {
		for(ParserListener<SpeciesData> listener : listeners) {
			listener.onSuccess(curFrameData);
		}
	}
	
	private void updateCurrentFrame() {
		if(data == null) return;
		
		int index = (int)(progress.get() / 100.0 * (double)data.size());
		index = Math.min(index, data.size() - 1);
		SpeciesData newData = data.get(index);
		
		if(curFrameData == null || newData != curFrameData) {
			curFrameData = newData;
			fire();
		}			
	}
}

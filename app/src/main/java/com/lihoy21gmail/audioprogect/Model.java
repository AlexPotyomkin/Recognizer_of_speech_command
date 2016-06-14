package com.lihoy21gmail.audioprogect;

import java.util.Observable;

public class Model extends Observable {
    private static Model instance;
    private int SpeechRecognitionResult;

    private Model() {
    }

    public int getSpeechRecognitionResult() {
        return SpeechRecognitionResult;
    }

    public void setSpeechRecognitionResult(int speechRecognitionResult) {
        SpeechRecognitionResult = speechRecognitionResult;
        setChanged();
        notifyObservers();
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }
}

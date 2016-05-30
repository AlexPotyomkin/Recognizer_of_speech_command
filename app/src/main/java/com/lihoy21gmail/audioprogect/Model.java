package com.lihoy21gmail.audioprogect;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class Model extends Observable {
    private static Model instance;
    private int myBufferSize = 66150;
    private int sampleRate = 22050;
    private short[] myBuffer = new short[myBufferSize];
    private List<short[]> array_of_words = new ArrayList<>();
    private int M;
    double array_of_MFCC[][];

    private Model(){}

    public List<short[]> getArray_of_words() {
        return array_of_words;
    }

    public void setArray_of_words(List<short[]> array_of_words) {
        this.array_of_words = array_of_words;
    }

    public static Model getInstance(){
        if(instance == null){
            instance = new Model();
        }
        return instance;
    }

    public int getMyBufferSize() {
        return myBufferSize;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public short[] getMyBuffer() {
        return myBuffer;
    }

    public void setMyBuffer(short[] myBuffer) {
        this.myBuffer = myBuffer;
    }

    public double[][] getArray_of_MFCC() {
        return array_of_MFCC;
    }

    public void setArray_of_MFCC(double[][] array_of_MFCC) {
        this.array_of_MFCC = array_of_MFCC;
        setChanged();
        notifyObservers();
    }
}

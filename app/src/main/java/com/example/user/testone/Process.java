package com.example.user.testone;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

/**
 * Created by theone on 4/17/15.
 */
public class Process {

    // Identification data
    private int processId;
    private String processName;

    // state
    int programCounter;
    int instructionReg;
    int statusReg;
    int stackPointer;
    int base;
    int limit;
    int[] register;
    int stackSize;
    int topOfStack;
    PageTable pageTable;
    int clock;

    // statistical data
    int cpuTime;
    int waitTime;
    int turnAroundTime;
    int ioTime;
    int interruptTime;
    int oldTime;


    // some other data
    int timeSlice;
    int programSize = 0;
    boolean pagedOut;
    FileOutputStream fos;
    FileInputStream fin;
    RandomAccessFile accessFile;

    //private Context mContext;

    /**
     * Constructor
     */
    Process(String processName, int processId, int size){
        this.processName = processName;
        this.processId = processId;
        setPageTable(size);
    }

    /**
     * Calculates the size of the page table
     * @throws IOException
     */
    public void setPageTable(int programSize){
        /*
        try {
            FileInputStream fis = mContext.openFileInput(name + ".o");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null)
                programSize++;
            reader.close();
        } catch (IOException e) {
            // Let's handle this further up the call stack
            throw e;
        }*/
        if( programSize < 8 )
    }

}

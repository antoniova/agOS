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
    int processId;
    int id;
    String name;

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
    int programSize;
    boolean pagedOut;
    FileOutputStream fos;
    FileInputStream fin;
    RandomAccessFile accessFile;


    private Context mContext;

    /**
     * Constructor
     */
    Process(String processName, Context context){
        this.name = processName;
        mContext = context;
        programSize = 0;
    }

    /**
     * Constructor
     */
    Process(String processName, int processId, Context context){
        name = processName;
        id = processId;
        mContext = context;
        programSize = 0;
    }

    /**
     * Calculates the size of the page table
     * @throws IOException
     */
    public void setPageTableSize() throws IOException{
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
        }
    }

}

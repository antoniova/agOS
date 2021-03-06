package com.example.user.testone;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by theone on 4/17/15.
 */
public class OperatingSystem extends AsyncTask<Void, Void, Void>{

    static final int FRAME_COUNT = 32;
    static final int FRAME_SIZE = 8;
    static final int PAGE_SIZE = FRAME_SIZE;
    static final int SHORT_WIDTH = 2;

    List<String> objectFileList;

    /**
     * A global list containing all the processes
     * After all processes are executed, we still need to access the state of all
     * the processes that were executed
     */
    Queue<Process> staticTaskVector;

    /**
     * unlike the static task vector, this task vector is used to hold processes
     * until they're ready to be scheduled; at which point they will be removed
     * from the list
     */
    Queue<Process> taskVector;

    // holds all free frames
    List<Integer> freeFrameList;

    /*
     * A FIFO queue. Used to queue jobs for processor times. This is the main method for
     * running processes. Since the total number of processes can be arbitrarily large, only
     * five processes are kept in the ready queue at a time. As jobs are run to completion, more
     * processes can be brought into the ready queue from the global list of processes
     */
    Queue<Process> readyQueue;

    /**
     *
     */
    Queue<Process> waitQueue;

    Context mContext;
    Process runningProcess;
    VirtualMachine mVMachine;

    OperatingSystem(Context context, List[] fileList){
        mContext = context;
        //objectFileList = new ArrayList<>(fileList);
        staticTaskVector = new LinkedList<>();
        taskVector = new LinkedList<>();
        freeFrameList = new LinkedList<>();
        readyQueue = new LinkedList<>();
        mVMachine = new VirtualMachine();
    }

    @Override
    protected Void doInBackground(Void... voids){
        return null;
    }

    @Override
    protected void onPostExecute(Void v){

    }

    public void execute(){

    }

    /**
     * The Runnable interface method
     */
    public void run(){

        this.init();
        //queueInitialJobs();
        //while(true)
            //runReaydJob();
    }

    private void init() {
        // initiazialize free frame list
        for(int i = 0; i < FRAME_COUNT; i++)
            freeFrameList.add(i);

        // Create process list. No need to get fancy with process_id generation
        // must start with pid = 1 because pid = 0 is reserved
        int pid = 1;
        for (String name : objectFileList) {
            String[] temp = name.split("\\.");
            //staticTaskVector.add(new Process(temp[0], pid++, mContext));
        }

        // Let's populate the temporary process list
        for(Process t : staticTaskVector)
            taskVector.add(t);

    }

    /*n
    private void loadPage(Process process, int  processId, int frame, int page){
        //Process process = findProcess(processId);
        int startAddress = frame * FRAME_SIZE;
        try {
            RandomAccessFile file = new RandomAccessFile(process.name + ".o", "rw");
            FileInputStream fis = new FileInputStream(file.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            process.fin = mContext.openFileInput(process.name + ".o");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.fin));
            // skip a certain number of lines corresponding to the page number (starting at 0)
            // for instance, if we must load page[1], we skip 8 lines
            for(int skipCount = page * PAGE_SIZE; skipCount > 0; skipCount--)
                reader.readLine();
            for(int i = 0; i < PAGE_SIZE; i++)
                mVMachine.memory[startAddress++] = Short.parseShort(reader.readLine());
            if(reader != null)
                reader.close();
        } catch (IOException e) {
            Log.d("MAIN_ACTIVITY", "Unable to open object file");
        }
        // update process page table entry
        process.pageTable.table[page].frame = frame;
        process.pageTable.table[page].validBit = true;
        // update inverted table
        mVMachine.invertedTable[frame].pageNumber = page;
        mVMachine.invertedTable[frame].pid = processId;

    }*/


    /**
     * Searches the taskVector list using the process id as the search criterion.
     * If the process is not found, null is returned. It is up to the calling method
     * to check for null return values
     * @param processId
     * @return
     */
    private Process findProcess(int processId){
        for(Process t : taskVector){
            if(t.id == processId)
                return t;
        }
        return null;
    }

    /**
     * Queues up an initial set of jobs. REa
     */

    private void queueInitialJobs(){
        // At least for now, we restrict the ready queue to five
        // processes
        if(taskVector.size() > 5)
            enqueueJobs(5);
        else
            enqueueJobs(taskVector.size());

        while(!readyQueue.isEmpty() || !waitQueue.isEmpty()){
            loadNextProcess();
            mVMachine.runProcess();//todo
            saveProcessState();
            switch( mVMachine.returnStatus ){
                /*
                case 0: if( PAGE_FAULT || OVERFLOW ){
                    systemTrap();
                    break;
                }
                    queryWaitQueue();
                    pushIntoReadyQ(running);
                    break;
                case 6: ioTrap(); // read instruction
                    queryWaitQueue();
                    break;
                case 7: ioTrap(); // write instruction
                    queryWaitQueue();
                    break;
                default: queryWaitQueue();
                    systemTrap();//all others are system traps
                    freeMemory();
                    scheduleJobs(1);
                    */
                case VirtualMachine.TIME_SLICE:
                    if(mVMachine.pageFaultFlag)
                        //do something
                    if(mVMachine.overflowFlag)
                        // do something

                    break;
                case VirtualMachine.READ_OPERATION:
                case VirtualMachine.WRITE_OPERATION:
                    //ioTrap();
                    break;
                default:

            }
        }
        outputStatistics();
    }



    private void enqueueJobs(int processCount){
        Process temp;
        int frame;
        if(!taskVector.isEmpty()){
            while( processCount > 0 ){
                temp = taskVector.remove();
                pushIntoReadyQ(temp);
                if(!freeFrameList.isEmpty()){


                }
            }


        }



    }

    private void loadNextProcess(){

    }

    private void saveProcessState(){

    }

    private void systemTrap(){

    }

    /*
    private void ioTrap(){
        if(mVMachine.returnStatus == VirtualMachine.READ_OPERATION)
            //do something
        else
         // do something


    }
    */

    /**
     * Frees memory used by a process once such process is finished execution.
     * This is done by relinquishing any used frames and adding then to the free
     * frames list
     */
    private void freeMemory(){
        for(int i = 0; i < runningProcess.pageTable.table.length; i++){
            if(runningProcess.pageTable.table[i].validBit){
                int frame = runningProcess.pageTable.table[i].frame;
                freeFrameList.add(frame);
                mVMachine.invertedTable[frame].pageNumber = 0;
                mVMachine.invertedTable[frame].pid = 0;
            }
        }
    }

    private void queryWaitQueue(){

    }

    private void pushIntoReadyQ(Process process){

    }

    private void outputStatistics(){

    }

    private void stackFrameRequest(int frame){
        VirtualMachine.InvertedTableEntry entry = mVMachine.invertedTable[frame];
        Process temp = findProcess(entry.pid);
        if(temp.pageTable.table[entry.pageNumber].dirtyBit){
            writePage( entry.pid, frame, entry.pageNumber);
        }else{
            temp.pageTable.clearEntry(entry.pageNumber);
            mVMachine.invertedTable[frame].pageNumber = 0;
            mVMachine.invertedTable[frame].pid = 0;
        }

    }

    private void writePage(int pid, int frame, int page){
        Process process = findProcess(pid);
        File file = new File(mContext.getFilesDir(), process.name + ".o");
        //TODO remove the file.exists() check
        if(file.exists()){
            int initAddress = frame * PAGE_SIZE;
            byte[] buf = new byte[2];
            try {
                process.accessFile = new RandomAccessFile(file, "rw");
                // Let's position the cursor in the proper location
                process.accessFile.seek(page * SHORT_WIDTH * PAGE_SIZE);
                for(int i = 0; i < PAGE_SIZE ; i++){
                    short temp = (short) mVMachine.memory[initAddress++];
                    buf[0] = (byte)(temp >> 8);
                    buf[1] = (byte) temp;
                    process.accessFile.write(buf);
                }
                process.accessFile.close();
            } catch (IOException e) {
                Log.d("MAIN_ACTIVITY", "Unable to open object file");
            }
        }else{
            Log.d("MAIN_ACTIVITY", "File: " + file.toString() + " does not exist.");
        }
    }


    static private class Process{
        // Identification data
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
        int programSize = 0;
        boolean pagedOut;
        RandomAccessFile accessFile;

        //private Context mContext;

        /**
         * Constructor
         */
        Process(String processName, int processId, int size){
            name = processName;
            id = processId;
            pageTable = new PageTable(size);
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
        }

    } // end of Process class


}


package com.example.user.testone;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Assembler implements Runnable {

    interface Instruction{
        void exec();
    }

    // opcode constants
    private static final int LOAD_OP 	= 0;
    private static final int LOADI_OP 	= 0;
    private static final int STORE_OP 	= 1;
    private static final int ADD_OP 	= 2;
    private static final int ADDI_OP 	= 2;
    private static final int ADDC_OP 	= 3;
    private static final int ADDCI_OP 	= 3;
    private static final int SUB_OP		= 4;
    private static final int SUBI_OP	= 4;
    private static final int SUBC_OP 	= 5;
    private static final int SUBCI_OP	= 5;
    private static final int AND_OP		= 6;
    private static final int ANDI_OP	= 6;
    private static final int XOR_OP		= 7;
    private static final int XORI_OP	= 7;
    private static final int COMPL_OP 	= 8;
    private static final int SHL_OP		= 9;
    private static final int SHLA_OP	= 10;
    private static final int SHR_OP		= 11;
    private static final int SHRA_OP	= 12;
    private static final int COMPR_OP	= 13;
    private static final int COMPRI_OP	= 13;
    private static final int GETSTAT_OP	= 14;
    private static final int PUTSTAT_OP	= 15;
    private static final int JUMP_OP	= 16;
    private static final int JUMPL_OP	= 17;
    private static final int JUMPE_OP	= 18;
    private static final int JUMPG_OP	= 19;
    private static final int CALL_OP	= 20;
    private static final int RET_OP		= 21;
    private static final int READ_OP	= 22;
    private static final int WRITE_OP	= 23;
    private static final int HALT_OP	= 24;
    private static final int NOOP_OP	= 25;
    private static final String SUCCESS = "Compilation finished successfully";
    // collections
    Map<String, Instruction> instructionSet;
    List<String> origSourceCode;
    List<String> strippedCode;
    List<String> sourceCode;
    ArrayList<Short> objectCode;
    HashMap<String, Integer> symbolTable;

    private String opcode;
    private int rdest, rsource, constant, address;
    private boolean error = false;
    private Tokenizer tokenizer;
    private Integer lineNumber = 0;
    private String fileName;
    private String objectFileName;
    boolean inSecondPass;
    Context mContext;
    Handler mHandler;
    Handler.Callback mEvent;

    // Constructor
    Assembler(Context context, List<String> code, String name, Handler handler){
        mContext = context;
        fileName = name;
        inSecondPass = false;
        mHandler = handler;
        origSourceCode =  code;
        objectCode  = new ArrayList<Short>();
        strippedCode = new ArrayList<>();
        sourceCode = new ArrayList<>();
        symbolTable = new HashMap<String, Integer>();
        instructionSet = new HashMap<String,Instruction>();
        instructionSet.put("load", load);
        instructionSet.put("loadi", loadi);
        instructionSet.put("store", store);
        instructionSet.put("add", add);
        instructionSet.put("addi", addi);
        instructionSet.put("addc", addc);
        instructionSet.put("addci", addci);
        instructionSet.put("sub", sub);
        instructionSet.put("subi", subi);
        instructionSet.put("subc", subc);
        instructionSet.put("subci", subci);
        instructionSet.put("and", and);
        instructionSet.put("andi", andi);
        instructionSet.put("xor", xor);
        instructionSet.put("xori", xori);
        instructionSet.put("compl", compl);
        instructionSet.put("shl", shl);
        instructionSet.put("shla", shla);
        instructionSet.put("shr", shr);
        instructionSet.put("shra", shra);
        instructionSet.put("compr", compr);
        instructionSet.put("compri", compri);
        instructionSet.put("getstat", getstat);
        instructionSet.put("putstat", putstat);
        instructionSet.put("jump", jump);
        instructionSet.put("jumpl", jumpl);
        instructionSet.put("jumpe", jumpe);
        instructionSet.put("jumpg", jumpg);
        instructionSet.put("call", call);
        instructionSet.put("return", ret);
        instructionSet.put("read", read);
        instructionSet.put("write", write);
        instructionSet.put("halt", halt);
        instructionSet.put("noop", noop);
    }

    /**
     * Runnable interface method
     */
    public void run(){
        assemble();

        Message msg = Message.obtain();
        //msg.obj = new String("all done");
        msg.obj = "Compilation finished successfully";
        mHandler.sendMessage(msg);
        //printObjectCode();

    }

    public void assemble(){
        if(!firstPass()){
            Message msg = Message.obtain();
            msg.obj = new String("During first pass. Error in line: " + lineNumber.toString() +
                    ": Label symbol already used.");
            //mEvent.handleMessage(msg);
            mHandler.sendMessage(msg);
            /*
            mEvent.errorEvent("During first pass. Error in line: " + lineNumber.toString() +
            ": Label symbol already used.");
            */
            return;
        }

        if(!secondPass()){
            //mEvent.errorEvent("During second pass. Error in line: ");
            return;
        }

        //printSourceCode();
        if(!thirdPass()){
            //mEvent.errorEvent("During third");
            return;
        }

        saveObjectCode();

        //mEvent.successEvent("Assembly complete. File \"" +
          //     objectFileName + "\" has been saved");
    }

    /**
     * The first pass of the assembly process. As it should be recalled,
     * every instruction has the following format: LABEL:;OPCODE AND ARGUMENTS; #COMMENT
     * where the semicolons are used for easy parsing. This method has two functions:
     * (1) It strips the comment and label sections from each line and stores the
     * remaining instruction in the strippedCode array list.
     * (2) It builds the label symbol table,
     */
    public boolean firstPass(){
        lineNumber = 0;
        for(String t : origSourceCode){
            // First, let's extract the label field from the instruction (if any)
            String [] tempArray = t.split(";");
            if(!tempArray[0].isEmpty()){
                // Now let's extract the label text. We're discarding the semi-colon
                String [] anotherTempArray = tempArray[0].split(":");
                // And finally, let's add a new entry into the symbol table
                // Must make sure that no symbol is repeated.
                if(!symbolTable.containsKey(anotherTempArray[0])){
                    symbolTable.put(anotherTempArray[0], lineNumber);
                }else{
                    return false;
                }
            }
            // Let's add the opcode and its arguments to strippedCode array
            strippedCode.add(tempArray[1]);
            lineNumber++;
        }
        return true;
    }

    /**
     * Second pass: Symbol resolution
     * @return
     */
    public boolean secondPass(){
        lineNumber = 0;
        error = false;
        inSecondPass = true;
        for(String line : strippedCode){
            if( !(line.trim()).isEmpty() ){ // Empty lines will now be omitted
                tokenizer = new Tokenizer(line);
                opcode = tokenizer.nextToken();
                switch (opcode) {
                    case "load":
                        instructionSet.get(opcode).exec();
                        break;
                    case "store":
                        instructionSet.get(opcode).exec();
                        break;
                    case "jump":
                        instructionSet.get(opcode).exec();
                        break;
                    case "jumpl":
                        instructionSet.get(opcode).exec();
                        break;
                    case "jumpe":
                        instructionSet.get(opcode).exec();
                        break;
                    case "jumpg":
                        instructionSet.get(opcode).exec();
                        break;
                    case "call":
                        instructionSet.get(opcode).exec();
                        break;
                    default:
                        sourceCode.add(line);
                }
            }
            if(error)
                return false;
            lineNumber++;
        }
        return true;
    }

    /**
     * Third pass (compilation): translates from assembly language
     * to machine language.
     * @return
     */
    public boolean thirdPass(){
        lineNumber = 0;
        error = false;
        inSecondPass = false;
        for(String line : sourceCode){
            tokenizer = new Tokenizer(line);
            opcode = tokenizer.nextToken();
            instructionSet.get(opcode).exec();
            if(error)
                return false;
            lineNumber++;
        }

        return true;
    }

    public void printSymbolTable(){
        Set<String> set = symbolTable.keySet();

        for(String i : set){
            Log.d("SYMBOL_TABLE", i + ":" + symbolTable.get(i));
        }
    }

    public void printStrippedCode(){
        for(String t : strippedCode)
            Log.d("STRIPPED_CODE", t);
    }
    public void printSourceCode(){
        for(String t : sourceCode)
            Log.d("SOURCE_CODE", t);
    }

    public void printObjectCode()
    {
        for(Short i : objectCode)
            Log.d("OBJECT_CODE", i.toString());
    }

    void saveObjectCode() {
        String [] tempArray = fileName.split("\\.");
        objectFileName = tempArray[0] + ".o";
        byte[] buf = new byte[2];
        try{
            FileOutputStream fos = mContext.openFileOutput(objectFileName, Context.MODE_PRIVATE);
            BufferedOutputStream outputStream = new BufferedOutputStream(fos);
            for(short t : objectCode){
                buf[0] = (byte) (t >> 8);
                buf[1] = (byte) t;
                outputStream.write(buf);
            }
            outputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**********************************************************
     *
     *   INSTRUCTIONS
     *
     **********************************************************/

    Instruction load = new Instruction(){
        public void exec(){
            if(inSecondPass){
                String dest = tokenizer.nextToken(); // hold this temporarily
                String address = tokenizer.nextToken();
                if(symbolTable.containsKey(address)){
                    sourceCode.add(opcode + " " + dest + " " + symbolTable.get(address));
                }else{
                    error = true;
                }
            }else {
                rdest = (Integer.valueOf(tokenizer.nextToken()));
                address = (Integer.valueOf(tokenizer.nextToken()));
                if (validAddress())
                    objectCode.add((short) ((LOAD_OP << 11) | (rdest << 9) | address));
                else
                    invalidAddressError();
            }
        }
    };

    Instruction loadi = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            constant = (Integer.valueOf( tokenizer.nextToken()));
            if(validConstant())
                objectCode.add((short) ( (LOADI_OP << 11)|(rdest << 9)|(1 << 8)|(constant & 0xff) ) );
            else
                invalidConstantError();
        }
    };

    Instruction store = new Instruction(){
        public void exec(){
            if(inSecondPass){
                String dest = tokenizer.nextToken(); // hold this temporarily
                String address = tokenizer.nextToken();
                if(symbolTable.containsKey(address)){
                    sourceCode.add(opcode + " " + dest + " " + symbolTable.get(address));
                }else{
                    error = true;
                }
            }else {
                rdest = (Integer.valueOf(tokenizer.nextToken()));
                address = (Integer.valueOf(tokenizer.nextToken()));
                if (validAddress())
                    objectCode.add((short) ((STORE_OP << 11) | (rdest << 9) | address));
                else
                    invalidAddressError();
            }
        }
    };

    Instruction add = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            rsource = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short) ( (ADD_OP << 11)|(rdest << 9)|(rsource << 6) ) );
        }
    };

    Instruction addi = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            constant = (Integer.valueOf( tokenizer.nextToken()));
            if(validConstant())
                objectCode.add((short) ( (ADDI_OP << 11)|(rdest << 9)|(1 << 8)|(constant & 0xff) ) );
            else
                invalidConstantError();
        }
    };

    Instruction addc = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            rsource = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short) ( (ADDC_OP << 11)|(rdest << 9)|(rsource << 6) ) );
        }
    };

    Instruction addci = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            constant = (Integer.valueOf( tokenizer.nextToken()));
            if(validConstant())
                objectCode.add((short)( (ADDCI_OP << 11) | (rdest << 9) | (1 << 8) | (constant & 0xff) ));
            else
                invalidConstantError();
        }
    };

    Instruction sub = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            rsource = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (SUB_OP << 11) | (rdest << 9) | (rsource << 6) ));
        }
    };

    Instruction subi = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            constant = (Integer.valueOf( tokenizer.nextToken()));
            if(validConstant())
                objectCode.add((short)( (SUBI_OP << 11) | (rdest << 9) | (1 << 8) | (constant & 0xff) ));
            else
                invalidConstantError();
        }
    };

    Instruction subc = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            rsource = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (SUBC_OP << 11) | (rdest << 9) | (rsource << 6) ));
        }
    };

    Instruction subci = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            constant = (Integer.valueOf( tokenizer.nextToken()));
            if(validConstant())
                objectCode.add((short)( (SUBCI_OP << 11) | (rdest << 9) | (1 << 8) | (constant & 0xff) ));
            else
                invalidConstantError();
        }
    };

    Instruction and = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            rsource = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (AND_OP << 11) | (rdest << 9) | (rsource << 6) ));
        }
    };

    Instruction andi = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            constant = (Integer.valueOf( tokenizer.nextToken()));
            if(validConstant())
                objectCode.add((short)( (ANDI_OP << 11) | (rdest << 9) | (1 << 8) | (constant & 0xff) ));
            else
                invalidConstantError();
        }
    };

    Instruction xor = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            rsource = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (XOR_OP << 11) | (rdest << 9) | (rsource << 6) ));
        }
    };

    Instruction xori = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            constant = (Integer.valueOf( tokenizer.nextToken()));
            if(validConstant())
                objectCode.add((short)( (XORI_OP << 11) | (rdest << 9) | (1 << 8) | (constant & 0xff) ));
            else
                invalidConstantError();
        }
    };

    Instruction compl = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (COMPL_OP << 11) | (rdest << 9) ));
        }
    };

    Instruction shl = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (SHL_OP << 11) | (rdest << 9) ));
        }
    };

    Instruction shla = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (SHLA_OP << 11) | (rdest << 9) ));
        }
    };

    Instruction shr = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (SHR_OP << 11) | (rdest << 9) ));
        }
    };

    Instruction shra = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (SHRA_OP << 11) | (rdest << 9) ));
        }
    };

    Instruction compr = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            rsource = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (COMPR_OP << 11) | (rdest << 9) | (rsource << 6) ));
        }
    };

    Instruction compri = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            constant = (Integer.valueOf( tokenizer.nextToken()));
            if(validConstant())
                objectCode.add((short)( (COMPRI_OP << 11) | (rdest << 9) | (1 << 8) | (constant & 0xff) ));
            else
                invalidConstantError();
        }
    };

    Instruction getstat = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (GETSTAT_OP << 11) | (rdest << 9) ));
        }
    };

    Instruction putstat = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (PUTSTAT_OP << 11) | (rdest << 9) ));
        }
    };

    Instruction jump = new Instruction(){
        public void exec(){
            if(inSecondPass){
                String address = tokenizer.nextToken();
                if(symbolTable.containsKey(address)){
                    sourceCode.add(opcode + " " + symbolTable.get(address));
                }else{
                    error = true;
                }
            }else {
                address = (Integer.valueOf(tokenizer.nextToken()));
                if (validAddress())
                    objectCode.add((short) ((JUMP_OP << 11) | address));
                else
                    invalidAddressError();
            }
        }
    };

    Instruction jumpl = new Instruction(){
        public void exec(){
            if(inSecondPass){
                String address = tokenizer.nextToken();
                if(symbolTable.containsKey(address)){
                    sourceCode.add(opcode + " " + symbolTable.get(address));
                }else{
                    error = true;
                }
            }else {
                address = (Integer.valueOf(tokenizer.nextToken()));
                if (validAddress())
                    objectCode.add((short) ((JUMPL_OP << 11) | address));
                else
                    invalidAddressError();
            }
        }
    };

    Instruction jumpe = new Instruction(){
        public void exec(){
            if(inSecondPass){
                String address = tokenizer.nextToken();
                if(symbolTable.containsKey(address)){
                    sourceCode.add(opcode + " " + symbolTable.get(address));
                }else{
                    error = true;
                }
            }else {
                address = (Integer.valueOf(tokenizer.nextToken()));
                if (validAddress())
                    objectCode.add((short) ((JUMPE_OP << 11) | address));
                else
                    invalidAddressError();
            }
        }
    };

    Instruction jumpg = new Instruction(){
        public void exec(){
            if(inSecondPass){
                String address = tokenizer.nextToken();
                if(symbolTable.containsKey(address)){
                    sourceCode.add(opcode + " " + symbolTable.get(address));
                }else{
                    error = true;
                }
            }else {
                address = (Integer.valueOf(tokenizer.nextToken()));
                if (validAddress())
                    objectCode.add((short) ((JUMPG_OP << 11) | address));
                else
                    invalidAddressError();
            }
        }
    };

    Instruction call = new Instruction(){
        public void exec(){
            if(inSecondPass){
                String address = tokenizer.nextToken();
                if(symbolTable.containsKey(address)){
                    sourceCode.add(opcode + " " + symbolTable.get(address));
                }else{
                    error = true;
                }
            }else {
                address = (Integer.valueOf(tokenizer.nextToken()));
                if (validAddress())
                    objectCode.add((short) ((CALL_OP << 11) | address));
                else
                    invalidAddressError();
            }
        }
    };

    Instruction ret = new Instruction(){
        public void exec(){
            objectCode.add((short)(RET_OP << 11));
        }
    };

    Instruction read = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (READ_OP << 11) | (rdest << 9) ));
        }
    };

    Instruction write = new Instruction(){
        public void exec(){
            rdest = (Integer.valueOf( tokenizer.nextToken()));
            objectCode.add((short)( (WRITE_OP << 11) | (rdest << 9) ));
        }
    };

    Instruction halt = new Instruction(){
        public void exec(){
            objectCode.add((short)(HALT_OP << 11));
        }
    };

    Instruction noop = new Instruction(){
        public void exec(){
            objectCode.add((short)(NOOP_OP << 11));
        }
    };

    /**********************************************************
     *
     * END OF INSTRUCTIONS
     *
     **********************************************************/



    boolean validAddress(){
        return (address >= 0) && (address <= 255);
    }

    boolean validConstant(){
        return (constant >= -128) && (constant <= 128);
    }

    void invalidAddressError(){
        error = true;
        Message msg = Message.obtain();
        msg.obj = new String("Error in line " + lineNumber + " : Invalid address value.") ;
        //mEvent.handleMessage(msg);
        mHandler.sendMessage(msg);
    }

    void invalidConstantError(){
        error = true;
        Message msg = Message.obtain();
        msg.obj = new String("Error in line " + lineNumber + " : Invalid constant value.") ;
        //mEvent.handleMessage(msg);
        mHandler.sendMessage(msg);
    }

}

package com.example.user.testone;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.widget.Toast;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Assembler extends AsyncTask<Void, Void, Void> {

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
    private int lineNumber;
    private int codeLineNumber;
    private String fileName;
    private String exitMessage;
    private boolean inSecondPass;
    Context mContext;
    private Activity mHost;

    // Constructor
    Assembler(Context context, List<String> code, String name){
        mContext = context;
        mHost = (Activity) context;
        fileName = name;
        origSourceCode = code;
        objectCode  = new ArrayList<>();
        strippedCode = new ArrayList<>();
        sourceCode = new ArrayList<>();
        symbolTable = new HashMap<>();
        instructionSet = new HashMap<>();
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


    protected Void doInBackground(Void... args){
        assemble();
        return null;
    }

    protected void onPostExecute(Void arg){
        if(error){
            // display error message. Runs in main thread. Do nothing else
            if(mHost.getCurrentFocus() != null){
                Snackbar.make(mHost.getCurrentFocus(), exitMessage, Snackbar.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext.getApplicationContext(), exitMessage, Toast.LENGTH_LONG).show();
            }
        } else {
            if(mHost.getCurrentFocus() != null){
                Snackbar.make(mHost.getCurrentFocus(), exitMessage, Snackbar.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext.getApplicationContext(), exitMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void assemble(){
        if(firstPass()){
            if(secondPass()){
                if(thirdPass()){
                    saveObjectCode();
                    exitMessage = "Compilation finished successfully";
                }
            }
        }
    }

    /**
     * First pass: Instruction breakdown and table symbol creation.
     * Instructions have the following format:  LABEL:;INSTRUCTION;#COMMENT
     * where the semicolons are used for easy parsing. This method serves two functions:
     * (I) It strips the comment and label sections from each line and stores the
     * remaining instruction in the strippedCode array list.
     * (II) It builds the symbol table using the stripped labels.
     */
    public boolean firstPass(){
        error = false;
        lineNumber = 0;
        codeLineNumber = 0;
        String tempLabel, tempOpcode;
        for(String t : origSourceCode){
            String [] tempArray = t.split(";");
            tempLabel = tempArray[0];
            tempOpcode = tempArray[1];
            if(!tempLabel.isEmpty()){
                // add label to symbol table
                if(!addLabelToTable(tempLabel)){
                    error = true;
                    exitMessage = "Error: label \"" + tempLabel +  "\" exists. ";
                    break;
                }
                if(tempOpcode.trim().isEmpty()){
                    tempOpcode = "noop";
                }
                codeLineNumber++;
            }else{
                if(!tempOpcode.trim().isEmpty()){
                    codeLineNumber++;
                }
            }
            strippedCode.add(tempOpcode);
            lineNumber++;
        }
        return !error;
    }

    /**
     * Second pass: Symbol resolution
     * @return True if no errors are encountered. False otherwise.
     */
    public boolean secondPass(){
        inSecondPass = true;
        error = false;
        lineNumber = 0;
        for(String line : strippedCode){
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
            if(error){
                break;
            }
            lineNumber++;
        }
        inSecondPass = false;
        return !error;
    }

    /**
     * Third pass (compilation): translates from assembly language
     * to machine language.
     * @return {@code true} if no errors are encountered; {@code false} otherwise.
     */
    public boolean thirdPass(){
        lineNumber = 0;
        error = false;
        for(String line : sourceCode){
            tokenizer = new Tokenizer(line);
            opcode = tokenizer.nextToken();
            if(!opcode.isEmpty()){
                instructionSet.get(opcode).exec();
            }
            if(error){
                break;
            }
            lineNumber++;
        }
        return !error;
    }

    /**
     * Adds a new entry into the symbol table. No duplicates are allowed.
     * @param label  the label to input into the table
     * @return   True if label successfully added to table. False otherwise.
     */
    private boolean addLabelToTable(String label){
        // remove semicolon from label
        label = label.substring(0, label.length() - 1);
        if(!symbolTable.containsKey(label)){
            symbolTable.put(label, codeLineNumber);
            return true;
        }
        return false;
    }

    /**
     * Saves the compiled code to internal storage.
     * Invoked if compilation finishes successfully.
     */
    private void saveObjectCode() {
        String file = fileName.replace(".s", ".o");
        byte[] buf = new byte[2];
        try{
            FileOutputStream fos = mContext.openFileOutput(file, Context.MODE_PRIVATE);
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

    private boolean validAddress(){
        return (address >= 0) && (address <= 255);
    }

    private boolean validConstant(){
        return (constant >= -128) && (constant <= 128);
    }

    private void invalidAddressError(){
        exitMessage = "Error in line: " + lineNumber + " : Invalid address value.";
        error = true;
    }

    private void invalidConstantError(){
        exitMessage = "Error in line: " + lineNumber + " : Invalid constant value.";
        error = true;
    }

    /**********************************************************
     *   INSTRUCTIONS
     **********************************************************/
    Instruction load = new Instruction(){
        public void exec(){
            if(inSecondPass){
                String dest = tokenizer.nextToken(); // hold this temporarily
                String address = tokenizer.nextToken();
                if(symbolTable.containsKey(address)){
                    sourceCode.add(opcode + " " + dest + " " + symbolTable.get(address));
                }else{
                    exitMessage = "Error in line: " + lineNumber + ". Label: \"" + address + "\" not found.";
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
                    exitMessage = "Error in line: " + lineNumber + ". Label: \"" + address + "\" not found.";
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
                    exitMessage = "Error in line: " + lineNumber + ". Label: \"" + address + "\" not found.";
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
                    exitMessage = "Error in line: " + lineNumber + ". Label: \"" + address + "\" not found.";
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
                    exitMessage = "Error in line: " + lineNumber + ". Label: \"" + address + "\" not found.";
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
                    exitMessage = "Error in line: " + lineNumber + ". Label: \"" + address + "\" not found.";
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
                    exitMessage = "Error in line: " + lineNumber + ". Label: \"" + address + "\" not found.";
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

}


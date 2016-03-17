package com.example.user.testone;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;

/**
 * Created by user on 1/13/15.
 */

public class VirtualMachine {

    interface Instruction{
        void exec();
    }

    class InvertedTableEntry{
        public int pid;
        public int pageNumber;
    }

    private final int REG_FILE_SIZE = 4;
    private final int MEMORY_SIZE = 256;
    private final int INSTRUCTION_SET_SIZE = 26;
    private final int SAVED_STATE_SIZE = 6;

    // Commonly used masks
    private final int OPCODE_MASK = 0xF800;		    // 1111 1000 0000 0000
    private final int DEST_REG_MASK = 0x0600;  	    // 0000 0110 0000 0000
    private final int I_BIT_MASK = 0x0100;	 	    // 0000 0001 0000 0000
    private final int SOURCE_REG_MASK = 0x00C0;     // 0000 0000 1100 0000
    private final int CONST_MASK = 0x00ff;		    // 0000 0000 1111 1111
    private final int SIGN_BIT_MASK = 0x80; 	    //	     	 1000 0000
    private final int SIGN_EXTEND_MASK = 0xffff0000;
    private final int LEG_BITS_MASK = 0x000E;       // 0000 0000 0000 1110
    private final int RESULT_CARRY_MASK = 0x10000;  // 0001 0000 0000 0000 0000
    private final int REG_MSB_MASK = 0x8000;        // 1000 0000 0000 0000
    private final int REG_LSB_MASK = 0x1;

    // virtual machine return status codes
    public static final int TIME_SLICE = 0;
    public static final int HALT_INSTRUCTION = 1;
    public static final int OUT_OF_BOUNDS_REF = 2;
    public static final int STACK_OVERFLOW = 3;
    public static final int STACK_UNDERFLOW = 4;
    public static final int INVALID_OPCODE = 5;
    public static final int READ_OPERATION = 6;
    public static final int WRITE_OPERATION = 7;

    private Context mcontext;
    private Instruction[] instructionSet;
    private int opcode;
    private int rs, rd;
    private int constant, address;
    // immediate bit
    private boolean immBit;


    // physical memory
    protected int[] memory;
    // register file
    protected int[] register;
    protected int programCounter = 0;
    protected int instructionReg = 0;
    protected int stackPointer = 256;

    /*
     * These fields and flags make up the status register with the following format:
     * [io_register][return_status][V][L][E][G][C]
     * We break it up into multiple fields in order to reduce bitwise operations
     */
    protected boolean pageFaultFlag;
    protected int ioRegister;
    protected int returnStatus;
    protected boolean overflowFlag;
    protected boolean lessFlag;
    protected boolean equalFlag;
    protected boolean greaterFlag;
    protected boolean carryFlag;


    private int base = 0;
    private int limit = 0;
    protected int clock = 0;
    protected PageTable TLB;
    protected InvertedTableEntry [] invertedTable;
    private boolean stop = false;


    /**
     * Constructor
     */
    VirtualMachine(){
        // allocate register file
        register = new int[REG_FILE_SIZE];
        // allocate ram memory
        memory = new int[MEMORY_SIZE];
        // setup inverted table
        invertedTable = new InvertedTableEntry[32];
        // setup instruction set
        instructionSet = new Instruction[INSTRUCTION_SET_SIZE];
        instructionSet[0] = load;
        instructionSet[1] = store;
        instructionSet[2] = add;
        instructionSet[3] = addc;
        instructionSet[4] = sub;
        instructionSet[5] = subc;
        instructionSet[6] = and;
        instructionSet[7] = xor;
        instructionSet[8] = compl;
        instructionSet[9] = shl;
        instructionSet[10] = shla;
        instructionSet[11] = shr;
        instructionSet[12] = shra;
        instructionSet[13] = compr;
        instructionSet[14] = getstat;
        instructionSet[15] = putstat;
        instructionSet[16] = jump;
        instructionSet[17] = jumpl;
        instructionSet[18] = jumpe;
        instructionSet[19] = jumpg;
        instructionSet[20] = call;
        instructionSet[21] = ret;
        instructionSet[22] = read;
        instructionSet[23] = write;
        instructionSet[24] = halt;
        instructionSet[25] = noop;
    }

    /**
     *
     */
    void runProcess(){
        // The main loop
        while(!stop){
            // fetch instruction from memory
            instructionReg = memory[programCounter++];
            // decode instruction
            decodeInstruction(instructionReg);
            // execute instruction
            instructionSet[opcode].exec();
        }
    }

    void loadObjectFile(ArrayList<Short> obj){
        // Load object file to memory
        for(int i = 0; i < obj.size(); i++)
            memory[i] = obj.get(i);
        limit = obj.size();
    }


    public int [] getRegisterStatus(){
        return register;
    }

    /**
     * Decodes a machine instruction. That is, it extracts
     * the opcode and all possible arguments to complete an
     * instruction.
     * @param instruction
     */
    void decodeInstruction(int instruction){
        opcode = (instruction & OPCODE_MASK) >> 11;
        rd = (instruction & DEST_REG_MASK ) >> 9;
        rs = (instruction & SOURCE_REG_MASK) >> 6;
        address = (instruction & CONST_MASK);
        constant = (instruction & CONST_MASK); //TODO constant needs to be sign extended
        immBit = ((instruction & I_BIT_MASK) > 0);
    }


    /*****************************************************
     *
     *         BEGINNING OF INSTRUCTION SET
     *
     ****************************************************/

    Instruction load = new Instruction(){
        public void exec(){
            if(immBit)
                register[rd] = constant;
            else
                register[rd] = memory[address];
        }
    };

    Instruction store = new Instruction(){
        public void exec(){
            memory[address] = register[rd];
        }
    };

    Instruction add = new Instruction(){
        public void exec(){
			if(immBit)
				register[rd] += constant;
			else
				register[rd] += register[rs];

            carryFlag = ( (register[rd] & RESULT_CARRY_MASK) > 0);
        }

    };

    Instruction addc = new Instruction(){
        public void exec(){
            if(immBit)
                register[rd] = register[rd] + constant + carryBit();
            else
                register[rd] = register[rd] + register[rs] + carryBit();

            carryFlag = ( (register[rd] & RESULT_CARRY_MASK) > 0);
        }
    };

    Instruction sub = new Instruction(){
        public void exec(){
            if(immBit)
                register[rd] -= constant;
            else
                register[rd] -= register[rs];

            carryFlag = ( (register[rd] & RESULT_CARRY_MASK) > 0);
        }
    };

    Instruction subc = new Instruction(){
        public void exec(){
            if(immBit)
                register[rd] = register[rd] - constant - carryBit();
            else
                register[rd] = register[rd] - register[rs] - carryBit();

            carryFlag = ( (register[rd] & RESULT_CARRY_MASK) > 0);
        }
    };

    Instruction and = new Instruction(){
        public void exec(){
            if(immBit)
                register[rd] &= constant;
            else
                register[rd] &= register[rs];
        }
    };

    Instruction xor = new Instruction(){
        public void exec(){
            if(immBit)
                register[rd] ^= constant;
            else
                register[rd] ^= register[rs];
        }
    };

    Instruction compl = new Instruction(){
        public void exec(){
            register[rd] = ~register[rd];
        }
    };

    Instruction shl = new Instruction(){
        public void exec(){
            register[rd] = register[rd] << 1;

            carryFlag = ( (register[rd] & RESULT_CARRY_MASK) > 0);
        }
    };

    Instruction shla  = new Instruction(){
        public void exec(){
            if((register[rd] & REG_LSB_MASK) > 0)
                register[rd] = (register[rd] << 1) | REG_LSB_MASK;
            else
                register[rd] = register[rd] << 1;

            carryFlag = ( (register[rd] & RESULT_CARRY_MASK) > 0);
        }
    };

    Instruction shr = new Instruction(){
        public void exec(){
            register[rd] = register[rd] >> 1;
        }
    };

    Instruction shra = new Instruction(){
        public void exec(){
            if((register[rd] & REG_MSB_MASK) > 0)
                register[rd] = (register[rd] >> 1) | REG_MSB_MASK;
            else
                register[rd] = register[rd] >> 1;
        }
    };

    Instruction compr = new Instruction(){
        public void exec(){
            clearEqualityFlags();
            if(immBit) {
                if (register[rd] > constant)
                    greaterFlag = true;
                else if(register[rd] == constant)
                    equalFlag = true;
                else
                    lessFlag = true;
            }else{
                if (register[rd] > register[rs])
                    greaterFlag = true;
                else if(register[rd] == register[rs])
                    equalFlag = true;
                else
                    lessFlag = true;
            }
        }
    };

    Instruction getstat = new Instruction(){
        public void exec(){
            register[rd] = getStatusReg();
        }
    };

    Instruction putstat = new Instruction(){
        public void exec(){
            setStatusReg(register[rd]);
        }
    };

    Instruction jump = new Instruction(){
        public void exec(){
            if(address > limit) {
                returnStatus = OUT_OF_BOUNDS_REF;
                stop = true;
            }
            programCounter = address;
        }
    };

    Instruction jumpl = new Instruction(){
        public void exec(){
            if(lessFlag)
                programCounter = address;
        }
    };

    Instruction jumpe = new Instruction(){
        public void exec(){
            if(equalFlag)
                programCounter = address;
        }
    };

    Instruction jumpg = new Instruction(){
        public void exec(){
            if(greaterFlag)
                programCounter = address;
        }
    };

    Instruction call = new Instruction(){
        public void exec(){
            if( stackPointer > limit + SAVED_STATE_SIZE ) {
                pushState();
                programCounter = address;
            }
            else{
                returnStatus = STACK_OVERFLOW;
                stop = true;
            }
        }
    };

    Instruction ret = new Instruction(){
        public void exec(){
            if(stackPointer < MEMORY_SIZE){
                popState();
            }else{
                returnStatus = STACK_UNDERFLOW;
                stop = true;
            }

        }
    };

    Instruction read = new Instruction(){
        public void exec(){
            returnStatus = READ_OPERATION;
            ioRegister = rd;
            stop = true;
        }
    };

    Instruction write = new Instruction(){
        public void exec(){
            returnStatus = WRITE_OPERATION;
            ioRegister = rd;
            stop = true;
        }
    };

    Instruction halt = new Instruction(){
        public void exec(){
            returnStatus = HALT_INSTRUCTION;
            stop = true;
        }
    };

    Instruction noop = new Instruction(){
        public void exec(){
        }
    };


    /**
     * Combines the multiple status flags that make up the status register
     * into a single value that resembles an actual register
     * @return the status register
     */
    private int getStatusReg(){
        int reg = 0;
        reg |= (carryFlag)?1:0;
        reg |= ((greaterFlag)?1:0) << 1;
        reg |= ((equalFlag)?1:0) << 2;
        reg |= ((lessFlag)?1:0) << 3;
        reg |= ((overflowFlag)?1:0) << 4;
        reg |= returnStatus << 5;
        reg |= ioRegister << 8;
        return reg;
    }

    /**
     *
     */
    private void setStatusReg(int reg){
        carryFlag    = (reg & 0b00_000_00001) > 0;
        greaterFlag  = (reg & 0b00_000_00010) > 0;
        equalFlag    = (reg & 0b00_000_00100) > 0;
        lessFlag     = (reg & 0b00_000_01000) > 0;
        overflowFlag = (reg & 0b00_000_10000) > 0;
        returnStatus = (reg & 0b00_111_00000) >> 5;
        ioRegister   = (reg & 0b11_000_00000) >> 8;
    }

    /**
     *
     * @return
     */
    int carryBit(){
        return (carryFlag)? 1:0;
    }

    /**
     * Clears the less than, greater than, and equals flags
     */
    void clearEqualityFlags(){
        lessFlag = false;
        equalFlag = false;
        greaterFlag = false;
    }

    /**
     * Pushes the virtual machine state onto the stack. The stack
     * grows upward. The state includes the program counter, all
     * four general purpose registers and the status register.
     */
    void pushState(){
        memory[--stackPointer] = programCounter;
        for(int i = 0; i < REG_FILE_SIZE; i++)
            memory[--stackPointer] = register[i];
        //memory[--stackPointer] = statusReg;
        memory[--stackPointer] = getStatusReg();
    }

    /**
     * Returns the virtual machine to a previously saved
     * state.
     */
    void popState(){
        //statusReg = memory[stackPointer++];
        setStatusReg(memory[stackPointer++]);
        for(int i = 3; i >=  0; i--)
            register[i] = memory[stackPointer++];
        programCounter = memory[stackPointer++];

    }


}


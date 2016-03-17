package com.example.user.testone;

/**
 * Created by user on 11/11/14.
 * All this class does is hold the constant values
 * that are used throughout the application
 */

public final class Const {

    private Const(){
        // no instantiation
    }

    // tags
    static final String STATE_ADAPTER = "adapter_array";
    static final String DIALOG_TYPE = "dialog_tag";
    static final String INSTRUCTION = "instruction_tag";
    static final String FILE_ACTION = "file_action_tag";
    static final String OPCODE = "opcode_tag";
    static final String RETURN_MSG = "return_mesg_tag";


    // dialog types (magic numbers)
    static final int COMMENT_DIALOG = 10;
    static final int SINGLE_ADDRESS_DIALOG = 11;
    static final int SINGLE_REG_DIALOG = 12;
    static final int REG_AND_ADDRESS_DIALOG = 13;
    static final int REG_AND_CONSTANT_DIALOG = 14;
    static final int REG_AND_REG_DIALOG = 15;
    static final int SAVE_SOURCE_DIALOG = 16;
    static final int SAVE_OBJECT_FILE_DIALOG = 17;
    static final int OPEN_SOURCE_DIALOG = 18;
    static final int NOOP_DIALOG = 19;

    // request and result codes
    static final int NEW_INSTRUCTION= 55;
    static final int EDIT_INSTRUCTION = 56;
    static final int GET_ARGUMENTS 	= 57;
    static final int RESULT_OK 		= 58;
    static final int GET_FILE_NAME = 59;
    static final int SAVE_SOURCE_FILE = 60;
    static final int SAVE_MODIFIED_SOURCE_FILE = 61;
    static final int SAVE_BEFORE_OPEN_FILE = 61;
    static final int OPEN_SOURCE_FILE = 62;

    static final short OPCODE_MASK =  (short)0xF800;
}

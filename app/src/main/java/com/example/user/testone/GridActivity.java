package com.example.user.testone;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;


/**
 * Created by AG on 11/11/14.
 * Instructions follow the following pattern:
 * LABEL:; OPCODE ; #COMMENT
 * The semi-colon is used as a delimiter when parsing an instruction.
 */
public class GridActivity extends ActionBarActivity {

    private static final String DEBUG_TAG = "Gestures";


    private static final int COMMENT= 0;
    private static final int LOAD 	= 1;
    private static final int LOADI 	= 2;
    private static final int STORE 	= 3;
    private static final int ADD 	= 4;
    private static final int ADDI 	= 5;
    private static final int ADDC 	= 6;
    private static final int ADDCI 	= 7;
    private static final int SUB	= 8;
    private static final int SUBI	= 9;
    private static final int SUBC 	= 10;
    private static final int SUBCI	= 11;
    private static final int AND	= 12;
    private static final int ANDI	= 13;
    private static final int XOR	= 14;
    private static final int XORI	= 15;
    private static final int COMPL 	= 16;
    private static final int SHL	= 17;
    private static final int SHLA	= 18;
    private static final int SHR	= 19;
    private static final int SHRA	= 20;
    private static final int COMPR	= 21;
    private static final int COMPRI	= 22;
    private static final int GETSTAT= 23;
    private static final int PUTSTAT= 24;
    private static final int JUMP	= 25;
    private static final int JUMPL	= 26;
    private static final int JUMPE	= 27;
    private static final int JUMPG	= 28;
    private static final int CALL	= 29;
    private static final int RET	= 30;
    private static final int READ	= 31;
    private static final int WRITE	= 32;
    private static final int HALT	= 33;
    private static final int NOOP	= 34;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        if(gridview == null){
            Log.d(DEBUG_TAG, "error getting gridview");
            finish();
        }
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                switch(position){
                    case COMMENT:
                        launchArgumentActivity(" ", Const.COMMENT_DIALOG);
                        break;
                    case LOAD 	:
                        launchArgumentActivity("load", Const.REG_AND_ADDRESS_DIALOG);
                        break;
                    case LOADI 	:
                        launchArgumentActivity("loadi", Const.REG_AND_CONSTANT_DIALOG);
                        break;
                    case STORE 	:
                        launchArgumentActivity("store", Const.REG_AND_ADDRESS_DIALOG);
                        break;
                    case ADD 	:
                        launchArgumentActivity("add", Const.REG_AND_REG_DIALOG);
                        break;
                    case ADDI 	:
                        launchArgumentActivity("addi", Const.REG_AND_CONSTANT_DIALOG);
                        break;
                    case ADDC 	:
                        launchArgumentActivity("addc", Const.REG_AND_REG_DIALOG);
                        break;
                    case ADDCI 	:
                        launchArgumentActivity("addci", Const.REG_AND_CONSTANT_DIALOG);
                        break;
                    case SUB	:
                        launchArgumentActivity("sub", Const.REG_AND_REG_DIALOG);
                        break;
                    case SUBI	:
                        launchArgumentActivity("subi", Const.REG_AND_CONSTANT_DIALOG);
                        break;
                    case SUBC 	:
                        launchArgumentActivity("subc", Const.REG_AND_REG_DIALOG);
                        break;
                    case SUBCI	:
                        launchArgumentActivity("subci", Const.REG_AND_CONSTANT_DIALOG);
                        break;
                    case AND		:
                        launchArgumentActivity("and", Const.REG_AND_REG_DIALOG);
                        break;
                    case ANDI	:
                        launchArgumentActivity("andi", Const.REG_AND_CONSTANT_DIALOG);
                        break;
                    case XOR	:
                        launchArgumentActivity("xor", Const.REG_AND_REG_DIALOG);
                        break;
                    case XORI	:
                        launchArgumentActivity("xori", Const.REG_AND_CONSTANT_DIALOG);
                        break;
                    case COMPL 	:
                        launchArgumentActivity("compl", Const.SINGLE_REG_DIALOG);
                        break;
                    case SHL	:
                        launchArgumentActivity("shl", Const.SINGLE_REG_DIALOG);
                        break;
                    case SHLA	:
                        launchArgumentActivity("shla", Const.SINGLE_REG_DIALOG);
                        break;
                    case SHR	:
                        launchArgumentActivity("shr", Const.SINGLE_REG_DIALOG);
                        break;
                    case SHRA	:
                        launchArgumentActivity("shra", Const.SINGLE_REG_DIALOG);
                        break;
                    case COMPR	:
                        launchArgumentActivity("compr", Const.REG_AND_REG_DIALOG);
                        break;
                    case COMPRI	:
                        launchArgumentActivity("compri", Const.REG_AND_CONSTANT_DIALOG);
                        break;
                    case GETSTAT	:
                        launchArgumentActivity("getstat", Const.SINGLE_REG_DIALOG);
                        break;
                    case PUTSTAT	:
                        launchArgumentActivity("putstat", Const.SINGLE_REG_DIALOG);
                        break;
                    case JUMP	:
                        launchArgumentActivity("jump", Const.SINGLE_ADDRESS_DIALOG);
                        break;
                    case JUMPL	:
                        launchArgumentActivity("jumpl", Const.SINGLE_ADDRESS_DIALOG);
                        break;
                    case JUMPE	:
                        launchArgumentActivity("jumpe", Const.SINGLE_ADDRESS_DIALOG);
                        break;
                    case JUMPG	:
                        launchArgumentActivity("jumpg", Const.SINGLE_ADDRESS_DIALOG);
                        break;
                    case CALL	:
                        launchArgumentActivity("call", Const.SINGLE_ADDRESS_DIALOG);
                        break;
                    case READ	:
                        launchArgumentActivity("read", Const.SINGLE_REG_DIALOG);
                        break;
                    case WRITE	:
                        launchArgumentActivity("write", Const.SINGLE_REG_DIALOG);
                        break;
                    case HALT:
                        finishWithResult(";halt; ");
                        break;
                    case RET:
                        finishWithResult(";return; ");
                        break;
                    case NOOP:
                        launchArgumentActivity("noop", Const.NOOP_DIALOG);
                        break;
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // This is an empty menu. There's no need for a menu when this fragment
        // is shown.
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // No menu. Nothing to handle
        return super.onOptionsItemSelected(item);
    }

    public void finishWithResult(String instr){
        Intent result = new Intent();
        result.putExtra(Const.INSTRUCTION, instr);
        setResult(Const.RESULT_OK, result);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(intent != null){
            if(requestCode == Const.GET_ARGUMENTS && resultCode == Const.RESULT_OK){
                //if(intent.hasExtra(Const.INSTRUCTION))
                finishWithResult(intent.getStringExtra(Const.RETURN_MSG));
            }
        }
    }


    public void launchArgumentActivity(String opcode, int dialog_type){
        Intent intent = new Intent(getApplicationContext(), ArgumentActivity.class);
        intent.putExtra(Const.OPCODE, opcode);
        intent.putExtra(Const.DIALOG_TYPE, dialog_type);
        startActivityForResult(intent, Const.GET_ARGUMENTS);
    }

}



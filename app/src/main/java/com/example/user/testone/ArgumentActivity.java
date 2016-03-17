package com.example.user.testone;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 11/11/14.
 */

/*
 * This activity could have been implemented using the
 * Dialog class. As it is, this activity behaves much
 * like a dialog since it uses the Theme.Holo.Dialog theme.
 */

public class ArgumentActivity extends Activity {

    EditText const_addr;
    EditText comment;
    EditText label;
    String opcode;
    String reg0, reg1;
    Spinner spinner0, spinner1;
    ArrayAdapter<CharSequence> adapter0;
    ArrayAdapter<CharSequence> adapter1;
    TextView text;
    int dialog_type;

    private final String WRONG_FILE_NAME = "Invalid name. Don't forget "
            + "the \".s\" extension.\nOnly characters," +
            " numbers and underscores are allowed";

    /**
     * OnItemSelected listeners for register spinners.
     */
    private AdapterView.OnItemSelectedListener listener0 = new AdapterView.OnItemSelectedListener(){

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
            reg0 = new String(Integer.toString(pos));
        }

        public void onNothingSelected(AdapterView<?> parent){} // nothing to do here
    };

    /**
     * Second OnItemSelected listener. Not always used.
     */
    private AdapterView.OnItemSelectedListener listener1 = new AdapterView.OnItemSelectedListener(){

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
            reg1 = new String(Integer.toString(pos));
        }

        public void onNothingSelected(AdapterView<?> parent){} // nothing to do here
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent prev = getIntent();

        // The intent will not always have an opcode, so this is necessary
        if(prev.hasExtra(Const.OPCODE))
            opcode = new String(prev.getStringExtra(Const.OPCODE));

        // Let's determine which layout to use for the dialog box.
        // The intent should always have a dialog type so a check might no be necessary
        if(prev.hasExtra(Const.DIALOG_TYPE))
            dialog_type = prev.getIntExtra(Const.DIALOG_TYPE, Const.COMMENT_DIALOG);

        switch(dialog_type){
            /*
            case Const.OPEN_SOURCE_DIALOG:
                setContentView(R.layout.open_textfile_dialog);
                setTitle("Enter file name");
                break;
                */
            case Const.SAVE_OBJECT_FILE_DIALOG:
                setContentView(R.layout.save_dialog);
                break;
            case Const.SAVE_SOURCE_DIALOG:
                setContentView(R.layout.save_dialog);
                setTitle("Enter file name");
                break;
            case Const.COMMENT_DIALOG:
                setContentView(R.layout.dialog_comment);
                break;
            case Const.NOOP_DIALOG:
                setContentView(R.layout.dialog_noop);
                label = (EditText) findViewById(R.id.label_edit_text);
                break;
            case Const.SINGLE_ADDRESS_DIALOG:
                setContentView(R.layout.dialog_single_address);
                text = (TextView) findViewById(R.id.opcode_text);
                text.setText(opcode);
                label = (EditText) findViewById(R.id.label_edit_text);
                const_addr = (EditText)findViewById(R.id.address_edit_text);
                break;
            case Const.SINGLE_REG_DIALOG:
                setContentView(R.layout.dialog_single_register);
                text = (TextView) findViewById(R.id.opcode_text);
                text.setText(opcode);
                label = (EditText) findViewById(R.id.label_edit_text);

                spinner0 = (Spinner) findViewById(R.id.first_reg_spinner);
                adapter0 = ArrayAdapter.createFromResource(this,
                        R.array.registers_array,R.layout.my_spinner_item);
                adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner0.setAdapter(adapter0);
                spinner0.setOnItemSelectedListener(listener0);
                break;
            case Const.REG_AND_ADDRESS_DIALOG:
                setContentView(R.layout.dialog_register_address);
                text = (TextView) findViewById(R.id.opcode_text);
                text.setText(opcode);
                label = (EditText) findViewById(R.id.label_edit_text);
                const_addr = (EditText)findViewById(R.id.address_edit_text);

                spinner0 = (Spinner) findViewById(R.id.first_reg_spinner);
                adapter0 = ArrayAdapter.createFromResource(this,
                        R.array.registers_array,R.layout.my_spinner_item);
                adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner0.setAdapter(adapter0);
                spinner0.setOnItemSelectedListener(listener0);
                break;
            case Const.REG_AND_CONSTANT_DIALOG:
                setContentView(R.layout.dialog_register_constant);
                text = (TextView) findViewById(R.id.opcode_text);
                text.setText(opcode);
                label = (EditText) findViewById(R.id.label_edit_text);
                const_addr = (EditText)findViewById(R.id.const_edit_text);

                spinner0 = (Spinner) findViewById(R.id.first_reg_spinner);
                adapter0 = ArrayAdapter.createFromResource(this,
                        R.array.registers_array,R.layout.my_spinner_item);
                adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner0.setAdapter(adapter0);
                spinner0.setOnItemSelectedListener(listener0);
                break;
            case Const.REG_AND_REG_DIALOG:
                setContentView(R.layout.dialog_two_registers);
                text = (TextView) findViewById(R.id.opcode_text);
                text.setText(opcode);
                label = (EditText) findViewById(R.id.label_edit_text);

                spinner0 = (Spinner) findViewById(R.id.first_reg_spinner);
                adapter0 = ArrayAdapter.createFromResource(this,
                        R.array.registers_array,R.layout.my_spinner_item);
                adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner0.setAdapter(adapter0);
                spinner0.setOnItemSelectedListener(listener0);

                spinner1 = (Spinner) findViewById(R.id.second_reg_spinner);
                adapter1 = ArrayAdapter.createFromResource(this,
                        R.array.registers_array,R.layout.my_spinner_item);
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner1.setAdapter(adapter1);
                spinner1.setOnItemSelectedListener(listener1);

        }

        comment = (EditText) findViewById(R.id.comment_edittext);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // There's no need for a menu in this activity since it is displayed
        // as a dialog anyway.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }


    /**
     * Returns a fully constructed instruction to the parent activity (GridActivity).
     * It's easier than having this inside finish() since finish() is called
     * by other events as well.
     * @param instr
     */
    public void finishWithResult(String instr){
        Intent result = new Intent();
        result.putExtra(Const.RETURN_MSG, instr);
        setResult(Const.RESULT_OK, result); // Class Activity has its own RESULT_OK
        finish();
    }


    /**
     * Called whenever the OK or Cancel button in the dialog is pressed.
     * Builds a fully built instruction and calls finisWithResult();
     * @param v
     */
    public void onClickEvent(View v){

        switch(v.getId()){
            case R.id.ok_button:
                if( (dialog_type == Const.OPEN_SOURCE_DIALOG) ||
                        (dialog_type == Const.SAVE_SOURCE_DIALOG) ){
                    checkFileName();
                }else
                    buildInstruction();
                break;

            case R.id.cancel_button:
                finish();
        }
    }

    private void buildInstruction(){
        String instr = ";" + opcode;
        if(label != null)
            if(!label.getText().toString().isEmpty()){
                instr = label.getText().toString() + ":" + instr;
            }

        if(reg0 != null)
            instr = instr + "  " + reg0;
        if(reg1 != null)
            instr = instr + "  " + reg1;
        if(const_addr != null)
            if(const_addr.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "You didn't enter a constant/address",
                        Toast.LENGTH_SHORT).show();
                return;
            }else{
                instr = instr + "  " + const_addr.getText().toString();
            }

        instr += "; ";
        if(!comment.getText().toString().isEmpty()) {
            instr = instr + "#" + comment.getText().toString();
        }

        finishWithResult(instr);
    }

    private void checkFileName(){
        if( !comment.getText().toString().isEmpty() ){
            if(isProperFileName(comment.getText().toString())){
                finishWithResult(comment.getText().toString());
            }else
                Toast.makeText(getApplicationContext(), WRONG_FILE_NAME, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Verifies that a file name conforms to the following rules:
     * It contains a ".s" extension
     * It only contains characters (upper or lowercase), numbers or underscores.
     * No spaces
     * @param fileName    The name of the file name to be verified
     * @return    (boolean) Whether or not the files conforms to the rules
     */
    private boolean isProperFileName(String fileName){
        // Let's be a little forgiving and allow space around the
        // file name. Let's trim it though.
        String temp = new String(fileName.trim());
        Pattern p = Pattern.compile("[^a-zA-Z0-9_\\.]");
        Matcher m = p.matcher(temp);
        // Look for anything that's not a-z, A-Z, 0-9,"_" or "." as
        // specified in the Pattern definition above
        if(m.find())
            return false;
        String[] result = temp.split("\\.", 2);
        if( !(result[0].isEmpty()) && (result.length > 1) )
            return result[1].matches("s");
        return false;
    }
}


package com.example.user.testone;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by user on 11/11/14.
 */
public class ThirdFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String FRAGMENT_BAR_TITLE = "Execution Statistics";
    private static final String RESULT_FRAGMENT_TAG = "Result_fragment";



    PageFragmentListener mListener;
    CardView mCardView;

    TextView reg0;
    TextView reg1;
    TextView reg2;
    TextView reg3;
    TextView programName;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ThirdFragment newInstance(int sectionNumber) {

        ThirdFragment fragment = new ThirdFragment();
        fragment.setRetainInstance(true);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ThirdFragment() {
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        // No menu so far.
//        inflater.inflate(R.menu.browser_menu, menu);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_third, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        programName = (TextView) view.findViewById(R.id.program_title);
        reg0 = (TextView)view.findViewById(R.id.register0_result);
        reg1 = (TextView)view.findViewById(R.id.register1_result);
        reg2 = (TextView)view.findViewById(R.id.register2_result);
        reg3 = (TextView)view.findViewById(R.id.register3_result);

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try{
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PageFragmentListener) activity;
        }catch(ClassCastException e){
            // The activity doesn't implement the callback interface, throw exception
            throw new ClassCastException(activity.toString() +
                    " must implement EditorNoticeListener");
        }

    }

    /**
     * This function displays the register values of the virtual machine
     * after execution has finished. Invoked by the main activity
     */
    public void showResults(String [] results, String programName){

        this.programName.setText(programName);
        this.programName.setTextColor(Color.RED);
        reg0.setText( reg0.getText().toString() + " " + results[0]);
        reg1.setText( reg1.getText().toString() + " " + results[1]);
        reg2.setText( reg2.getText().toString() + " " + results[2]);
        reg3.setText( reg3.getText().toString() + " " + results[3]);

    }
}

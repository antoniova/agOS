package com.example.user.testone;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by theone on 4/20/15.
 */
public class ResultFragment extends ListFragment {


    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String FRAGMENT_BAR_TITLE = "Execution Statistics";
    private static final String RESULT_FRAGMENT_TAG = "Result_fragment";


    PageFragmentListener mListener;
    ResultFragmentAdapter mAdapter;
    ArrayList<String> mList;


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ResultFragment newInstance(int sectionNumber) {
        ResultFragment fragment = new ResultFragment();
        fragment.setRetainInstance(true);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ResultFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mList = new ArrayList<>();
        /*
        mList.add("first;1;2;3;4");
        mList.add("second;2;3;5;1");
        mList.add("third;4;5;3;8");
        mList.add("fourth;3;5;2;3");
        */

        if(savedInstanceState != null)
            mList = savedInstanceState.getStringArrayList(Const.STATE_ADAPTER);


        mAdapter = new ResultFragmentAdapter(getActivity(), R.layout.cardview_layout, mList);

        //if(savedInstanceState != null)
          //  mAdapter.mSelections = (HashMap<Integer,Boolean>) savedInstanceState.getSerializable("HASH_MAP");

        setHasOptionsMenu(true);

        setListAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        // No menu so far.
//        inflater.inflate(R.menu.browser_menu, menu);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.result_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

    }

    /*
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
    */

    /**
     * This function displays the register values of the virtual machine
     * after execution has finished. Invoked by the main activity
     */
    public void showResults(String [] results, String programName){

        String l = programName + ";" + results[0] + ";" + results[1] + ";" + results[2] +";" + results[3];
        mAdapter.add(l);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putStringArrayList(Const.STATE_ADAPTER, mList);
        super.onSaveInstanceState(outState);
    }
}

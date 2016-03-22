package com.example.user.testone;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 11/11/14.
 */
public class FileBrowserFragment extends ListFragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String FRAGMENT_BAR_TITLE = "File Browser";
    private static final String FRAGMENT_TAG = "FILE_BROWSER";

    OnFileActionListener mListener;
    FileBrowserCustomAdapter mAdapter;
    ArrayList<String> fileList;
    int selectionCount = 0;

    FileBrowserCustomAdapter.ViewHolder mHolder;



    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FileBrowserFragment newInstance(int sectionNumber) {
        FileBrowserFragment fragment = new FileBrowserFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        fileList = new ArrayList<>();
        if(savedInstanceState != null)
            fileList = savedInstanceState.getStringArrayList(Const.STATE_ADAPTER);


        mAdapter = new FileBrowserCustomAdapter(getActivity(), R.layout.browser_list_item, fileList);
        if(savedInstanceState != null)
            mAdapter.mSelections = (HashMap<Integer,Boolean>) savedInstanceState.getSerializable("HASH_MAP");

        setHasOptionsMenu(true);

        setListAdapter(mAdapter);
    }

    /**
     * Empty public constructor
     */
    public FileBrowserFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return true;
    }

    /**
     * Used to reload the file list.
     */
    public void reloadFileList(){
        String [] files = getActivity().getFilesDir().list();
        mAdapter.clear();
        for(String file :  files)
            mAdapter.add(file);
    }

    void deleteSelectedItems(Set<Integer> set){
        Integer [] selected = new Integer[set.size()];
        set.toArray(selected);
        for(Integer i : selected){
            File fileToDelete = getActivity().getFileStreamPath(fileList.get(i).toString());
            if(!fileToDelete.delete())
                Log.d(FRAGMENT_TAG, "File: " + i.toString() + " not deleted.");
        }
        reloadFileList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putStringArrayList(Const.STATE_ADAPTER,fileList);
        outState.putSerializable("HASH_MAP", mAdapter.mSelections);
        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //View rootView = inflater.inflate(R.layout.file_browser_layout, container, false);
        return inflater.inflate(R.layout.file_browser_layout, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(mModeListener);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        // Verify that the host activity (MainActivity) implements the callback interface
        try{
            // Instantiate the PageFragmentListener so we can send events to the host
            mListener = (OnFileActionListener) activity;
        }catch(ClassCastException e){
            // The activity doesn't implement the callback interface, throw exception
            throw new ClassCastException(activity.toString() +
                    " must implement EditorNoticeListener");
        }

    }


    /**
     * Called when one of the files listed is clicked. Calls the editGivenFile callback
     * implemented in MainActivity or the executeGiveFile callback
     * for single-mode object file execution
     */
    @Override
    public void onListItemClick(ListView l, View v , int position, long id){
        // Let's determine whether the file to open is a source file
        // or an object file and invoke the appropriate callback method
        TextView txt = (TextView) v.findViewById(R.id.file_item_text);
        String file = txt.getText().toString();
        if(file.contains(".s"))
            mListener.editGivenFile(file);
        else
            mListener.executeGivenFile(file);
    }


    /**
     * The MultiChoiceModeListener implementation
     */
    AbsListView.MultiChoiceModeListener mModeListener = new AbsListView.MultiChoiceModeListener() {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            if(checked) {
                mAdapter.setNewSelection(position, true);
            }else{
                mAdapter.removeSelection(position);
            }
            mode.invalidate();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            menu.clear();
            if(mAdapter.selectionExecutable())
                mode.getMenuInflater().inflate(R.menu.context_execute_menu, menu);
            else
                mode.getMenuInflater().inflate(R.menu.context_many_selections, menu);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.action_delete_file:
                    deleteSelectedItems(mAdapter.getSelections());
                    mode.finish();
                    return true;
                case R.id.action_execute_selected:
                    List<String> list = new ArrayList<>();
                    for(int i : mAdapter.getSelections())
                        list.add(fileList.get(i));

                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB

            // We need to know the type of the selected file in order to inflate
            // the appropriate menu. Let's wait 'till onItemCheckedStateChanged() is invoked
            return true;
        }

        /**
         * Here you can make any necessary updates to the activity when
         * the CAB is removed. By default, selected items are deselected/unchecked.
         * @param mode
         */
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelection();
        }
    };

}

package com.example.user.testone;

//import android.app.ListFragment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;


/**
 * Created by user on 11/10/14.
 */
public class EditorFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_0";
    public static final String FRAGMENT_BAR_TITLE = "Text editor";
    public static final String STATE_ADAPTER =  "adapter_array";

    PageFragmentListener mListener;
    OnFileActionListener assemblerListener;
    ArrayList<String> textbuffer;
    CustomEditorAdapter mAdapter;
    Assembler mAssembler;
    Handler mHandler;
    ActionMode mActionMode;

    /**
     * The adapter used with the RecyclerView
     */
    CustomAdapter mRecyclerAdapter;

    RecyclerView mRecyclerViewList;

    private int positionToEdit = 0;

    /**
     * The following fields need to be saved during configuration changes
     * (i.e., screen rotations)
     */
    boolean sourceModified = false;
    boolean hasBeenSaved = false;
    String currentFileName = "untitled";
    int selectionCount = 0;
    boolean multipleSelected = false;


    /**
     * Return an instance of this fragment
     * @param sectionNumber
     * @return
     */
    public static EditorFragment newInstance(int sectionNumber){
        EditorFragment fragment = new EditorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Empty constructor
     */
    public EditorFragment(){
    }


    @Override
    public void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);

        textbuffer = new ArrayList<>();
        if(SavedInstanceState != null) {
            textbuffer = SavedInstanceState.getStringArrayList(STATE_ADAPTER);
        }

        // todo: remove
        mAdapter = new CustomEditorAdapter(getActivity(), R.layout.editor_list_item, textbuffer);
        mRecyclerAdapter = new CustomAdapter(getActivity(), textbuffer);

        if(SavedInstanceState != null) {
            sourceModified = SavedInstanceState.getBoolean("MODIFIED_SOURCE");
            hasBeenSaved = SavedInstanceState.getBoolean("FILE_HAS_BEEN_SAVED");
            currentFileName = SavedInstanceState.getString("FILE_NAME");
            selectionCount = SavedInstanceState.getInt("SELECTION_COUNT");
            multipleSelected = SavedInstanceState.getBoolean("MULTIPLE_SELECTED");
            mRecyclerAdapter.mSelectedItems = (HashSet<Integer>) SavedInstanceState.getSerializable("HASH_MAP");
        }
        // So this fragment can add its own menu entries
        setHasOptionsMenu(true);

        // I can't remember why this is here
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg){
                String txt = (String) msg.obj;
                Toast.makeText(getActivity(), txt, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        // todo: change to alternative layout
        //return inflater.inflate(R.layout.editor_layout, container, false);
         return inflater.inflate(R.layout.editor_layout_alt, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.editor_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGridActivity(Const.NEW_INSTRUCTION);
            }
        });

        /* get a handle on the recycler view list and set it up */
        mRecyclerViewList = (RecyclerView) view.findViewById(R.id.editor_recycler_view);
        mRecyclerViewList.setLayoutManager(new LinearLayoutManager(mRecyclerViewList.getContext()));
        /* Initialize recyclerview adapter */
        //mRecyclerAdapter = new CustomAdapter(getActivity(), textbuffer);
        mRecyclerViewList.setAdapter(mRecyclerAdapter);

        mListener.changeActionBarTitle(currentFileName, false);
        // Were we in Action Mode?
        if(savedInstanceState != null){
           if(savedInstanceState.getBoolean("ACTION_MODE_STATE")){
               mActionMode = getActivity().startActionMode(mActionModeCallback);
           }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        // Changes the menu
        inflater.inflate(R.menu.editor_menu, menu);
    }

    /**
     * Invoked by the main activity to determine what to use for the action bar
     * title when the editor fragment is in the foreground
     * @return The title to use on the action bar
     */
    public void getCorrectTitle(){
        mListener.changeActionBarTitle(currentFileName, sourceModified);
    }


    /**
     * Handles the menu and actionbar actions. Technically, this
     * gets called only after the main activity's own onOptionsItemSelected
     * is called. All the actions not handled in the main activity are handled here
     * @param item    The action selected in the menu or action bar
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_assemble:
                //assembleFile();
                assemblerListener.assembleFile(textbuffer, currentFileName);
                break;
            case R.id.action_save_text_file:
                saveTextFileAction();
                break;
            case R.id.action_save_text_file_as:
                showFileActionDialog(Const.SAVE_SOURCE_DIALOG, Const.SAVE_SOURCE_FILE);
                break;
            case R.id.action_test:
                doFileTest();

        }

        return true;
    }

    public void doFileTest(){
        File mfile = getActivity().getFileStreamPath("det.o");
        try{
            RandomAccessFile file;
            file = new RandomAccessFile(mfile, "r");
            //String l= file.readLine();
            //Short i = file.readShort();
            Short i = file.readShort();
            //Toast.makeText(getActivity(), size.toString(), Toast.LENGTH_SHORT).show();
            Log.d("MYTAG" , i.toString());
            i= file.readShort();
            Log.d("MYTAG" , i.toString());
            file.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(STATE_ADAPTER, textbuffer);
        outState.putBoolean("MODIFIED_SOURCE",sourceModified);
        outState.putBoolean("FILE_HAS_BEEN_SAVED", hasBeenSaved);
        outState.putString("FILE_NAME", currentFileName);
        outState.putInt("SELECTION_COUNT", selectionCount);
        outState.putBoolean("MULTIPLE_SELECTED", multipleSelected);
        outState.putSerializable("HASH_MAP", mRecyclerAdapter.mSelectedItems);
        outState.putBoolean("ACTION_MODE_STATE", (mActionMode != null));
        super.onSaveInstanceState(outState);
    }


    /**
     * Launches the GridActivity. Any results sent back from
     * GridActivity are handled in onActivityResult();
     */
    void launchGridActivity(int requestCode){
        Intent intent = new Intent(getActivity(),GridActivity.class);
        startActivityForResult(intent, requestCode);
    }

    /**
     * Launches the ArgumentActivity to obtain from the user the name
     * the current source file should be saved as
     * @param dialog_type
     * @param requestCode
     */
    public void showFileActionDialog(int dialog_type, int requestCode){
        Intent intent = new Intent(getActivity(), ArgumentActivity.class);
        intent.putExtra(Const.DIALOG_TYPE, dialog_type);
        startActivityForResult(intent, requestCode);
    }

    /**
     * Receives the assembly instruction string from GridActivity
     * and inserts it into the text file buffer.
     * It also handles file names obtained from a save-file dialog
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(intent != null && resultCode == Const.RESULT_OK){
            switch (requestCode){
                case Const.NEW_INSTRUCTION:
                    mRecyclerAdapter.addItem(intent.getStringExtra(Const.INSTRUCTION));
                    break;
                case Const.EDIT_INSTRUCTION:
                    // todo: update with new adapter
                    textbuffer.set(positionToEdit, intent.getStringExtra(Const.INSTRUCTION));
                    mAdapter.notifyDataSetChanged();
                    break;
                case Const.SAVE_SOURCE_FILE:
                    writeToDisk(intent.getStringExtra(Const.RETURN_MSG));
                    return;
            }
            mListener.changeActionBarTitle(currentFileName, sourceModified = true);
        }
    }

    /**
     * //TODO : add a verification dialog
     * @param file
     */
    public void openNewFile(final String file){
        loadFromDisk(file);
        currentFileName = file;
        sourceModified = false;
        hasBeenSaved = true;
    }

    void saveTextFileAction(){
        if(hasBeenSaved) {
            writeToDisk(currentFileName);
            //sourceModified = false;
            //mListener.changeActionBarTitle(currentFileName, sourceModified);
        }else{
            showFileActionDialog(Const.SAVE_SOURCE_DIALOG, Const.SAVE_SOURCE_FILE);
        }
    }

    /**
     * Writes the contents of the editor text buffer to disk using the name
     * given in filename parameter.
     * @param filename  The name of the file to save
     */
    void writeToDisk(final String filename){
        // Disk I/O is a good candidate for concurrency
        new WriteFileTask().execute(filename);
    }

    /**
     * Opens the specified file for editing. If the file fails
     * to open, the contents of the editor remain unchanged.
     * @param filename   The name of the file to open
     */
    void loadFromDisk(String filename){
        List<String> tempBuffer = new ArrayList<>();
        try {
            FileInputStream fis = getActivity().openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null){
                tempBuffer.add(line);
            }
            reader.close();
            fis.close();
        } catch (IOException e) {
            // Load failed. Let user know and don't make any changes to editor
            Toast.makeText(getActivity(), "Unable to open file", Toast.LENGTH_SHORT).show();
        }
        // Successful load, copy to editor's text buffer
        mRecyclerAdapter.clear();
        for(String string : tempBuffer){
            mRecyclerAdapter.addItem(string);
        }
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try{
            // Instantiate the PageFragmentListener so we can send events to the host
            mListener = (PageFragmentListener) activity;
            assemblerListener = (OnFileActionListener) activity;
        }catch(ClassCastException e){
            // The activity doesn't implement the callback interface, throw exception
            throw new ClassCastException(activity.toString() +
                    " is missing an interface implementation. Check for this.");
        }

    }

    /**
     * Called when the user clicks on a list item. Used to edit an instruction.
     * @param l the ListView object
     * @param v the actual View item clicked
     * @param position the position in the ListView of the View item
     * @param id item id
     */
    /*
    @Override
    public void onListItemClick(ListView l, View v , int position, long id){
        positionToEdit = position;
        launchGridActivity(Const.EDIT_INSTRUCTION);
    }
    */

    /**
     * Deletes the selected items in the text buffer. Care must be taken
     * that the items are deleted in descending order since textbuffer will be
     * resized after every remove() operation. Index values greater than the index previously
     * removed will point to invalid locations.
     * @param set
     */
    void deleteSelectedItems(Set<Integer> set){
        Integer [] t = new Integer[set.size()];
        set.toArray(t);
        Arrays.sort(t, Collections.reverseOrder());
        for(int i : t){
            textbuffer.remove(i);
        }
        mAdapter.notifyDataSetChanged();
        sourceModified = true;
        mListener.changeActionBarTitle(currentFileName, sourceModified);
    }


    /**
     * The MultiChoiceModeListener implementation
     */
    /*
    AbsListView.MultiChoiceModeListener mModeListener = new AbsListView.MultiChoiceModeListener() {



        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            if(checked) {
                mAdapter.setNewSelection(position, true);
                selectionCount++;
            }else{
                mAdapter.removeSelection(position);
                selectionCount--;
            }
            mode.invalidate();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.action_delete_file:
                    deleteSelectedItems(mAdapter.getSelections());
                    mode.finish();
                    return true;
                case R.id.action_insert_line:
                    // There should only be a single item in the Set returned by
                    // mAdapter.getSelections(), so iterating over a single-valued Set might not be optimal
                    for(int i : mAdapter.getSelections())
                        textbuffer.add(i, " ; ; ");
                    mAdapter.notifyDataSetChanged();
                    sourceModified = true;
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_one_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // This might not be the most efficient way to update the CAB
            // but is fast enough
            menu.clear();
            if(selectionCount > 1)
                mode.getMenuInflater().inflate(R.menu.context_many_selections, menu);
            else
                mode.getMenuInflater().inflate(R.menu.context_one_selection, menu);

            return true;
        }

        /**
         * Here you can make any necessary updates to the activity when
         * the CAB is removed. By default, selected items are deselected/unchecked.
         * @param mode

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelection();
            selectionCount = 0;
        }
    };
    */

    /**
     * The new adapter to be used with the recycler view
     */
    private class CustomAdapter extends
            RecyclerView.Adapter<CustomAdapter.ViewHolder>{
        /**
         * The textual (source code) data in the text editor
         */
        private List<String> mTextData;

        /**
         * Keeps track of the selected list view items when in action mode
         */
        private HashSet<Integer> mSelectedItems = new HashSet<>();


        public CustomAdapter(Context context, List<String> items){
            mTextData = items;
        }


        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener{

            private final View mRootView;
            private final TextView mLabel;
            private final TextView mInstruction;
            private final TextView mComment;

            public ViewHolder(View view){
                super(view);
                mRootView = view;
                mRootView.setOnClickListener(this);
                mRootView.setOnLongClickListener(this);
                mLabel = (TextView) view.findViewById(R.id.label_text);
                mInstruction = (TextView) view.findViewById(R.id.instruction_text);
                mComment = (TextView) view.findViewById(R.id.comment_text);
            }

            /**
             * Invoked when an item list view is clicked
             * @param view the view that was clicked
             */
            @Override
            public void onClick(View view){

            }

            /**
             * Invoked when an item list view is long-clicked
              * @param view  the view that was long-clicked
             * @return  return true if action was taken. Always true.
             */
            @Override
            public boolean onLongClick(View view){
                if (mActionMode != null) {
                    // In Action Mode. Two possible scenarios here:
                    // either another item view in the list has been selected,
                    // or the same item view has been selected again.
                    if (!mSelectedItems.add(getAdapterPosition())) {
                        mSelectedItems.remove(getAdapterPosition());
                    }
                    view.setSelected(mSelectedItems.contains(getAdapterPosition()));
                    mActionMode.invalidate();
                    if (mSelectedItems.isEmpty()){
                        mActionMode.finish();
                    }
                } else {
                    // Not in Action Mode. Enter Action Mode and add selection
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                    view.setSelected(mSelectedItems.add(getAdapterPosition()));
                }
                return true;
            }
        } // end of ViewHolder class

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.editor_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position){
            String[] tempArray = mTextData.get(position).split(";");
            try{
                holder.mLabel.setText(tempArray[0]);
                holder.mInstruction.setText(tempArray[1]);
                holder.mComment.setText(tempArray[2]);
            }catch (ArrayIndexOutOfBoundsException e){
                Log.d("EDITOR_FRAGMENT", "array problem");
            }
            holder.mRootView.setSelected(mSelectedItems.contains(position));
        }

        @Override
        public int getItemCount(){
            return mTextData.size();
        }

        public void addItem(String string){
            mTextData.add(string);
            notifyItemInserted(mTextData.size() - 1);

        }

        public void removeItem(int position) {
            mTextData.remove(position);
            notifyItemRemoved(position);
        }

        public void setItem(String string, int position) {
            mTextData.set(position, string);
            notifyItemChanged(position);
        }

        /**
         * Clears the textual data buffer and notifies that a full re-binding is needed
         */
        public void clear(){
            mTextData.clear();
            notifyDataSetChanged();
        }
        /**
         * Invoked when exiting Action Mode using the back button. Clears
         * all selections and forces a full re-binding.
         */
        public void clearSelections(){
            mSelectedItems.clear();
            // Full re-binding. Not most the efficient.
            notifyDataSetChanged();
        }

        public boolean multipleItemsSelected(){
            return (mSelectedItems.size() > 1);
        }

        public boolean hasSelections(){
            return !mSelectedItems.isEmpty();
        }
    } // end of CustomAda

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_one_selection, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        // Return false if nothing is done
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            if(mRecyclerAdapter.multipleItemsSelected()){
                mode.getMenuInflater().inflate(R.menu.context_many_selections, menu);
            }else{
                mode.getMenuInflater().inflate(R.menu.context_one_selection, menu);
            }
            return true;
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            /*
            switch (item.getItemId()) {
                case R.id.menu_share:
                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
            */
            return true;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            if (mRecyclerAdapter.hasSelections()){
                mRecyclerAdapter.clearSelections();
            }
        }
    }; // end of ActionMode.Callback mActionModeCallback

    /**
     * Used to write the contents of the text buffer to disk during a
     * "save file" operation
     */
    private class WriteFileTask extends AsyncTask<String, Void, Boolean>{

        private String fileName;

        protected Boolean doInBackground(String... arg){
            boolean success = true;
            fileName = arg[0];
            try{
                FileOutputStream fos = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
                for(String line : textbuffer){
                    fos.write( (line + "\n").getBytes() );
                }
                fos.close();
            }catch(IOException e){
                success = false;
            }
            return success;
        }

        protected void onPostExecute(Boolean result){
            if(result){
                Toast.makeText(getActivity(), "Saved file: " + fileName, Toast.LENGTH_SHORT)
                        .show();
                hasBeenSaved = true;
                mListener.changeActionBarTitle(currentFileName = fileName,sourceModified = false);
            } else {
                Toast.makeText(getActivity(), "Unable to save file:" + fileName, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

}

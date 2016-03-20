package com.example.user.testone;

//import android.app.ListFragment;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    FloatingActionButton mFab;
    ArrayList<String> textbuffer;
    CustomEditorAdapter mAdapter;
    Assembler mAssembler;
    ArrayList<Short> objectCode;
    Handler mHandler;
    ActionMode mActionMode;

    /* the brand spaking new adapter to be used with RecyclerView */
    CustomRecyclerViewAdapter mRecyclerAdapter;

    RecyclerView mRecyclerViewList;

    int positionToEdit = 0;

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
        mRecyclerAdapter = new CustomRecyclerViewAdapter(getActivity(), textbuffer);

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

        mFab = (FloatingActionButton) view.findViewById(R.id.editor_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGridActivity(Const.NEW_INSTRUCTION);
            }
        });

        /* get a handle on the recycler view list and set it up */
        mRecyclerViewList = (RecyclerView) view.findViewById(R.id.editor_recycler_view);
        mRecyclerViewList.setLayoutManager(new LinearLayoutManager(mRecyclerViewList.getContext()));
        /* Initialize recyclerview adapter */
        //mRecyclerAdapter = new CustomRecyclerViewAdapter(getActivity(), textbuffer);
        mRecyclerViewList.setAdapter(mRecyclerAdapter);

        // Were we in Action Mode?
        if(savedInstanceState != null){
           if(savedInstanceState.getBoolean("ACTION_MODE_STATE")){
               mActionMode = getActivity().startActionMode(mActionModeCallback);
               return;
           }
        }
        mListener.changeActionBarTitle(currentFileName, false);
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
        outState.putBoolean("ACTION_MODE_STATE", (mActionMode != null) );
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
                    // todo : remove
                    //mAdapter.add(intent.getStringExtra(Const.INSTRUCTION));

                    mRecyclerAdapter.addItem(intent.getStringExtra(Const.INSTRUCTION));
                    sourceModified = true;
                    break;
                case Const.EDIT_INSTRUCTION:
                    textbuffer.set(positionToEdit, intent.getStringExtra(Const.INSTRUCTION));
                    mAdapter.notifyDataSetChanged();
                    sourceModified = true;
                    break;
                case Const.SAVE_SOURCE_FILE:
                    writeTextToDisk(intent.getStringExtra(Const.RETURN_MSG));
                    sourceModified = false;
                    currentFileName = intent.getStringExtra(Const.RETURN_MSG);
                    hasBeenSaved = true;
            }
            mListener.changeActionBarTitle(currentFileName, sourceModified);
        }
    }

    /**
     * //TODO : add a verification dialog
     * @param file
     */
    public void openNewFile(final String file){
        loadTextFile(file);
        currentFileName = file;
        sourceModified = false;
        hasBeenSaved = true;
    }

    void saveTextFileAction(){
        if(hasBeenSaved) {
            writeTextToDisk(currentFileName);
            sourceModified = false;
            mListener.changeActionBarTitle(currentFileName, sourceModified);
        }else{
            showFileActionDialog(Const.SAVE_SOURCE_DIALOG, Const.SAVE_SOURCE_FILE);
        }
    }

    /**
     * Writes the file currently shown on the screen to disk.
     * @param filename  The name of the file to save
     */
    void writeTextToDisk(final String filename){
        // Since file I/O is a good candidate for concurrency...threads!
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    FileOutputStream fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                    for(String line : textbuffer){
                        fos.write( (line + "\n").getBytes() );
                    }
                    fos.close();
                }catch(IOException e){
                    Toast.makeText(getActivity(), "Unable to save file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        thread.start();
        Toast.makeText(getActivity(), "saved " + filename, Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens the file specified in the parameter and
     * fills the text file buffer.
     * @param filename   The name of the file to open
     */
    void loadTextFile(String filename){
        mAdapter.clear();
        try {
            FileInputStream fis = getActivity().openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null)
                mAdapter.add(line);
            reader.close();
            fis.close();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Unable to open file", Toast.LENGTH_SHORT).show();
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
    private class CustomRecyclerViewAdapter extends
            RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder>{

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private int mItemSelectedBackground;
        private List<String> mTextData;

        // Used to hold the selected items when in action mode
        private HashMap<Integer, Boolean> mSelections = new HashMap<>();
        private HashSet<Integer> mSelectedItems = new HashSet<>();


        class ViewHolder extends RecyclerView.ViewHolder{

            public final View mView;
            public final TextView mLabel;
            public final TextView mInstruction;
            public final TextView mComment;

            public ViewHolder(View view){
                super(view);
                mView = view;
                mLabel = (TextView) view.findViewById(R.id.label_text);
                mInstruction = (TextView) view.findViewById(R.id.instruction_text);
                mComment = (TextView) view.findViewById(R.id.comment_text);
            }
        }

        /**
         * Constructor
         * @param context
         * @param items
         */
        public CustomRecyclerViewAdapter(Context context, List<String> items){
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            context.getTheme().resolveAttribute(R.drawable.abc_list_pressed_holo_light, mTypedValue, true);
            //context.getTheme().resolveAttribute(R.attr.colorControlActivated, mTypedValue, true);
            mItemSelectedBackground = mTypedValue.resourceId;
            mTextData = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.editor_list_item, parent, false);
            //view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position){
            String [] tempArray = mTextData.get(position).split(";");
            try{
                holder.mLabel.setText(tempArray[0]);
                holder.mInstruction.setText(tempArray[1]);
                holder.mComment.setText(tempArray[2]);
            }catch (ArrayIndexOutOfBoundsException e){
                Log.d("EDITOR_FRAGMENT", "array problem");
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "Item: " + Integer.toString(position) + " clicked", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            });

            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    /* Are we in Action Mode? */
                    if (mActionMode != null) {
                        /* In Action Mode. Two possible scenarios here:
                        either another view in the list has been selected, or the same view
                        has been selected again. */
                        if (mSelectedItems.add(position)) {
                            view.setBackgroundResource(mItemSelectedBackground);
                        }else{
                            /* Selected view already selected. Remove from selections */
                            mSelectedItems.remove(position);
                            view.setBackgroundResource(mBackground);
                            if (mSelectedItems.isEmpty()) {
                                mActionMode.finish();
                                return true;
                            }
                        }
                        mActionMode.invalidate();
                        return true;
                    }
                    /* Not in Action Mode. Enter Action Mode */
                    mActionMode = getActivity().startActionMode(mActionModeCallback);

                    mSelectedItems.add(position);
                    view.setBackgroundResource(mItemSelectedBackground);
                    return true;
                }
            });

            // todo: need to handle selected items. ie background color and all that
            if(mSelectedItems.contains(position)){
                holder.mView.setBackgroundResource(mItemSelectedBackground);
                //holder.mView.setBackgroundResource(R.drawable.abc_list_longpressed_holo);
            }else{
                holder.mView.setBackgroundResource(mBackground);
            }

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

        public void setItem(String string, int position){
            mTextData.set(position, string);
            notifyItemChanged(position);
        }

        /** Invoked when exiting from Action Mode using the back button. Will clear
         * all selections and force a full re-binding.
         */
        public void clearSelections(){
            mSelectedItems.clear();
            // Full re-binding. Not most the efficient.
            notifyDataSetChanged();
        }

        public Set<Integer> getSelections(){
            return mSelections.keySet();
        }

        public boolean multipleItemsSelected(){
            return (mSelectedItems.size() > 1);
        }

        public boolean hasSelections(){
            return !mSelectedItems.isEmpty();
        }
    }

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
            if(mRecyclerAdapter.hasSelections()){
                mRecyclerAdapter.clearSelections();
            }

        }
    };

}

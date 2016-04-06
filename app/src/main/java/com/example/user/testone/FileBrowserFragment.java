package com.example.user.testone;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by user on 11/11/14.
 */
public class FileBrowserFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String FRAGMENT_BAR_TITLE = "File Browser";
    private static final String FRAGMENT_TAG = "FILE_BROWSER";

    private OnFileActionListener mListener;
    private ArrayList<String> fileList;
    private ActionMode mActionMode;
    private BrowserAdapter mAdapter;

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

    /**
     * constructor
     */
    public FileBrowserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            fileList = savedInstanceState.getStringArrayList(Const.STATE_ADAPTER);
        } else {
            fileList = new ArrayList<>();
        }

        mAdapter = new BrowserAdapter(getActivity(), fileList);

        if(savedInstanceState != null){
            mAdapter.mSelections = (TreeSet<Integer>) savedInstanceState.getSerializable("TREE_SET");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_browser_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /* get a handle on the recycler view and set it up */
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.browser_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(mAdapter);

        // Were we in Action Mode?
        if(savedInstanceState != null){
            if(savedInstanceState.getBoolean("ACTION_MODE_STATE")){
                mActionMode = getActivity().startActionMode(mActionModeCallback);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){}

    @Override
    public boolean onOptionsItemSelected(MenuItem item){return true;}

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putStringArrayList(Const.STATE_ADAPTER, fileList);
        outState.putSerializable("TREE_SET", mAdapter.mSelections);
        outState.putBoolean("ACTION_MODE_STATE", (mActionMode != null));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        // Verify that the host activity (MainActivity) implements the callback interface
        try{
            mListener = (OnFileActionListener) activity;
        }catch(ClassCastException e){
            // The activity doesn't implement the callback interface, throw exception
            throw new ClassCastException(activity.toString() +
                    " must implement EditorNoticeListener");
        }
    }

    /**
     * Used to reload the file list.
     */
    public void reloadFileList(){
        String [] files = getActivity().getFilesDir().list();
        mAdapter.clear();
        for(String file : files){
            mAdapter.addItem(file);
        }
    }

    /**
     * Custom RecyclerView.Adapter to be used in this fragment
     */
    private class BrowserAdapter extends RecyclerView.Adapter<BrowserAdapter.ViewHolder>{

        private List<String> mItems;

        private TreeSet<Integer> mSelections = new TreeSet<>();

        /**
         * The backgrounds used when an item is selected and when it isn't
         */
        private int mBackground;
        private int mSelectedBackground;

        // Constructor
        public BrowserAdapter(Context context, List<String> items){
            mBackground = ContextCompat.getColor(context, R.color.white);
            mSelectedBackground = ContextCompat.getColor(context, R.color.selection);
            mItems = items;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements
                View.OnClickListener, View.OnLongClickListener{

            private final View mRoot;
            private final ImageView mPicture;
            private final TextView mText;

            public ViewHolder(View view){
                super(view);
                mRoot = view;
                mRoot.setOnClickListener(this);
                mRoot.setOnLongClickListener(this);
                mPicture = (ImageView) view.findViewById(R.id.file_icon);
                mText = (TextView) view.findViewById(R.id.file_item_text);
            }

            @Override
            public void onClick(View view){
                int pos = getAdapterPosition();
                if (mActionMode != null) {
                    // In Action Mode. Either another item view in the list has
                    // been selected, or the same item view has been selected again.
                    if (!mSelections.add(pos)) {
                        mSelections.remove(pos);
                    }
                    //mActionMode.invalidate();
                    if (mSelections.isEmpty()) {
                        mActionMode.finish();
                    }
                    view.setBackgroundColor(mSelections.contains(pos)? mSelectedBackground : mBackground);
                } else {
                    // Let's determine whether the file to open is a source file
                    // or an object file and invoke the appropriate callback method
                    if(mItems.get(pos).contains(".s")){
                        mListener.editGivenFile(mItems.get(pos));
                    } else {
                        mListener.executeGivenFile(mItems.get(pos));
                    }
                }
            }

            @Override
            public boolean onLongClick(View view){
                int pos = getAdapterPosition();
                if (mActionMode != null) {
                    // In Action Mode. Either another item view in the list has
                    // been selected, or the same item view has been selected again.
                    if (!mSelections.add(pos)) {
                        mSelections.remove(pos);
                    }
                    //mActionMode.invalidate();
                    if (mSelections.isEmpty()){
                        mActionMode.finish();
                    }
                } else {
                    // Not in Action Mode. Enter Action Mode and add selection
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                    mSelections.add(pos);
                }
                view.setBackgroundColor(mSelections.contains(pos)? mSelectedBackground : mBackground);
                return true;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            return new ViewHolder( LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.browser_list_item, parent, false) );
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position){
            viewHolder.mText.setText(mItems.get(position));
            viewHolder.mPicture.setImageResource( mItems.get(position).contains(".s")?
                    R.drawable.asm_file_icon : R.drawable.obj_file_icon );
            viewHolder.mRoot.setBackgroundColor( mSelections.contains(position)?
                    mSelectedBackground : mBackground );
        }

        @Override
        public int getItemCount(){
            return mItems.size();
        }

        public void addItem(String string){
            mItems.add(string);
            notifyItemInserted(mItems.size() - 1);
        }

        public void setItem(int position, String string) {
            mItems.set(position, string);
            notifyDataSetChanged();
        }

        /**
         * Clears the textual data buffer and notifies that a full re-binding is needed
         */
        public void clear(){
            mItems.clear();
            notifyDataSetChanged();
        }

        /**
         * Invoked when exiting Action Mode using the back button. Clears
         * all selections, but doesn't delete them, and forces a full re-binding.
         */
        public void clearSelections(){
            mSelections.clear();
            // Full re-binding. Not the most efficient.
            notifyDataSetChanged();
        }

        /**
         * Deletes the selected items in the text buffer. Care must be taken
         * that the items are deleted in descending order since textbuffer will be
         * resized after every remove() operation. Index values greater than the index previously
         * removed will point to invalid locations.
         */
        public void removeSelections(){
            while(!mSelections.isEmpty()){
                int position = mSelections.pollLast();
                File fileToDelete = getActivity().getFileStreamPath(mItems.get(position));
                if(fileToDelete.delete()){
                    mItems.remove(position);
                    notifyItemRemoved(position);
                } else {
                    Log.d(FRAGMENT_TAG, "File: " + mItems.get(position) + " not deleted.");
                }
            }
            //reloadFileList();
            //Integer [] selected = new Integer[set.size()];
            //set.toArray(selected);
            //for(int i : mSelections){
              //  File fileToDelete = getActivity().getFileStreamPath(fileList.get(i).toString());
                //if(!fileToDelete.delete())
                  //  Log.d(FRAGMENT_TAG, "File: " + i.toString() + " not deleted.");
            //}
        }

        public boolean hasSelections(){
            return !mSelections.isEmpty();
        }
    } // end of adapter class

    /**
     *
     */
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            mode.getMenuInflater().inflate(R.menu.context_execute_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        // Return false if nothing is done
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete_selection:
                    mAdapter.removeSelections();
                    mode.finish();
                    return true;
                case R.id.action_execute_selection:
                    // not implemented yet
                    // will execute selected object files even in cases of mixed selections
                    // inform user if no executable files selected
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            if (mAdapter.hasSelections()){
                mAdapter.clearSelections();
            }
        }
    }; // end of ActionMode.Callback mActionModeCallback

}

package com.example.user.testone;

import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PageFragmentListener, OnFileActionListener {

    static final int EDITOR_FRAGMENT = 0;
    static final int BROWSER_FRAGMENT = 1;
    static final int RESULTS_FRAGMENT = 2;

    private Toolbar toolbar;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    public static String fileToOpen = null;

    // We may need these to call on the various fragments in the ViewPager
    EditorFragment editorHandle;
    FileBrowserFragment browserHandle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        // Init ViewPagerAdapter
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(pageChangeListener);         // Add a page change listener
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_assemble:
                assembleSourceCode();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Instantiates the appropriate fragment based on position.
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return EditorFragment.newInstance(position);
                case 1: return FileBrowserFragment.newInstance(position);
                default: return ResultFragment.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            return 3;  // There are three panels
        }
    }

    /**
     * Used to change the action bar title. Invoked every time
     * there's a page change.
     * @param bar_title  The new action bar title
     */
    public void changeActionBarTitle(String bar_title, Boolean isModified){
        if(isModified)
            getSupportActionBar().setTitle(bar_title + "*");
        else
            getSupportActionBar().setTitle(bar_title);
    }

    /**
     * Callback method invoked by the FileBrowserFragment class when the user
     * chooses to edit a file different from the one currently being edited.
     * @param file
     */
    public void editGivenFile(String file){
        // Let's try to get a handle on the editor fragment
        editorHandle = (EditorFragment) getFragmentFromPager(EDITOR_FRAGMENT);
        if(editorHandle != null)
            editorHandle.openNewFile(file);

        // Let's bring editor fragment to the fore.
        mViewPager.setCurrentItem(EDITOR_FRAGMENT);
    }

    public void executeSelectedFiles(List<String> files){

    }

    /**
     * Callback method invoked by the FileBrowserFragment class when the user
     * chooses to execute an object file
     * @param fileName
     */
    public void executeGivenFile(String fileName){

        ArrayList<Short> objectCode = new ArrayList<>();
        File file = new File(getFilesDir(), fileName);
        if(file.exists()){
            try{
                RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
                // let's read until EOFException is thrown
                while(true)
                    try {
                        objectCode.add(accessFile.readShort());
                    }catch (EOFException e){
                        break;
                    }

                accessFile.close();

            }catch (IOException e) {
                e.printStackTrace();
                Log.d("MAIN_ACTIVITY", "Unable to open object file");
            }

            VirtualMachine vm = new VirtualMachine();
            vm.loadObjectFile(objectCode);
            try {
                vm.runProcess();
            }catch(ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
            }

            if( vm.returnStatus == VirtualMachine.HALT_INSTRUCTION ) {
                int [] reg = vm.getRegisterStatus();
                String[] results = new String[4];
                for (int i = 0; i < reg.length; i++)
                    results[i] = Integer.toString(reg[i]);


                ResultFragment fragment = (ResultFragment) getFragmentFromPager(2);
                if (fragment != null) {
                    mViewPager.setCurrentItem(RESULTS_FRAGMENT);
                    fragment.showResults(results, fileName);
                }
                Log.d("MAIN_ACTIVITY", "VM finished successfully");
            }
        }else{
            Log.d("MAIN_ACTIVITY", "Object file:" + fileName + " does not exist");
        }

    }

    /**
     * Used to get a handle on any particular page (fragment) from the pager.
     * The returned value must be casted to a specific fragment class.
     * @return
     */
    Object getFragmentFromPager(int position){
        Object fragment = null;
        try{
            fragment = mSectionsPagerAdapter.instantiateItem(mViewPager, position);
        }catch (Exception e){
            Log.d("MAIN_ACTIVITY", "Error while trying to get handle on fragment (NULL)");
        }
        return fragment;
    }

    /**
     * Used to react to page changes. Many things need to happen when a new page is selected
     */
    ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener(){

        @Override
        public void onPageSelected(int position){
            switch (position){
                case EDITOR_FRAGMENT:
                    ((EditorFragment)getFragmentFromPager(position)).getCorrectTitle() ;
                    break;
                case BROWSER_FRAGMENT:
                    ((FileBrowserFragment)getFragmentFromPager(position)).reloadFileList();
                    changeActionBarTitle("File Browser", false);
                    break;
                case RESULTS_FRAGMENT:
                    changeActionBarTitle("Program Statistics", false);
            }
        }
    };

    /**
     * Assembles the source code currently in the editor. If successful, the {@link Assembler}
     * will save the compiled file in the internal storage.
     */
    public void assembleSourceCode(){
        editorHandle = (EditorFragment) getFragmentFromPager(EDITOR_FRAGMENT);
        if (editorHandle != null){
            if (editorHandle.getTextBuffer().isEmpty()){
                Snackbar.make(getCurrentFocus(), "There's nothing to compile.", Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                new Assembler(this, editorHandle.getTextBuffer(), editorHandle.getCurrentFileName())
                        .execute();
            }
        }
    }

}

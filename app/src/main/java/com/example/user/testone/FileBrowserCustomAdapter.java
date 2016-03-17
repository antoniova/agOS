package com.example.user.testone;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by theone on 3/31/15.
 */
public class FileBrowserCustomAdapter extends ArrayAdapter<String> {

    // Used to hold the selected items when in action mode
    HashMap<Integer, Boolean> mSelections = new HashMap<Integer, Boolean>();


    /*
     * Let's use the view holder pattern
     */
    static class ViewHolder{
        TextView text;
        ImageView icon;
    }


    /**
     * Constructor. There are more constructors that need to be
     * implemented. We can get away with defining just this one for now.
     * @param context
     * @param resource
     * @param objects
     */
    FileBrowserCustomAdapter(Context context, int resource, List objects){
        super(context, resource, objects);
    }

    /**
     * Creates the view widget for each row item in the file browser list
     * Each list item contains an icon and a file name. The icon needs to be
     * set according to the file type.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        View view;
        LayoutInflater inflater;

        view = convertView;
        // Check if recycling is possible
        if (view == null) {
            // New view. Must inflate layout
            inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.browser_list_item, parent, false);

            // We can avoid expensive calls to findViewById() by storing
            // all the pertinent information in a ViewHolder
            ViewHolder holder = new ViewHolder();
            holder.text = (TextView) view.findViewById(R.id.file_item_text);
            holder.icon = (ImageView) view.findViewById(R.id.file_icon);
            view.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        String item = getItem(position);
        holder.text.setText(item.toString());

        // The file extension determines which file icon to use
        String [] tempArray = item.toString().split("\\.");
        if(tempArray[1].equals("s")){
            holder.icon.setImageResource(R.drawable.asm_file_icon);
        }else{
            holder.icon.setImageResource(R.drawable.obj_file_icon);
        }

        if(mSelections.get(position) != null){
           view.setBackgroundResource(R.drawable.abc_list_longpressed_holo);
        }else{
            view.setBackgroundResource(0);
        }

        return view;
    }


    public void setNewSelection(int position, boolean value){
        mSelections.put(position, value);
        notifyDataSetChanged();
    }

    public void removeSelection(int position){
        mSelections.remove(position);
        notifyDataSetChanged();
    }

    public void clearSelection(){
        mSelections = new HashMap<Integer, Boolean>();
        notifyDataSetChanged();
    }

    public Set<Integer> getSelections(){
        return mSelections.keySet();
    }

    public boolean selectionExecutable(){
        for(int i : mSelections.keySet()){
            if(getItem(i).contains(".s"))
                return false;
        }
        return true;
    }

}

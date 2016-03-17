package com.example.user.testone;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;

/**
 * Created by user on 11/10/14.
 */
public class CustomEditorAdapter extends ArrayAdapter<String> {

    // Used to hold the selected items when in action mode
    HashMap<Integer, Boolean> mSelections = new HashMap<Integer, Boolean>();

    static class ViewHolder{
        public TextView label;
        public TextView instr;
        public TextView comment;
    }

    /**
     * Constructor. There are more constructors that need to be
     * implemented. We can get away with defining just this one for now.
     * @param context
     * @param resource
     * @param objects
     */
    CustomEditorAdapter(Context context, int resource, List objects){
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view;
        LayoutInflater inflater;

        view = convertView;
        if(view == null){
            inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.editor_list_item, parent, false);

            // Viewholder
            ViewHolder holder = new ViewHolder();
            holder.label = (TextView) view.findViewById(R.id.label_text);
            holder.instr = (TextView) view.findViewById(R.id.instruction_text);
            holder.comment = (TextView) view.findViewById(R.id.comment_text);
            view.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        String item = getItem(position);
        String [] tempArray = item.split(";");
        try{
            holder.label.setText(tempArray[0]);
            holder.instr.setText(tempArray[1]);
            holder.comment.setText(tempArray[2]);
        }catch (ArrayIndexOutOfBoundsException e){
            Log.d("EDITOR_FRAGMENT", "array problem");
        }

        if(mSelections.get(position) != null){
            view.setBackgroundResource(R.drawable.abc_list_longpressed_holo);
        }else{
            view.setBackgroundResource(0);
        }

        // AG

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


}

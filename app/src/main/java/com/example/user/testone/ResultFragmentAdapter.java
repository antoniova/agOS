package com.example.user.testone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * Created by theone on 4/20/15.
 */
public class ResultFragmentAdapter extends ArrayAdapter<String>{

    /**
     * Let's use the view holder pattern
     */
    static class ViewHolder{
        TextView title;
        TextView reg0;
        TextView reg1;
        TextView reg2;
        TextView reg3;
    }

    /*
     * constructor
     */
    ResultFragmentAdapter(Context context, int resource, List objects){
        super(context, resource, objects);
    }

    /**
     * Creates the view widget for each row item in the file browser list
     * Each list item contains an icon and a file name. The icon needs to be
     * set according to the file type.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        // Check if recycling is possible
        if (convertView == null) {
            // New view. Must inflate layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.cardview_layout, parent, false);

            // We can avoid expensive calls to findViewById() by storing
            // all the pertinent information in a ViewHolder
            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.program_title);
            holder.reg0 = (TextView) convertView.findViewById(R.id.register0);
            holder.reg1 = (TextView) convertView.findViewById(R.id.register1);
            holder.reg2 = (TextView) convertView.findViewById(R.id.register2);
            holder.reg3 = (TextView) convertView.findViewById(R.id.register3);
            convertView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();

        String [] tempArray = getItem(position).split(";");
        holder.title.setText(tempArray[0]);
        holder.reg0.setText("Register 0 : " + tempArray[1]);
        holder.reg1.setText("Register 1 : " + tempArray[2]);
        holder.reg2.setText("Register 2 : " + tempArray[3]);
        holder.reg3.setText("Register 3 : " + tempArray[4]);

        return convertView;
    }

}

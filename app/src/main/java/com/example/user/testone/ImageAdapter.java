package com.example.user.testone;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by user on 11/11/14.
 */

public class ImageAdapter extends BaseAdapter {

    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(200,200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8,8,8,8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);



        return imageView;
    }



    private Integer[] mThumbIds = {
            R.drawable.comment,
            R.drawable.load, R.drawable.loadi,
            R.drawable.store, R.drawable.add,
            R.drawable.addi, R.drawable.addc,
            R.drawable.addci, R.drawable.sub,
            R.drawable.subi, R.drawable.subc,
            R.drawable.subci, R.drawable.and,
            R.drawable.andi, R.drawable.xor,
            R.drawable.xori, R.drawable.compl,
            R.drawable.shl, R.drawable.shla,
            R.drawable.shr, R.drawable.shra,
            R.drawable.compr, R.drawable.compri,
            R.drawable.getstat, R.drawable.putstat,
            R.drawable.jump, R.drawable.jumpl,
            R.drawable.jumpe, R.drawable.jumpg,
            R.drawable.call, R.drawable.ret,
            R.drawable.read, R.drawable.write,
            R.drawable.halt, R.drawable.noop,
    };

}

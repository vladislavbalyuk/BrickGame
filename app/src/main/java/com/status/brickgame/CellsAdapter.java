package com.status.brickgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;


public class CellsAdapter extends ArrayAdapter<Integer> {

    private LayoutInflater inflater;
    private int layout;
    private List<Integer> cells;

    public CellsAdapter(Context context, int resourse, List<Integer> cells){
        super(context, resourse, cells);
        this.cells = cells;
        this.layout = resourse;
        this.inflater = LayoutInflater.from(context);
    }


    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Integer item = cells.get(position);

        if(item.intValue() == 0) {
            viewHolder.imageView.setImageDrawable(null);
        }
        else{
            viewHolder.imageView.setImageResource(item.intValue());
        }

        return convertView;
    }
    private class ViewHolder {
        final ImageView imageView;
        ViewHolder(View view){
            imageView = (ImageView)view.findViewById(R.id.image);
        }
    }

}

package com.seta.tollroaddroid.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.seta.tollroaddroid.app.R;
import com.seta.tollroaddroid.app.model.MenuItem;

import java.util.ArrayList;

/**
 * Created by thomas on 2016-02-29.
 */
public class MenuItemAdapter extends ArrayAdapter<MenuItem> {
    private final Context context;
    private final ArrayList<MenuItem> menuItems;

    public MenuItemAdapter(Context context, int resource, ArrayList<MenuItem> menuItems) {
        super(context, resource, menuItems);

        this.context = context;
        this.menuItems = menuItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_menu, parent, false);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.iv_icon);
        TextView textView = (TextView) rowView.findViewById(R.id.tv_name);

        if (menuItems.get(position).getIconResource() != 0) {
            imageView.setImageResource(menuItems.get(position).getIconResource());
        }
        textView.setText(menuItems.get(position).getName());

        return rowView;
    }
}
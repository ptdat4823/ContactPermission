package com.example.loadcontacts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class contactAdapter extends ArrayAdapter<contactModel> {
    private Context context;

    private final int resourceLayout;


    public contactAdapter(Context context, int resourceLayout, List<contactModel> contacts) {
        super(context, resourceLayout, contacts);
        this.context = context;
        this.resourceLayout = resourceLayout;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if(view == null)
        {
            view = LayoutInflater.from(context).inflate(resourceLayout,null);
        }

        contactModel contactModel = getItem(position);
        TextView name = (TextView) view.findViewById(R.id.contact_name);
        TextView phoneNumber = (TextView) view.findViewById(R.id.contact_number);

        name.setText(contactModel.getName());
        phoneNumber.setText(contactModel.getPhoneNumber());

        return view;
    }

}

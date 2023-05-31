package com.example.loadcontacts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loadcontacts.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int READ_CONTACTS_REQUEST_CODE = 1;
    public static final int WRITE_CONTACTS_REQUEST_CODE = 2;
    public static final int CONTACT_LOADER = 1;
    private ActivityMainBinding binding;
    List<contactModel> listContacts;

    public boolean isASC = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
        setContentView(binding.getRoot());
    }

    @Override public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_CONTACTS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LoaderManager.getInstance(this).restartLoader(this.CONTACT_LOADER,null,this);
            }
            else {
                finish();
            }
        }
    }
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == CONTACT_LOADER) {
            String[] SELECTED_FIELDS = new String[] {
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                };
            return new CursorLoader(this, ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    SELECTED_FIELDS,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " " + (isASC ? "ASC" : "DESC")); }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CONTACT_LOADER) {
            listContacts = new ArrayList<>();
            if (data != null) {
                while (!data.isClosed() && data.moveToNext()) {
                    String phoneNumber = data.getString(1);
                    String name = data.getString(2);
                    listContacts.add(new contactModel(name, phoneNumber));
                }
                binding.listviewContacts.setAdapter(new contactAdapter(this, R.layout.contact_item, listContacts));
                data.close();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        loader = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.option_add:
            {
                showPopupAddContact();
                break;
            }
            case R.id.option_asc:
            {
                isASC = true;
                LoaderManager.getInstance(this).restartLoader(this.CONTACT_LOADER,null,this);
                break;
            }
            case R.id.option_desc:
            {
                isASC = false;
                LoaderManager.getInstance(this).restartLoader(this.CONTACT_LOADER,null,this);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopupAddContact()
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_add_contact);

        EditText tvName = dialog.findViewById(R.id.name);
        EditText tvPhoneNumber = dialog.findViewById(R.id.phonenumber);
        AppCompatButton btnAdd = dialog.findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = tvName.getText().toString();
                String phoneNumber = tvPhoneNumber.getText().toString();
                if(checkPhoneNumber(phoneNumber))
                    insertContact(name,phoneNumber);
                else
                    Toast.makeText(MainActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
    }

    private void insertContact(String name, String phoneNumber)
    {
        Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
        contactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        contactIntent
                .putExtra(ContactsContract.Intents.Insert.NAME, name)
                .putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        startActivityIfNeeded(contactIntent, WRITE_CONTACTS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == WRITE_CONTACTS_REQUEST_CODE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Toast.makeText(this, "Add contact successfully!!", Toast.LENGTH_SHORT).show();
                LoaderManager.getInstance(this).restartLoader(this.CONTACT_LOADER,null,this);
            }
            else
            {
                Toast.makeText(this, "Add contact fail!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPhoneNumber(String phoneNumber)
    {
        if(phoneNumber.length() != 10)
            return false;
        return true;
    }
}
package com.sausagetaste.book_wishlist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Vector;


public class MainActivity extends AppCompatActivity {

    // Definitions

    public static final String EXTRA_MESSAGE = "com.sausagetaste.book_wishlist.MESSAGE";

    private class BookListView_OnClickListener implements AdapterView.OnItemClickListener {

        final private AppCompatActivity parent_activity;

        BookListView_OnClickListener(AppCompatActivity activity) {
            this.parent_activity = activity;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String item_text = adapter.getItem(i);

            Intent intent = new Intent(this.parent_activity, DetailActivity.class);
            intent.putExtra(EXTRA_MESSAGE, item_text);
            startActivity(intent);
        }
    }

    class OnClickFAB implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
            builder.setTitle("Title");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String input_text = input.getText().toString();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

    }


    // Attributes

    private Vector<String> list_item_texts;
    private ArrayAdapter<String> adapter;


    // Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.list_item_texts = new Vector<String>();
        this.adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, this.list_item_texts
        );

        ListView lv = (ListView) this.findViewById(R.id.book_list_view);
        lv.setOnItemClickListener(new BookListView_OnClickListener(this));
        lv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new OnClickFAB());

        for (int i = 0; i < 10000; ++i) {
            this.list_item_texts.add("사람 " + Integer.toString(i));
        }
    }

}

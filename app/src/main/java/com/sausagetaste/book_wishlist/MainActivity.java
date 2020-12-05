package com.sausagetaste.book_wishlist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private static class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... url) {
            return this.download_html(url[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (null == result) {
                Log.v("Download Task", "Failed to get html");
            }
            else {
                Log.v("Download Task", result);
            }
        }

        private String download_html(final String url_str) {
            try {
                URL url = new URL(url_str);

                HttpURLConnection url_conn = (HttpURLConnection) url.openConnection();
                url_conn.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0"
                );

                url_conn.connect();
                Reader r = new InputStreamReader(url_conn.getInputStream());
                StringBuilder buf = new StringBuilder();

                while (true) {
                    int ch = r.read();
                    if (ch < 0) {
                        break;
                    }
                    else {
                        buf.append((char) ch);
                    }
                }

                return buf.toString();
            }
            catch (Exception e) {
                Log.e("Download Task", e.toString());
                return null;
            }
        }

    }

    class OnClickFAB implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
            builder.setTitle("Title");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText("https://ridibooks.com/books/1811176154");
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String input_text = input.getText().toString();

                    DownloadTask task = new DownloadTask();
                    task.execute(input_text);
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

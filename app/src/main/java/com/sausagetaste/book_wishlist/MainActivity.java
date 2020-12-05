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
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

    private class LoadHTMLTask extends AsyncTask<String, Integer, String> {

        final private MainActivity parent_activity;

        LoadHTMLTask(MainActivity activity) {
            this.parent_activity = activity;
        }

        @Override
        protected String doInBackground(String... url) {
            return this.download_html(url[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (null != result) {
                this.parent_activity.process_html(result);
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
            catch (MalformedURLException e) {
                Toast.makeText(
                        this.parent_activity, "Invalid url: " + url_str, Toast.LENGTH_LONG
                ).show();
                Log.e("Download Task", e.toString());
                return null;
            }
            catch (Exception e) {
                Toast.makeText(
                        this.parent_activity, "failed to load url: " + url_str, Toast.LENGTH_LONG
                ).show();
                Log.e("Download Task", e.toString());
                return null;
            }
        }

    }

    class OnClickFAB implements View.OnClickListener {

        final private MainActivity parent_activity;

        OnClickFAB(MainActivity parent_activity) {
            this.parent_activity = parent_activity;
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
            builder.setTitle("Title");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            //input.setText("https://ridibooks.com/books/606002239");
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String input_text = input.getText().toString();

                    LoadHTMLTask task = new LoadHTMLTask(parent_activity);
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
    private Vector<Integer> list_item_ids;
    private ArrayAdapter<String> adapter;
    private DBManager db_man;


    // Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.list_item_texts = new Vector<String>();
        this.list_item_ids = new Vector<Integer>();
        this.adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, this.list_item_texts
        );
        this.db_man = new DBManager(this);

        ListView lv = (ListView) this.findViewById(R.id.book_list_view);
        lv.setOnItemClickListener(new BookListView_OnClickListener(this));
        lv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new OnClickFAB(this));

        this.update_book_list();
    }

    public void process_html(final String html) {
        BookStoreParser.Parser_HTML parser = BookStoreParser.select_html_parser(html);

        DBManager.BookRecord data = new DBManager.BookRecord();
        data.title = parser.find_title();
        data.cover_url = parser.find_cover_url();
        data.note = "";
        this.db_man.override_record(data);

        this.update_book_list();
    }

    private void update_book_list() {
        this.list_item_texts.clear();
        this.list_item_ids.clear();
        this.db_man.get_all_titles(this.list_item_ids, this.list_item_texts);
        this.adapter.notifyDataSetChanged();

        Log.v("update_book_list", Integer.toString(this.list_item_texts.size()));
    }

}

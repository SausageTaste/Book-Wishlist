package com.sausagetaste.book_wishlist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Vector;


public class MainActivity extends AppCompatActivity implements EventManager.HTMLLoadedListener {

    // Definitions

    public static final String EXTRA_BOOK_ID = "com.sausagetaste.book_wishlist.BOOK_ID";

    private class BookListView_OnClickListener implements AdapterView.OnItemClickListener {

        final private MainActivity parent_activity;

        BookListView_OnClickListener(MainActivity activity) {
            this.parent_activity = activity;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final int item_id = this.parent_activity.list_item_ids.elementAt(i);
            final DBManager.BookRecord book_record = this.parent_activity.db_man.get_record_with_id(item_id);

            final String file_path = this.parent_activity.make_png_path_of_id(item_id);
            File file = new File(file_path);
            if (file.exists()) {
                Log.i("onItemClick", "File exists: " + file_path);
            }
            else {
                Log.i("onItemClick", "Start downloading: " + file_path);
                DownloadImageTask task = new DownloadImageTask(item_id, file_path);
                task.execute(book_record.cover_url);
            }

            Intent intent = new Intent(this.parent_activity, DetailActivity.class);
            intent.putExtra(EXTRA_BOOK_ID, item_id);
            startActivity(intent);
        }

    }

    private static class LoadHTMLTask extends AsyncTask<String, Integer, String> {

        private String url;

        @Override
        protected String doInBackground(String... url) {
            this.url = url[0];
            return this.download_html(this.url);
        }

        @Override
        protected void onPostExecute(String result) {
            EventManager.get_inst().notify_html_loaded(result, this.url);
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
                Log.e("Load HTML Task", e.toString());
                return null;
            }
        }

    }

    private static class DownloadImageTask extends AsyncTask<String, Integer, String>  {

        final int book_id;
        final String file_path;

        DownloadImageTask(final int book_id, final String file_path) {
            this.book_id = book_id;
            this.file_path = file_path;
        }

        @Override
        protected String doInBackground(String... strings) {
            Bitmap bitmap = this.get_bitmap_from_url(strings[0]);
            Log.i("Download Image Task", "Image downloaded: " + strings[0] + ", " + bitmap.getWidth() + ", " + bitmap.getHeight());

            this.save_bitmap_to_dist(bitmap);
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            EventManager.get_inst().notify_image_downloaded();
        }

        private Bitmap get_bitmap_from_url(final String url_str) {
            try {
                URL url = new URL(url_str);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            }
            catch (Exception e) {
                Log.e("Download Image Task", e.toString());
                return null;
            }
        }

        private void save_bitmap_to_dist(final Bitmap bitmap) {
            try {
                FileOutputStream out = new FileOutputStream(this.file_path);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Log.i("Download Image Task", "Image saved: " + this.file_path);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    class OnClickFAB implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
            builder.setTitle("Enter URL of book");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                final String input_text = input.getText().toString();

                LoadHTMLTask task = new LoadHTMLTask();
                task.execute(input_text);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

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

        this.list_item_texts = new Vector<>();
        this.list_item_ids = new Vector<>();
        this.adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, this.list_item_texts
        );
        this.db_man = new DBManager(this);

        ListView lv = (ListView) this.findViewById(R.id.book_list_view);
        lv.setOnItemClickListener(new BookListView_OnClickListener(this));
        lv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new OnClickFAB());

        EventManager.get_inst().register_html_loaded(this);
        this.update_book_list();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.update_book_list();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventManager.get_inst().deregister_html_loaded(this);
    }

    @Override
    public void notify_html_loaded(final String html, final String url_origin) {
        this.process_html(html, url_origin);
        this.update_book_list();
    }

    public void process_html(final String html, final String url_origin) {
        BookStoreParser.Parser_HTML parser = BookStoreParser.select_html_parser(html);

        DBManager.BookRecord data = new DBManager.BookRecord();
        data.title = parser.find_title();
        data.cover_url = parser.find_cover_url();
        data.rating_normalized = parser.find_rating_normalized();
        data.isbn = parser.find_isbn();
        data.description = parser.find_description();
        data.note = "";
        data.url_origin = url_origin;
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

    public static String make_png_path_of_id_static(final int book_id, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Paths.get(context.getFilesDir().toString(), book_id + ".png").toString();
        }
        else {
            throw new RuntimeException("Paths.get method is not supported");
        }
    }

    public String make_png_path_of_id(final int book_id) {
        return make_png_path_of_id_static(book_id, this);
    }

}

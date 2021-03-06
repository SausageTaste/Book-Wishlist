package com.sausagetaste.book_wishlist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;


public class DetailActivity extends AppCompatActivity implements EventManager.ImageDownloadedListener {

    private DBManager db_man;
    private int book_id;

    private String url_origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_detail);

        this.db_man = new DBManager(this);

        Intent intent = this.getIntent();
        this.book_id = intent.getIntExtra(MainActivity.EXTRA_BOOK_ID, -1);
        DBManager.BookRecord book_info = this.db_man.get_record_with_id(this.book_id);
        this.url_origin = book_info.url_origin;

        {
            TextView textView = this.findViewById(R.id.text_view_title);
            textView.setText("Title : " + book_info.title);

            textView = this.findViewById(R.id.text_view_rating_normalized);
            textView.setText("Rating : " + book_info.rating_normalized.toString());

            textView = this.findViewById(R.id.text_view_isbn);
            textView.setText("ISBN : " + book_info.isbn);

            textView = this.findViewById(R.id.text_view_description);
            textView.setText("Description : " + book_info.description);

            EditText editText = this.findViewById(R.id.edit_text_note);
            editText.setText(book_info.note);
        }

        this.set_cover_img_if_exists();
        EventManager.get_inst().register_image_downloaded(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.get_inst().deregister_image_downloaded(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.book_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_open_web:
                on_open_url_origin();
                return true;
            case R.id.action_delete:
                this.on_delete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void on_btn_save_clicked(View v) {
        DBManager.BookRecord new_data = new DBManager.BookRecord();
        new_data.id = this.book_id;
        EditText editText = this.findViewById(R.id.edit_text_note);
        new_data.note = editText.getText().toString();

        this.db_man.override_record(new_data);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    public void on_delete() {
        this.db_man.delete_by_id(this.book_id);

        final String file_path = MainActivity.make_png_path_of_id_static(this.book_id, this);
        File file = new File(file_path);
        final boolean deleted = file.delete();

        this.finish();
    }

    public void on_open_url_origin() {
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(this.url_origin)));
    }

    private boolean set_cover_img_if_exists() {
        final String file_path = MainActivity.make_png_path_of_id_static(this.book_id, this);
        File file = new File(file_path);

        if (file.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ImageView myImage = (ImageView) findViewById(R.id.book_cover_view);
            myImage.setImageBitmap(myBitmap);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void notify_image_downloaded() {
        this.set_cover_img_if_exists();
    }

}

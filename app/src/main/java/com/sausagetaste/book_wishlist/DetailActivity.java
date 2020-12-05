package com.sausagetaste.book_wishlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class DetailActivity extends AppCompatActivity {

    private DBManager db_man;
    private int book_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_detail);

        this.db_man = new DBManager(this);

        Intent intent = this.getIntent();
        this.book_id = intent.getIntExtra(MainActivity.EXTRA_BOOK_ID, -1);
        DBManager.BookRecord book_info = this.db_man.get_record_with_id(this.book_id);

        TextView textView = this.findViewById(R.id.text_view_title);
        textView.setText(book_info.title);

        textView = this.findViewById(R.id.text_view_cover_url);
        textView.setText(book_info.cover_url);

        {
            EditText editText = this.findViewById(R.id.edit_text_note);
            editText.setText(book_info.note);
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

    public void on_btn_delete_clicked(View v) {
        this.db_man.delete_by_id(this.book_id);
        this.finish();
    }

}

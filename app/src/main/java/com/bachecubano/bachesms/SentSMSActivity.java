package com.bachecubano.bachesms;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.bachecubano.bachesms.adapters.MessagesAdapter;
import com.bachecubano.bachesms.models.Message;

import java.util.ArrayList;
import java.util.List;

public class SentSMSActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_sms);

        //Read all data in table
        DataBaseHelper myDb = new DataBaseHelper(this);
        List<Message> messageList = new ArrayList<>();

        Cursor cursor = myDb.getAllData();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Message message = new Message();
                    message.setTo_phone(cursor.getString(cursor.getColumnIndex(DataBaseHelper.COL_TO_PHONE)));
                    message.setMessage(cursor.getString(cursor.getColumnIndex(DataBaseHelper.COL_SMS_TEXT)));
                    message.setCreatedAt(cursor.getString(cursor.getColumnIndex(DataBaseHelper.COL_DATE_SENT)));
                    messageList.add(message);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("ErrorSQLTag", "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        int newMsgPosition = messageList.size() - 1;

        MessagesAdapter mMessageAdapter = new MessagesAdapter(messageList);
        RecyclerView mMessageRecycler = findViewById(R.id.reyclerview_message_list);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setHasFixedSize(true);
        mMessageRecycler.setItemAnimator(new DefaultItemAnimator());
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.scrollToPosition(newMsgPosition);
    }
}

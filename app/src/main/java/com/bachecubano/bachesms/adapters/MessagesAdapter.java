package com.bachecubano.bachesms.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bachecubano.bachesms.R;
import com.bachecubano.bachesms.models.Message;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter {

    private final List<Message> mMessageList;

    public MessagesAdapter(List<Message> messageList) {
        mMessageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        ((MessageHolder) holder).bind(message);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}

class MessageHolder extends RecyclerView.ViewHolder {

    private final TextView messageText;
    private final TextView timeText;
    private final TextView nameText;

    MessageHolder(View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.text_message_body);
        timeText = itemView.findViewById(R.id.text_message_time);
        nameText = itemView.findViewById(R.id.text_message_name);
        ImageView profileImage = itemView.findViewById(R.id.image_message_profile);
    }

    void bind(Message message) {
        messageText.setText(message.getMessage());
        nameText.setText(message.getTo_phone());
        timeText.setText(message.getCreatedAt());
        //timeText.setText(DateUtils.formatDateTime(mContext, Long.valueOf(message.getCreatedAt()), 0));
    }
}

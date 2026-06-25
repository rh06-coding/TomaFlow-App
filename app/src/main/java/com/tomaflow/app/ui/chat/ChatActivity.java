package com.tomaflow.app.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.tomaflow.app.R;
import com.tomaflow.app.data.model.ChatMessage;
import com.tomaflow.app.data.repository.ChatRepository;

public class ChatActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(com.tomaflow.app.utils.LanguageManager.wrap(base));
    }

    public static final String EXTRA_FRIEND_ID = "friend_id";
    public static final String EXTRA_FRIEND_NAME = "friend_name";
    public static final String EXTRA_FRIEND_AVATAR = "friend_avatar";

    private String currentUserId;
    private String friendId;
    private String friendName;
    private String friendAvatar;

    private String chatId;
    private ChatRepository chatRepository;
    private ChatAdapter chatAdapter;

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        friendId = getIntent().getStringExtra(EXTRA_FRIEND_ID);
        friendName = getIntent().getStringExtra(EXTRA_FRIEND_NAME);
        friendAvatar = getIntent().getStringExtra(EXTRA_FRIEND_AVATAR);

        if (friendId == null) {
            finish();
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRepository = new ChatRepository();
        chatId = chatRepository.getChatId(currentUserId, friendId);

        initHeader();
        initRecyclerView();
        initMessageInput();
        
        loadMessages();
    }

    private void initHeader() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        TextView tvName = findViewById(R.id.tv_header_name);
        tvName.setText(friendName != null ? friendName : "Chat");

        ImageView ivAvatar = findViewById(R.id.iv_header_avatar);
        TextView tvInitials = findViewById(R.id.tv_header_initials);

        if (friendAvatar != null && !friendAvatar.isEmpty()) {
            ivAvatar.setVisibility(View.VISIBLE);
            tvInitials.setVisibility(View.GONE);
            Glide.with(this).load(friendAvatar).circleCrop().into(ivAvatar);
        } else {
            ivAvatar.setVisibility(View.GONE);
            tvInitials.setVisibility(View.VISIBLE);
            String initials = "";
            if (friendName != null) {
                String[] parts = friendName.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    initials += parts[0].charAt(0);
                    if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                        initials += parts[parts.length - 1].charAt(0);
                    }
                }
            }
            tvInitials.setText(initials.toUpperCase());
        }
    }

    private void initRecyclerView() {
        rvChat = findViewById(R.id.rv_chat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(currentUserId, friendAvatar, friendName);
        rvChat.setAdapter(chatAdapter);
    }

    private void initMessageInput() {
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    btnSend.setEnabled(false);
                    btnSend.setAlpha(0.5f);
                } else {
                    btnSend.setEnabled(true);
                    btnSend.setAlpha(1.0f);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        ImageButton btnShare = findViewById(R.id.btn_share_achievement);
        btnShare.setOnClickListener(v -> {
            // Fetch stats to share
            com.tomaflow.app.data.remote.FirestoreSessionRemoteDataSource sessionSource = new com.tomaflow.app.data.remote.FirestoreSessionRemoteDataSource();
            sessionSource.fetchSessions(currentUserId, new com.tomaflow.app.data.remote.FirestoreSessionRemoteDataSource.SessionFetchCallback() {
                @Override
                public void onSuccess(java.util.List<com.tomaflow.app.data.db.entity.SessionEntity> sessions) {
                    int totalMinutes = 0;
                    for (com.tomaflow.app.data.db.entity.SessionEntity session : sessions) {
                        if ("Completed".equals(session.status)) {
                            totalMinutes += session.duration / 60;
                        }
                    }
                    int hours = totalMinutes / 60;
                    int level = (totalMinutes / 120) + 1;
                    
                    String achievementText = "🏆 I just reached Level " + level + " with " + hours + " hours of focus!";
                    sendMessage(achievementText, "achievement");
                }
                
                @Override
                public void onFailure(Exception e) {
                    com.tomaflow.app.utils.TomaToast.show(ChatActivity.this, "Failed to load stats", false);
                }
            });
        });

        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                sendMessage(content, "text");
                etMessage.setText("");
            }
        });
    }

    private void sendMessage(String content, String type) {
        ChatMessage message = new ChatMessage(
                null,
                currentUserId,
                friendId,
                content,
                type,
                System.currentTimeMillis(),
                false
        );
        chatRepository.sendMessage(chatId, message);
    }

    private void loadMessages() {
        chatRepository.getMessages(chatId).observe(this, messages -> {
            if (messages != null) {
                chatAdapter.submitList(messages);
                if (messages.size() > 0) {
                    rvChat.scrollToPosition(messages.size() - 1);
                }
            }
        });
    }
}


package com.example.aipet.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipet.R;
import com.example.aipet.data.model.Message;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_PET = 1;

    private final List<Message> messages;
    private final Set<Long> expandedThinkingKeys = new HashSet<>();

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message != null && Message.ROLE_USER.equals(message.getRole())) {
            return TYPE_USER;
        } else {
            return TYPE_PET;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_pet, parent, false);
            return new PetViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvMessage.setText(message.getContent());
            return;
        }

        if (holder instanceof PetViewHolder) {
            ((PetViewHolder) holder).bind(message, position);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static abstract class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMessage;

        MessageViewHolder(@NonNull View itemView, int textViewId) {
            super(itemView);
            tvMessage = itemView.findViewById(textViewId);
        }
    }

    static class UserViewHolder extends MessageViewHolder {

        UserViewHolder(@NonNull View itemView) {
            super(itemView, R.id.tv_message_user);
        }
    }

    class PetViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvThinkingToggle;
        private final TextView tvThinking;
        private final TextView tvAnswer;

        PetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvThinkingToggle = itemView.findViewById(R.id.tv_thinking_toggle);
            tvThinking = itemView.findViewById(R.id.tv_message_pet_thinking);
            tvAnswer = itemView.findViewById(R.id.tv_message_pet_answer);
        }

        void bind(@NonNull Message message, int position) {
            long thinkingKey = buildThinkingKey(message, position);
            ParsedReply parsed = ParsedReply.parse(message.getContent());
            tvAnswer.setText(parsed.answer);

            if (parsed.hasThinking()) {
                boolean expanded = expandedThinkingKeys.contains(thinkingKey);
                tvThinkingToggle.setVisibility(View.VISIBLE);
                tvThinkingToggle.setText(expanded
                        ? itemView.getContext().getString(R.string.chat_thinking_collapse)
                        : itemView.getContext().getString(R.string.chat_thinking_expand));
                tvThinking.setVisibility(expanded ? View.VISIBLE : View.GONE);
                tvThinking.setText(parsed.thinking);

                tvThinkingToggle.setOnClickListener(v -> {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }
                    long currentKey = buildThinkingKey(messages.get(adapterPosition), adapterPosition);
                    if (expandedThinkingKeys.contains(currentKey)) {
                        expandedThinkingKeys.remove(currentKey);
                    } else {
                        expandedThinkingKeys.add(currentKey);
                    }
                    notifyItemChanged(adapterPosition);
                });
            } else {
                tvThinkingToggle.setVisibility(View.GONE);
                tvThinking.setVisibility(View.GONE);
                tvThinkingToggle.setOnClickListener(null);
            }
        }

        private long buildThinkingKey(@NonNull Message message, int position) {
            long id = message.getId();
            if (id > 0) {
                return id;
            }
            long ts = message.getTimestamp();
            int contentHash = message.getContent() == null ? 0 : message.getContent().hashCode();
            return (((long) contentHash) << 32) ^ ts ^ position;
        }
    }

    private static final class ParsedReply {
        private static final String THINKING_TITLE = "【深度思考】";
        private static final String ANSWER_TITLE = "【回答】";

        final String thinking;
        final String answer;

        ParsedReply(String thinking, String answer) {
            this.thinking = thinking;
            this.answer = answer;
        }

        boolean hasThinking() {
            return thinking != null && !thinking.trim().isEmpty();
        }

        static ParsedReply parse(String content) {
            String source = content == null ? "" : content.trim();
            if (source.isEmpty()) {
                return new ParsedReply("", "");
            }

            int thinkingIdx = source.indexOf(THINKING_TITLE);
            int answerIdx = source.indexOf(ANSWER_TITLE);
            if (thinkingIdx >= 0 && answerIdx > thinkingIdx) {
                String thinking = source.substring(thinkingIdx + THINKING_TITLE.length(), answerIdx).trim();
                String answer = source.substring(answerIdx + ANSWER_TITLE.length()).trim();
                return new ParsedReply(thinking, answer);
            }

            if (thinkingIdx >= 0 && answerIdx < 0) {
                String thinking = source.substring(thinkingIdx + THINKING_TITLE.length()).trim();
                return new ParsedReply(thinking, "");
            }

            int fallbackThinking = source.indexOf("深度思考");
            int fallbackAnswer = source.indexOf("回答");
            if (fallbackThinking >= 0 && fallbackAnswer > fallbackThinking) {
                String thinking = source.substring(fallbackThinking, fallbackAnswer)
                        .replace("深度思考", "")
                        .replace("：", "")
                        .replace(":", "")
                        .trim();
                String answer = source.substring(fallbackAnswer)
                        .replace("回答", "")
                        .replace("：", "")
                        .replace(":", "")
                        .trim();
                return new ParsedReply(thinking, answer);
            }

            return new ParsedReply("", source);
        }
    }
}

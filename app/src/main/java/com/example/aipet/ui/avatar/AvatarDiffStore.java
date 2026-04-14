package com.example.aipet.ui.avatar;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * 角色差分图持久化与读取。
 */
public final class AvatarDiffStore {

    private static final String ROOT_DIR = "character_diff";

    private AvatarDiffStore() {
    }

    @NonNull
    public static String[] supportedEmotions() {
        return new String[]{"normal", "happy", "sad", "angry", "shy", "surprised", "excited"};
    }

    @NonNull
    public static String[] supportedEmotionLabels() {
        return new String[]{"常态", "开心", "伤心", "生气", "害羞", "惊讶", "兴奋"};
    }

    @NonNull
    public static String emotionTagFromIndex(int index) {
        String[] tags = supportedEmotions();
        if (index < 0 || index >= tags.length) {
            return "normal";
        }
        return tags[index];
    }

    public static void ensurePlaceholders(@NonNull Context context, @NonNull String outfitId) {
        if (TextUtils.isEmpty(outfitId)) {
            return;
        }
        for (String emotion : supportedEmotions()) {
            File dir = getEmotionDir(context, outfitId, emotion);
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            File mark = new File(dir, "PLACEHOLDER.txt");
            if (!mark.exists()) {
                try (FileOutputStream fos = new FileOutputStream(mark, false)) {
                    String content = "Upload diff image for " + outfitId + " / " + emotion + "\n";
                    fos.write(content.getBytes());
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static boolean saveDiffImage(@NonNull Context context,
                                        @NonNull String outfitId,
                                        @NonNull String emotion,
                                        @NonNull Uri sourceUri) {
        File targetDir = getEmotionDir(context, outfitId, emotion);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return false;
        }
        String fileName = String.format(Locale.ROOT, "%s__exp_%s__v001.png", outfitId, emotion);
        File target = new File(targetDir, fileName);

        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             FileOutputStream out = new FileOutputStream(target, false)) {
            if (in == null) {
                return false;
            }
            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Nullable
    public static String resolveDiffSource(@NonNull Context context,
                                           @Nullable String outfitId,
                                           @NonNull String emotion) {
        if (TextUtils.isEmpty(outfitId)) {
            return null;
        }
        String normalizedEmotion = TextUtils.isEmpty(emotion) ? "normal" : emotion;
        File local = new File(getEmotionDir(context, outfitId, normalizedEmotion),
                String.format(Locale.ROOT, "%s__exp_%s__v001.png", outfitId, normalizedEmotion));
        if (local.exists()) {
            return local.getAbsolutePath();
        }
        return AvatarExpressionResolver.buildDiffAssetSource(outfitId, normalizedEmotion);
    }

    @NonNull
    private static File getEmotionDir(@NonNull Context context,
                                      @NonNull String outfitId,
                                      @NonNull String emotion) {
        return new File(new File(new File(context.getFilesDir(), ROOT_DIR), outfitId), emotion);
    }
}

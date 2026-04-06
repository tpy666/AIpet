package com.example.aipet.ui.avatar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

/**
 * 角色头像上传入口协议。
 *
 * 预留本地图片与网络图片两类来源，后续可替换为真实上传/下载实现。
 */
public interface AvatarUploadPort {

    void requestLocalImage(@NonNull ActivityResultLauncher<String> pickerLauncher);

    void requestRemoteImage(@NonNull String imageUrl);
}

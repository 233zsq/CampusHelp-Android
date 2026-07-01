package com.campus.help.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.campus.help.core.base.BaseFragment;
import com.campus.help.core.base.Callback;
import com.campus.help.core.utils.PermissionUtils;
import com.campus.help.data.model.User;
import com.campus.help.data.repo.UserManager;
import com.campus.help.databinding.FragmentMineBinding;
import com.campus.help.ui.login.LoginActivity;

import java.io.File;

/**
 * 我的 / 个人中心（成员 D）。
 * 头像 / 昵称 / 学号 / 信用分全部走网络（GET /api/users/{id}）；
 * 头像支持相册 / 拍照上传（POST /api/upload → PUT /api/users/{id} 回写）；
 * 信用明细入口；退出登录走后端 logout + 清本地 + 停 WebSocketService。
 */
public class MineFragment extends BaseFragment<FragmentMineBinding> {

    private UserManager userManager;

    /** 拍照时写入的 Uri（须在 cameraLauncher 之前声明，避免前向引用） */
    @Nullable
    private Uri pendingCameraUri;

    /** 相册选图 */
    private final ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::onGalleryPicked);

    /** 拍照（结果 Uri 由 launch 时传入，拍照成功后写到该 Uri） */
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && pendingCameraUri != null) {
                    uploadAvatar(pendingCameraUri);
                }
            });

    /** 相机权限请求 */
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                } else {
                    toast("需要相机权限才能拍照");
                }
            });

    @Override
    protected FragmentMineBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentMineBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        userManager = UserManager.get();

        // 头像点击 → 选择相册 / 拍照
        binding.avatar.setOnClickListener(v -> showAvatarPicker());

        // 入口
        binding.entryPublished.setOnClickListener(v ->
                toast("待 B 对接 publisherId 便捷接口"));
        binding.entryAccepted.setOnClickListener(v ->
                toast("待 B 对接 takerId 便捷接口"));
        binding.entryCredit.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), CreditDetailActivity.class);
            startActivity(it);
        });

        // 退出登录
        binding.btnLogout.setOnClickListener(v -> {
            binding.btnLogout.setEnabled(false);
            userManager.logout(requireContext(), () -> {
                startActivity(new Intent(getContext(), LoginActivity.class));
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        });
    }

    @Override
    protected void initData() {
        long uid = userManager.getCurrentUserId(requireContext());
        if (uid <= 0) {
            return;
        }
        userManager.getUserInfo(uid).observe(getViewLifecycleOwner(), this::bindUser);
    }

    private void bindUser(@Nullable User user) {
        if (user == null) {
            toast("用户信息加载失败");
            return;
        }
        binding.tvName.setText(user.name == null || user.name.isEmpty() ? "未设置" : user.name);
        binding.tvStudentId.setText(user.studentId == null ? "—" : user.studentId);
        binding.creditGauge.setScore(user.creditScore);
        if (user.avatar != null && !user.avatar.isEmpty()) {
            Glide.with(this).load(user.avatar).circleCrop().into(binding.avatar);
        } else {
            Glide.with(this).clear(binding.avatar);
            binding.avatar.setImageResource(0);
        }
    }

    // ===== 头像上传 =====

    private void showAvatarPicker() {
        String[] options = {"从相册选择", "拍照"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("更换头像")
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        launchGallery();
                    } else {
                        ensureCameraPermissionThenLaunch();
                    }
                })
                .show();
    }

    private void launchGallery() {
        galleryLauncher.launch(
                new androidx.activity.result.PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
    }

    private void onGalleryPicked(@Nullable Uri uri) {
        if (uri != null) {
            uploadAvatar(uri);
        }
    }

    private void ensureCameraPermissionThenLaunch() {
        if (PermissionUtils.hasCamera(requireContext())) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        Uri uri = createCameraUri();
        if (uri == null) {
            toast("无法创建图片文件");
            return;
        }
        pendingCameraUri = uri;
        cameraLauncher.launch(uri);
    }

    private Uri createCameraUri() {
        File dir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "avatar");
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        File file = new File(dir, "avatar_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + ".fileprovider", file);
    }

    private void uploadAvatar(Uri uri) {
        // 先预览
        Glide.with(this).load(uri).circleCrop().into(binding.avatar);
        toast("上传中…");
        userManager.uploadAvatar(requireContext(), uri, new Callback<User>() {
            @Override
            public void onResult(User result) {
                if (result != null) {
                    bindUser(result);
                    toast("头像更新成功");
                } else {
                    toast("头像上传失败");
                }
            }
        });
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}

package edu.travels.travelapp.ui.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import com.google.android.material.textfield.TextInputEditText;

import edu.travels.travelapp.R;

public class EditProfileActivity extends AppCompatActivity {

    private CircleImageView ivAvatar;
    private TextInputEditText etNickname, etPassword;
    private Button btnSave;
    private Uri avatarUri; // 保存选择的头像路径

    // 相册选择器
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    avatarUri = result.getData().getData();
                    ivAvatar.setImageURI(avatarUri);
                }
            });

    // 拍照
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && avatarUri != null) {
                    ivAvatar.setImageURI(avatarUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 显示返回箭头
        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);

        ivAvatar = findViewById(R.id.iv_avatar);
        etNickname = findViewById(R.id.et_nickname);
        etPassword = findViewById(R.id.et_password);
        btnSave = findViewById(R.id.btn_save);

        // 点击头像选择
        ivAvatar.setOnClickListener(v -> showImagePickDialog());

        // 保存按钮
        btnSave.setOnClickListener(v -> {
            String nick = etNickname.getText().toString().trim();
            if (nick.isEmpty()) {
                Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 保存到本地（头像用 Uri 转 Base64 或上传，这里先简单保存路径）
            getSharedPreferences("travel_prefs", MODE_PRIVATE).edit()
                    .putString("nickname", nick)
                    .putString("avatar_uri", avatarUri != null ? avatarUri.toString() : "")
                    .apply();

            Toast.makeText(this, "资料修改成功！", Toast.LENGTH_LONG).show();
            finish();
        });

        // 读取上次保存的头像
        String savedUri = getSharedPreferences("travel_prefs", MODE_PRIVATE)
                .getString("avatar_uri", "");
        if (!savedUri.isEmpty()) {
            ivAvatar.setImageURI(Uri.parse(savedUri));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // 点击返回箭头就关闭页面
        return true;
    }

    private void showImagePickDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("选择头像")
                .setItems(new String[]{"拍照", "从相册选择"}, (dialog, which) -> {
                    if (which == 0) {
                        // 拍照
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        avatarUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, avatarUri);
                        cameraLauncher.launch(intent);
                    } else {
                        // 相册
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryLauncher.launch(intent);
                    }
                })
                .show();
    }
}
package edu.travels.travelapp.ui.publish;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import edu.travels.travelapp.R;

public class PublishFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.publish, container, false);

        TextInputEditText etTitle = view.findViewById(R.id.et_title);
        TextInputEditText etLocation = view.findViewById(R.id.et_location);
        Button btnSubmit = view.findViewById(R.id.btn_submit_post);

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "请输入标题", Toast.LENGTH_SHORT).show();
                return;
            }
            if (location.isEmpty()) {
                Toast.makeText(getContext(), "请输入地点", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: 真正调用后端发布接口
            Toast.makeText(getContext(), "发布成功！\n标题：" + title + "\n地点：" + location, Toast.LENGTH_LONG).show();
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // 方案1：用系统自带动画（最稳！永不出错！）
            getDialog().getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

            // 方案2：你想要更丝滑的从底部弹出？用下面这行（推荐！）
            getDialog().getWindow().getAttributes().windowAnimations = R.style.BottomSlideAnimation;

            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
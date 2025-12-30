package edu.travels.travelapp.ui.user.myposts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.travels.travelapp.R;
import edu.travels.travelapp.model.dto.ImageItemDTO;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.ViewHolder> {
    private List<ImageItemDTO> imageList;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onEditClick(ImageItemDTO imageItem);
        void onDeleteClick(ImageItemDTO imageItem);
    }
    
    public MyPostsAdapter(List<ImageItemDTO> imageList, OnItemClickListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_post, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= 0 && position < imageList.size()) {
            ImageItemDTO imageItem = imageList.get(position);
            holder.bind(imageItem);
        }
    }
    
    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvLocation;
        private TextView tvCreateTime;
        private TextView btnEdit;
        private TextView btnDelete;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
        
        public void bind(ImageItemDTO imageItem) {
            // 加载图片
            if (imageItem.getImageUrl() != null && !imageItem.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageItem.getImageUrl())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.default_avatar);
            }
            
            // 解析标题和描述
            String[] titleAndDesc = parseTitleAndDescription(imageItem.getDescription());
            String title = titleAndDesc[0];
            String description = titleAndDesc[1];
            
            // 设置标题
            if (title != null && !title.trim().isEmpty()) {
                tvTitle.setText(title);
                tvTitle.setVisibility(View.VISIBLE);
            } else {
                tvTitle.setVisibility(View.GONE);
            }
            
            // 设置描述
            if (description != null && !description.trim().isEmpty()) {
                tvDescription.setText(description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            
            // 设置位置
            if (imageItem.getLocationName() != null && !imageItem.getLocationName().trim().isEmpty()) {
                tvLocation.setText(imageItem.getLocationName());
                tvLocation.setVisibility(View.VISIBLE);
            } else {
                tvLocation.setVisibility(View.GONE);
            }
            
            // 设置时间
            if (imageItem.getCreateTime() != null) {
                tvCreateTime.setText(formatTime(imageItem.getCreateTime()));
            } else {
                tvCreateTime.setText("");
            }
            
            // 编辑按钮
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(imageItem);
                }
            });
            
            // 删除按钮
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(imageItem);
                }
            });
        }
        
        private String formatTime(Date date) {
            long timeDiff = System.currentTimeMillis() - date.getTime();
            
            if (timeDiff < 60 * 1000) {
                return "刚刚";
            } else if (timeDiff < 60 * 60 * 1000) {
                return (timeDiff / (60 * 1000)) + "分钟前";
            } else if (timeDiff < 24 * 60 * 60 * 1000) {
                return (timeDiff / (60 * 60 * 1000)) + "小时前";
            } else {
                return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(date);
            }
        }
        
        /**
         * 从 description 字段中解析出标题和描述
         * 格式：标题. 描述 或 标题
         */
        private String[] parseTitleAndDescription(String description) {
            String[] result = new String[]{"", ""};
            if (description == null || description.trim().isEmpty()) {
                return result;
            }
            
            // 查找第一个 ". " 分隔符
            int dotIndex = description.indexOf(". ");
            if (dotIndex > 0) {
                result[0] = description.substring(0, dotIndex).trim();
                result[1] = description.substring(dotIndex + 2).trim();
            } else {
                // 没有分隔符，整个作为标题
                result[0] = description.trim();
            }
            
            return result;
        }
    }
}


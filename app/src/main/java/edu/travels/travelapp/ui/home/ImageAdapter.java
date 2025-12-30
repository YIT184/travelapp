package edu.travels.travelapp.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.travels.travelapp.R;
import edu.travels.travelapp.model.dto.ImageItemDTO;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<ImageItemDTO> imageList;
    private OnImageInteractionListener listener;

    public ImageAdapter(List<ImageItemDTO> imageList, OnImageInteractionListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (position >= 0 && position < imageList.size()) {
            ImageItemDTO imageItem = imageList.get(position);
            holder.bind(imageItem);
        } else {
            android.util.Log.e("ImageAdapter", "位置越界: position=" + position + ", listSize=" + imageList.size());
        }
    }
    
    @Override
    public void onViewRecycled(@NonNull ImageViewHolder holder) {
        super.onViewRecycled(holder);
        // 强制清理 Glide 请求和ImageView，防止视图复用时显示错误的图片
        if (holder.ivImage != null) {
            Glide.with(holder.itemView.getContext()).clear(holder.ivImage);
            holder.ivImage.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void updateData(List<ImageItemDTO> newImageList) {
        this.imageList = newImageList;
        notifyDataSetChanged();
    }

    public void addData(List<ImageItemDTO> moreImageList) {
        int startPosition = imageList.size();
        imageList.addAll(moreImageList);
        notifyItemRangeInserted(startPosition, moreImageList.size());
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivUserAvatar;
        private TextView tvUsername;
        private TextView tvCreateTime;
        private TextView tvLocation;
        private ImageView ivImage;
        private TextView tvDescription;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
            tvLocation = itemView.findViewById(R.id.tv_location);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }

        public void bind(ImageItemDTO imageItem) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                android.util.Log.w("ImageAdapter", "绑定时位置无效");
                return;
            }
            
            // 调试日志：打印用户信息
            String userId = imageItem.getUserId() != null ? imageItem.getUserId() : "null";
            String uploaderNickname = imageItem.getUploaderNickname() != null ? imageItem.getUploaderNickname() : "null";
            String nickname = imageItem.getNickname() != null ? imageItem.getNickname() : "null";
            String uploaderAvatarUrl = imageItem.getUploaderAvatarUrl() != null ? imageItem.getUploaderAvatarUrl() : "null";
            String userAvatarUrl = imageItem.getUserAvatarUrl() != null ? imageItem.getUserAvatarUrl() : "null";
            android.util.Log.d("ImageAdapter", String.format("位置[%d] 用户信息: userId=%s, uploaderNickname=%s, nickname=%s, uploaderAvatarUrl=%s, userAvatarUrl=%s", 
                position, userId, uploaderNickname, nickname, uploaderAvatarUrl, userAvatarUrl));
            
            // 设置用户信息 - 显示用户昵称或用户ID
            String displayName = getDisplayName(imageItem, itemView.getContext());
            tvUsername.setText(displayName);
            android.util.Log.d("ImageAdapter", String.format("位置[%d] 最终显示名称: %s", position, displayName));
            
            // 设置用户头像 - 如果是当前用户发布的，显示当前用户的头像
            setUserAvatar(imageItem, itemView.getContext());
            
            // 设置时间
            if (imageItem.getCreateTime() != null) {
                tvCreateTime.setText(formatTime(imageItem.getCreateTime()));
            }
            
            // 设置位置
            if (imageItem.getLocationName() != null && !imageItem.getLocationName().isEmpty()) {
                tvLocation.setText(imageItem.getLocationName());
                tvLocation.setVisibility(View.VISIBLE);
            } else {
                tvLocation.setVisibility(View.GONE);
            }
            
            // 设置图片 - 强制清除缓存并重新加载
            if (imageItem.getImageUrl() != null && !imageItem.getImageUrl().isEmpty()) {
                String imageUrl = imageItem.getImageUrl();
                String imageId = imageItem.getImageId() != null ? imageItem.getImageId() : "unknown_" + position;
                android.util.Log.d("ImageAdapter", String.format("绑定位置 %d: imageId=%s, imageUrl=%s, hashCode=%d", 
                    position, imageId, imageUrl, imageUrl.hashCode()));
                
                // 先清除ImageView的所有内容
                ivImage.setImageDrawable(null);
                ivImage.setTag(null); // 清除tag，防止异步加载时显示错误的图片
                
                // 取消之前的Glide请求
                Glide.with(itemView.getContext()).clear(ivImage);
                
                // 设置tag为当前imageId，用于验证加载完成后是否还是这个图片
                ivImage.setTag(imageId);
                
                // 使用Glide加载图片
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .signature(new com.bumptech.glide.signature.ObjectKey(imageId)) // 使用imageId作为签名，确保每个图片唯一
                        .skipMemoryCache(false) // 允许内存缓存，但使用签名区分不同图片
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL) // 使用磁盘缓存
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, 
                                    com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                android.util.Log.e("ImageAdapter", "图片加载失败: " + imageUrl + ", 错误: " + (e != null ? e.getMessage() : "未知"));
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, 
                                    com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                    com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                // 验证加载的图片是否还是当前应该显示的图片
                                String currentTag = (String) ivImage.getTag();
                                if (currentTag != null && currentTag.equals(imageId)) {
                                    android.util.Log.d("ImageAdapter", "图片加载成功: " + imageUrl + " (位置: " + position + ")");
                                } else {
                                    android.util.Log.w("ImageAdapter", "图片加载完成但已过期: " + imageUrl + " (期望: " + imageId + ", 实际: " + currentTag + ")");
                                }
                                return false;
                            }
                        })
                        .into(ivImage);
            } else {
                android.util.Log.w("ImageAdapter", "位置 " + position + " 图片URL为空，imageId=" + imageItem.getImageId());
                ivImage.setImageDrawable(null);
                ivImage.setTag(null);
                Glide.with(itemView.getContext()).clear(ivImage);
                ivImage.setImageResource(R.drawable.default_avatar);
            }
            
            // 设置描述
            if (imageItem.getDescription() != null && !imageItem.getDescription().isEmpty()) {
                tvDescription.setText(imageItem.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // 长按回调（用于管理页编辑/删除）
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onImageLongClick(imageItem);
                    return true;
                }
                return false;
            });
        }

        private String formatTime(Date date) {
            // 后端时间少了8小时，这里做一次 +8 小时校正（适配东八区）
            long correctedTime = date.getTime() + 8 * 60 * 60 * 1000L;
            long timeDiff = System.currentTimeMillis() - correctedTime;
            
            if (timeDiff < 60 * 1000) {
                return "刚刚";
            } else if (timeDiff < 60 * 60 * 1000) {
                return (timeDiff / (60 * 1000)) + "分钟前";
            } else if (timeDiff < 24 * 60 * 60 * 1000) {
                return (timeDiff / (60 * 60 * 1000)) + "小时前";
            } else {
                return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                        .format(new Date(correctedTime));
            }
        }
        
        /**
         * 获取显示名称：使用图片上传者的信息
         */
        private String getDisplayName(ImageItemDTO imageItem, Context context) {
            // 优先使用 uploaderNickname（上传者昵称）
            if (imageItem.getUploaderNickname() != null && !imageItem.getUploaderNickname().trim().isEmpty()) {
                return imageItem.getUploaderNickname();
            }
            
            // 如果没有 uploaderNickname，使用 nickname（兼容旧字段）
            if (imageItem.getNickname() != null && !imageItem.getNickname().trim().isEmpty()) {
                return imageItem.getNickname();
            }
            
            // 如果都没有昵称，使用用户ID
            String userId = imageItem.getUserId();
            if (userId != null && !userId.isEmpty()) {
                return "用户" + userId;
            }
            
            return "匿名用户";
        }
        
        /**
         * 设置用户头像：显示图片上传者的头像
         */
        private void setUserAvatar(ImageItemDTO imageItem, Context context) {
            // 优先使用 uploaderAvatarUrl（上传者头像URL）
            String avatarUrl = imageItem.getUploaderAvatarUrl();
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(ivUserAvatar);
                return;
            }
            
            // 如果没有 uploaderAvatarUrl，使用 userAvatarUrl（兼容旧字段）
            avatarUrl = imageItem.getUserAvatarUrl();
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(ivUserAvatar);
                return;
            }
            
            // 如果都没有头像URL，显示默认头像
            Glide.with(context)
                    .load(R.drawable.default_avatar)
                    .circleCrop()
                    .into(ivUserAvatar);
        }
    }

    public interface OnImageInteractionListener {
        // 默认空实现，避免老代码强制实现
        default void onImageLongClick(ImageItemDTO imageItem) {}
    }
}
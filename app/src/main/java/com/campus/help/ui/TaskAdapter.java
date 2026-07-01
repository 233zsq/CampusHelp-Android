package com.campus.help.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.campus.help.R;
import com.campus.help.core.base.BaseAdapter;
import com.campus.help.core.utils.TimeUtils;
import com.campus.help.data.model.Task;
import com.campus.help.databinding.ItemTaskBinding;

/**
 * 任务列表适配器。根据 status 和 deadline 展示状态标签。
 */
public class TaskAdapter extends BaseAdapter<Task, ItemTaskBinding> {

    private static final String[] TYPE_LABEL = {"跑腿", "拼单", "二手"};

    /** 状态常量，与 Task.status 对应 */
    private static final int STATUS_PENDING   = 0;
    private static final int STATUS_ACCEPTED  = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_CANCELLED = 3;

    @Override
    protected ItemTaskBinding createBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemTaskBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void bind(@NonNull ItemTaskBinding b, @NonNull Task task, int position) {
        b.taskTitle.setText(task.title);
        b.taskType.setText(task.type >= 0 && task.type < TYPE_LABEL.length
                ? TYPE_LABEL[task.type] : "其他");
        b.taskReward.setText(String.format("¥%.1f", task.reward));
        b.taskLocation.setText(task.location);
        b.taskDeadline.setText("截止 " + TimeUtils.formatTime(task.deadline));

        // 状态标签
        setStatusBadge(b, task);
    }

    private void setStatusBadge(@NonNull ItemTaskBinding b, @NonNull Task task) {
        String statusText;
        @ColorRes int textColor;
        @ColorRes int bgColor;

        if (task.status == STATUS_PENDING && task.deadline < System.currentTimeMillis()) {
            // 待接单但已过截止时间 → 超时
            statusText = "超时";
            textColor = R.color.white;
            bgColor = R.color.status_overtime;
        } else {
            switch (task.status) {
                case STATUS_PENDING:
                    statusText = "待接单";
                    textColor = R.color.white;
                    bgColor = R.color.status_pending;
                    break;
                case STATUS_ACCEPTED:
                    statusText = "已接单";
                    textColor = R.color.white;
                    bgColor = R.color.status_accepted;
                    break;
                case STATUS_COMPLETED:
                    statusText = "已完成";
                    textColor = R.color.white;
                    bgColor = R.color.status_completed;
                    break;
                case STATUS_CANCELLED:
                    statusText = "已取消";
                    textColor = R.color.white;
                    bgColor = R.color.status_cancelled;
                    break;
                default:
                    statusText = "未知";
                    textColor = R.color.white;
                    bgColor = R.color.text_secondary;
                    break;
            }
        }

        b.taskStatus.setText(statusText);
        b.taskStatus.setTextColor(ContextCompat.getColor(b.getRoot().getContext(), textColor));
        b.taskStatus.setBackgroundColor(ContextCompat.getColor(b.getRoot().getContext(), bgColor));
    }
}

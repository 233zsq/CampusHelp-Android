package com.campus.help.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.campus.help.core.base.BaseAdapter;
import com.campus.help.core.utils.TimeUtils;
import com.campus.help.data.model.Task;
import com.campus.help.databinding.ItemTaskBinding;

/**
 * 任务列表适配器（成员 B 可扩展为多类型 / 筛选排序）。
 */
public class TaskAdapter extends BaseAdapter<Task, ItemTaskBinding> {

    private static final String[] TYPE_LABEL = {"跑腿", "拼单", "二手"};

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
    }
}

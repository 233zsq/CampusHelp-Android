package com.campus.help.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.campus.help.R;
import com.campus.help.core.utils.TimeUtils;
import com.campus.help.data.model.Task;
import com.campus.help.databinding.ActivityTaskDetailBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 任务详情页。展示任务全部信息，提供接单按钮（暂不提交后端）。
 */
public class TaskDetailActivity extends AppCompatActivity {

    private ActivityTaskDetailBinding binding;
    private Task task;

    private static final String[] TYPE_LABEL = {"跑腿", "拼单", "二手"};
    private static final int STATUS_PENDING   = 0;
    private static final int STATUS_ACCEPTED  = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_CANCELLED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        task = (Task) getIntent().getSerializableExtra("task");
        if (task == null) {
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());

        // 标题
        binding.detailTitle.setText(task.title);

        // 报酬
        binding.detailReward.setText(task.reward > 0
                ? String.format("¥%.2f", task.reward) : "面议");

        // 类型标签
        String typeText = task.type >= 0 && task.type < TYPE_LABEL.length
                ? TYPE_LABEL[task.type] : "其他";
        binding.detailType.setText(typeText);

        // 状态标签
        setStatusBadge();

        // 任务要求
        binding.detailContent.setText(task.content != null && !task.content.isEmpty()
                ? task.content : "暂无详细描述");

        // 地址
        binding.detailLocation.setText(task.location != null && !task.location.isEmpty()
                ? task.location : "未填写");

        // 截止时间
        binding.detailDeadline.setText("截止 " + TimeUtils.formatTime(task.deadline));

        // 接单按钮 — 仅待接单且未超时可用
        boolean canAccept = task.status == STATUS_PENDING
                && task.deadline > System.currentTimeMillis();

        if (canAccept) {
            binding.btnAccept.setEnabled(true);
            binding.btnAccept.setText("接 单");
            binding.btnAccept.setOnClickListener(v -> onAcceptClick());
        } else {
            binding.btnAccept.setEnabled(false);
            String btnText;
            if (task.status != STATUS_PENDING) {
                btnText = getStatusText();
            } else {
                btnText = "已超时";
            }
            binding.btnAccept.setText(btnText);
        }
    }

    private void setStatusBadge() {
        String statusText;
        @ColorRes int textColor;
        @ColorRes int bgColor;

        if (task.status == STATUS_PENDING && task.deadline < System.currentTimeMillis()) {
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
        binding.detailStatus.setText(statusText);
        binding.detailStatus.setTextColor(ContextCompat.getColor(this, textColor));
        binding.detailStatus.setBackgroundColor(ContextCompat.getColor(this, bgColor));
    }

    private void onAcceptClick() {
        String info = "【测试】接单确认\n\n"
                + "任务：" + task.title + "\n"
                + "类型：" + TYPE_LABEL[task.type] + "\n"
                + "报酬：¥" + (task.reward > 0 ? String.format("%.2f", task.reward) : "面议") + "\n"
                + "地址：" + (task.location != null ? task.location : "") + "\n\n"
                + "（暂未提交到后端）";

        new MaterialAlertDialogBuilder(this)
                .setTitle("确认接单")
                .setMessage(info)
                .setPositiveButton("确认接单", (d, w) -> {
                    // TODO: 提交接单到后端
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String getStatusText() {
        if (task.status == STATUS_PENDING && task.deadline < System.currentTimeMillis()) {
            return "超时";
        }
        switch (task.status) {
            case STATUS_PENDING:   return "待接单";
            case STATUS_ACCEPTED:  return "已接单";
            case STATUS_COMPLETED: return "已完成";
            case STATUS_CANCELLED: return "已取消";
            default:               return "未知";
        }
    }
}

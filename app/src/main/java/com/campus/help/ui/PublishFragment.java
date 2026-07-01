package com.campus.help.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campus.help.R;
import com.campus.help.core.base.BaseFragment;
import com.campus.help.core.base.Callback;
import com.campus.help.data.model.Task;
import com.campus.help.data.repo.TaskRepository;
import com.campus.help.databinding.FragmentPublishBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 发布任务界面。
 * 包含：任务类型 / 标题 / 要求 / 报酬 / 地址 / 截止时间。
 * 预览：弹窗展示填写内容；发布：提交至后端。
 */
public class PublishFragment extends BaseFragment<FragmentPublishBinding> {

    /** 当前选中的任务类型：0 跑腿 1 拼单 2 二手 */
    private int selectedType = -1;

    private final Calendar deadlineCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private TaskRepository taskRepository;

    @Override
    protected FragmentPublishBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentPublishBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        taskRepository = new TaskRepository();

        // 任务类型选择
        setupTypeSelector();

        // 截止时间点击弹出选择器
        binding.etDeadline.setOnClickListener(v -> showDatePicker());
        binding.deadlineLayout.setEndIconOnClickListener(v -> showDatePicker());

        // 预览按钮
        binding.btnPreview.setOnClickListener(v -> onPreviewClick());

        // 发布按钮
        binding.btnPublish.setOnClickListener(v -> onPublishClick());
    }

    // ==================== 任务类型选择 ====================

    private void setupTypeSelector() {
        View.OnClickListener listener = v -> selectType(v.getId());

        binding.typeExpress.setOnClickListener(listener);
        binding.typeGroupBuy.setOnClickListener(listener);
        binding.typeSecondhand.setOnClickListener(listener);
    }

    private void selectType(int viewId) {
        binding.typeExpress.setSelected(false);
        binding.typeGroupBuy.setSelected(false);
        binding.typeSecondhand.setSelected(false);

        if (viewId == R.id.type_express) {
            binding.typeExpress.setSelected(true);
            selectedType = 0;
        } else if (viewId == R.id.type_group_buy) {
            binding.typeGroupBuy.setSelected(true);
            selectedType = 1;
        } else if (viewId == R.id.type_secondhand) {
            binding.typeSecondhand.setSelected(true);
            selectedType = 2;
        }
    }

    // ==================== 截止时间选择 ====================

    private void showDatePicker() {
        int year = deadlineCalendar.get(Calendar.YEAR);
        int month = deadlineCalendar.get(Calendar.MONTH);
        int day = deadlineCalendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(),
                (view, y, m, d) -> {
                    deadlineCalendar.set(Calendar.YEAR, y);
                    deadlineCalendar.set(Calendar.MONTH, m);
                    deadlineCalendar.set(Calendar.DAY_OF_MONTH, d);
                    showTimePicker();
                }, year, month, day).show();
    }

    private void showTimePicker() {
        int hour = deadlineCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = deadlineCalendar.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(),
                (view, h, min) -> {
                    deadlineCalendar.set(Calendar.HOUR_OF_DAY, h);
                    deadlineCalendar.set(Calendar.MINUTE, min);
                    deadlineCalendar.set(Calendar.SECOND, 0);
                    binding.etDeadline.setText(dateFormatter.format(deadlineCalendar.getTime()));
                }, hour, minute, true).show();
    }

    // ==================== 表单校验 ====================

    /**
     * 校验表单并组装 Task 对象，失败弹错误提示并返回 null。
     */
    @Nullable
    private Task validateAndBuildTask() {
        if (selectedType == -1) {
            showErrorDialog(getString(R.string.publish_please_select_type));
            return null;
        }

        String title = binding.etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            binding.titleLayout.setError(getString(R.string.publish_please_enter_title));
            return null;
        }
        binding.titleLayout.setError(null);

        String content = binding.etContent.getText().toString().trim();
        if (content.isEmpty()) {
            binding.contentLayout.setError(getString(R.string.publish_please_enter_content));
            return null;
        }
        binding.contentLayout.setError(null);

        String rewardStr = binding.etReward.getText().toString().trim();
        double reward = 0.0;
        if (!rewardStr.isEmpty()) {
            try {
                reward = Double.parseDouble(rewardStr);
            } catch (NumberFormatException e) {
                binding.rewardLayout.setError("请输入正确的金额");
                return null;
            }
        }
        binding.rewardLayout.setError(null);

        String location = binding.etLocation.getText().toString().trim();
        if (location.isEmpty()) {
            binding.locationLayout.setError(getString(R.string.publish_please_enter_location));
            return null;
        }
        binding.locationLayout.setError(null);

        String deadlineStr = binding.etDeadline.getText().toString().trim();
        if (deadlineStr.isEmpty()) {
            showErrorDialog(getString(R.string.publish_please_select_deadline));
            return null;
        }

        long deadline = deadlineCalendar.getTimeInMillis();
        if (deadline <= System.currentTimeMillis()) {
            showErrorDialog(getString(R.string.publish_deadline_must_be_future));
            return null;
        }

        Task task = new Task();
        task.type = selectedType;
        task.title = title;
        task.content = content;
        task.reward = reward;
        task.location = location;
        task.deadline = deadline;
        return task;
    }

    // ==================== 预览 ====================

    private void onPreviewClick() {
        Task task = validateAndBuildTask();
        if (task == null) return;

        String[] typeNames = {"跑腿", "拼单", "二手"};
        String info = "【预览】发布信息\n\n"
                + "类型：" + typeNames[task.type] + "\n"
                + "标题：" + task.title + "\n"
                + "要求：" + task.content + "\n"
                + "报酬：¥" + (task.reward > 0 ? String.format("%.2f", task.reward) : "面议") + "\n"
                + "地址：" + task.location + "\n"
                + "截止：" + binding.etDeadline.getText().toString().trim() + "\n\n"
                + "（预览模式，未提交）";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("任务预览")
                .setMessage(info)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    // ==================== 发布到后端 ====================

    private void onPublishClick() {
        Task task = validateAndBuildTask();
        if (task == null) return;

        binding.btnPublish.setEnabled(false);
        binding.btnPublish.setText("发布中…");

        taskRepository.insert(task, new Callback<Long>() {
            @Override
            public void onResult(Long taskId) {
                if (getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    binding.btnPublish.setEnabled(true);
                    binding.btnPublish.setText(R.string.publish_button);

                    if (taskId != null) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("发布成功")
                                .setMessage("任务已发布，ID：" + taskId)
                                .setPositiveButton(R.string.confirm, (d, w) -> clearForm())
                                .show();
                    } else {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("发布失败")
                                .setMessage("网络异常或未登录，请重试")
                                .setPositiveButton(R.string.confirm, null)
                                .show();
                    }
                });
            }
        });
    }

    /** 清空表单 */
    private void clearForm() {
        binding.typeExpress.setSelected(false);
        binding.typeGroupBuy.setSelected(false);
        binding.typeSecondhand.setSelected(false);
        selectedType = -1;
        binding.etTitle.setText("");
        binding.etContent.setText("");
        binding.etReward.setText("");
        binding.etLocation.setText("");
        binding.etDeadline.setText("");
    }

    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }
}

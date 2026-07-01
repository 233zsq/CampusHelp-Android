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
import com.campus.help.databinding.FragmentPublishBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 发布任务界面。
 * 包含：任务类型 / 标题 / 要求 / 报酬 / 地址 / 截止时间。
 * 点击发布后展示测试信息，暂不提交后端。
 */
public class PublishFragment extends BaseFragment<FragmentPublishBinding> {

    /** 当前选中的任务类型：0 跑腿 1 拼单 2 二手 */
    private int selectedType = -1;

    private final Calendar deadlineCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected FragmentPublishBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentPublishBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void initView() {
        // 任务类型选择
        setupTypeSelector();

        // 截止时间点击弹出选择器
        binding.etDeadline.setOnClickListener(v -> showDatePicker());
        binding.deadlineLayout.setEndIconOnClickListener(v -> showDatePicker());

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
        // 重置所有
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

    // ==================== 发布 ====================

    private void onPublishClick() {
        // 校验
        if (selectedType == -1) {
            showErrorDialog(getString(R.string.publish_please_select_type));
            return;
        }

        String title = binding.etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            binding.titleLayout.setError(getString(R.string.publish_please_enter_title));
            return;
        }
        binding.titleLayout.setError(null);

        String content = binding.etContent.getText().toString().trim();
        if (content.isEmpty()) {
            binding.contentLayout.setError(getString(R.string.publish_please_enter_content));
            return;
        }
        binding.contentLayout.setError(null);

        String rewardStr = binding.etReward.getText().toString().trim();
        double reward = 0.0;
        if (!rewardStr.isEmpty()) {
            try {
                reward = Double.parseDouble(rewardStr);
            } catch (NumberFormatException e) {
                binding.rewardLayout.setError("请输入正确的金额");
                return;
            }
        }

        String location = binding.etLocation.getText().toString().trim();
        if (location.isEmpty()) {
            binding.locationLayout.setError(getString(R.string.publish_please_enter_location));
            return;
        }
        binding.locationLayout.setError(null);

        // 清除报酬错误
        binding.rewardLayout.setError(null);

        String deadlineStr = binding.etDeadline.getText().toString().trim();
        if (deadlineStr.isEmpty()) {
            showErrorDialog(getString(R.string.publish_please_select_deadline));
            return;
        }

        long deadline = deadlineCalendar.getTimeInMillis();
        if (deadline <= System.currentTimeMillis()) {
            showErrorDialog(getString(R.string.publish_deadline_must_be_future));
            return;
        }

        // 构建测试信息
        String[] typeNames = {"跑腿", "拼单", "二手"};
        String info = "【测试】发布信息预览\n\n"
                + "类型：" + typeNames[selectedType] + "\n"
                + "标题：" + title + "\n"
                + "要求：" + content + "\n"
                + "报酬：¥" + (reward > 0 ? String.format("%.2f", reward) : "面议") + "\n"
                + "地址：" + location + "\n"
                + "截止：" + deadlineStr + "\n\n"
                + "（暂未提交到后端）";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.publish_success_title)
                .setMessage(info)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }
}

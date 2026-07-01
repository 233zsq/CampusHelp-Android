package com.campus.help.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.campus.help.R;
import com.campus.help.core.base.BaseAdapter;
import com.campus.help.core.utils.TimeUtils;
import com.campus.help.data.model.CreditRecord;
import com.campus.help.databinding.ItemCreditRecordBinding;

/**
 * 信用分明细适配器。delta>0 绿色显示 +N，delta<0 红色显示 N。
 */
public class CreditRecordAdapter extends BaseAdapter<CreditRecord, ItemCreditRecordBinding> {

    @Override
    protected ItemCreditRecordBinding createBinding(@NonNull LayoutInflater inflater,
                                                    @NonNull ViewGroup parent) {
        return ItemCreditRecordBinding.inflate(inflater, parent, false);
    }

    @Override
    protected void bind(@NonNull ItemCreditRecordBinding b, @NonNull CreditRecord item, int position) {
        String text = (item.delta >= 0 ? "+" : "") + item.delta;
        b.tvDelta.setText(text);

        @ColorRes int color = item.delta >= 0 ? R.color.credit_high : R.color.credit_low;
        b.tvDelta.setTextColor(ContextCompat.getColor(b.getRoot().getContext(), color));

        b.tvReason.setText(item.reason == null || item.reason.isEmpty() ? "—" : item.reason);
        b.tvTime.setText(item.timestamp > 0 ? TimeUtils.formatTime(item.timestamp) : "—");
    }
}

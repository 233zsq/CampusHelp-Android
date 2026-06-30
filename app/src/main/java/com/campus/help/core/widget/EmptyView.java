package com.campus.help.core.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.campus.help.databinding.ViewEmptyBinding;

/**
 * 空状态占位视图。列表无数据时展示。
 * 用法：在布局中直接 <com.campus.help.core.widget.EmptyView .../>，或代码 addView。
 */
public class EmptyView extends LinearLayout {

    private ViewEmptyBinding binding;

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        binding = ViewEmptyBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setText(CharSequence text) {
        binding.emptyText.setText(text);
    }
}

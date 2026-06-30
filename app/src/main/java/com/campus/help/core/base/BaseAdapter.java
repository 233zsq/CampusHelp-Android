package com.campus.help.core.base;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView 通用基类（ViewBinding + 单类型列表）。
 * 成员 B 如需多类型布局，可另建 MultiTypeAdapter 或引入 BRVAH。
 */
public abstract class BaseAdapter<T, VB extends ViewBinding>
        extends RecyclerView.Adapter<BaseAdapter.BaseHolder<VB>> {

    protected final List<T> items = new ArrayList<>();

    public void submit(List<T> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void add(T item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public List<T> getItems() {
        return items;
    }

    @NonNull
    @Override
    public BaseHolder<VB> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VB binding = createBinding(LayoutInflater.from(parent.getContext()), parent);
        return new BaseHolder<>(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder<VB> holder, int position) {
        bind(holder.binding, items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected abstract VB createBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent);

    protected abstract void bind(@NonNull VB binding, @NonNull T item, int position);

    public static class BaseHolder<VB extends ViewBinding> extends RecyclerView.ViewHolder {
        public final VB binding;

        public BaseHolder(@NonNull VB binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

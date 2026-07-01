package com.campus.help.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campus.help.core.base.BaseFragment;
import com.campus.help.data.model.Task;
import com.campus.help.data.repo.TaskRepository;
import com.campus.help.databinding.FragmentHomeBinding;

import java.util.List;

/**
 * 首页 / 接单大厅（成员 B）。地基阶段：展示本地任务列表，验证 Room + LiveData + RecyclerView 链路。
 */
public class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    private TaskRepository repository;
    private TaskAdapter adapter;

    @Override
    protected FragmentHomeBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        adapter = new TaskAdapter();
        adapter.setOnItemClickListener(task -> {
            Intent intent = new Intent(getContext(), TaskDetailActivity.class);
            intent.putExtra("task", task);
            startActivity(intent);
        });
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        repository = new TaskRepository();
        LiveData<List<Task>> data = repository.observeAll();
        data.observe(getViewLifecycleOwner(), tasks -> adapter.submit(tasks));
    }
}

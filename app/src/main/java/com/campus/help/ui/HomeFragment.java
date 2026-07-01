package com.campus.help.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campus.help.R;
import com.campus.help.core.base.BaseFragment;
import com.campus.help.data.model.Task;
import com.campus.help.data.repo.TaskRepository;
import com.campus.help.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页 / 接单大厅。支持按类型筛选 + "仅看可接单"开关。
 */
public class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    private TaskRepository repository;
    private TaskAdapter adapter;

    /** 当前选中的类型：-1=全部, 0=跑腿, 1=拼单, 2=二手 */
    private int selectedType = -1;

    /** 是否仅显示可接单（待接单 & 未超时） */
    private boolean availableOnly = false;

    /** 当前类型筛选下的完整列表，切换"可接单"开关时做本地过滤 */
    private List<Task> fullList = new ArrayList<>();

    @Override
    protected FragmentHomeBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        repository = new TaskRepository();

        adapter = new TaskAdapter();
        adapter.setOnItemClickListener(task -> {
            Intent intent = new Intent(getContext(), TaskDetailActivity.class);
            intent.putExtra("task", task);
            startActivity(intent);
        });
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        // 类型筛选
        setupTypeFilter();

        // 可接单开关
        binding.switchAvailable.setOnCheckedChangeListener((btn, checked) -> {
            availableOnly = checked;
            applyFilters();
        });

        // 默认选中"全部"
        binding.filterAll.setSelected(true);
    }

    @Override
    protected void initData() {
        loadTasks();
    }

    // ==================== 类型筛选 ====================

    private void setupTypeFilter() {
        View.OnClickListener listener = v -> selectType(v.getId());

        binding.filterAll.setOnClickListener(listener);
        binding.filterExpress.setOnClickListener(listener);
        binding.filterGroup.setOnClickListener(listener);
        binding.filterSecondhand.setOnClickListener(listener);
    }

    private void selectType(int viewId) {
        binding.filterAll.setSelected(false);
        binding.filterExpress.setSelected(false);
        binding.filterGroup.setSelected(false);
        binding.filterSecondhand.setSelected(false);

        if (viewId == R.id.filter_all) {
            binding.filterAll.setSelected(true);
            selectedType = -1;
        } else if (viewId == R.id.filter_express) {
            binding.filterExpress.setSelected(true);
            selectedType = 0;
        } else if (viewId == R.id.filter_group) {
            binding.filterGroup.setSelected(true);
            selectedType = 1;
        } else if (viewId == R.id.filter_secondhand) {
            binding.filterSecondhand.setSelected(true);
            selectedType = 2;
        }

        loadTasks();
    }

    // ==================== 数据加载 ====================

    private void loadTasks() {
        LiveData<List<Task>> data;
        if (selectedType >= 0) {
            data = repository.observeByType(selectedType);
        } else {
            data = repository.observeAll();
        }

        data.observe(getViewLifecycleOwner(), tasks -> {
            fullList = tasks != null ? tasks : new ArrayList<>();
            applyFilters();
        });
    }

    /** 对 fullList 应用"可接单"本地过滤，提交到 adapter */
    private void applyFilters() {
        if (availableOnly) {
            List<Task> filtered = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (Task t : fullList) {
                if (t.status == 0 && t.deadline > now) {
                    filtered.add(t);
                }
            }
            adapter.submit(filtered);
        } else {
            adapter.submit(fullList);
        }
    }
}

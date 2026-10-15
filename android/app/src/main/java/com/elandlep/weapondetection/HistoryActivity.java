package com.elandlep.weapondetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private HistoryAdapter adapter;
    private List<DatabaseHelper.HistoryItem> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DatabaseHelper(this);
        historyList = dbHelper.getAllHistory();

        RecyclerView recyclerView = findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        // --- 1. تفعيل السحب للمسح (Swipe) ---
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                DatabaseHelper.HistoryItem item = historyList.get(position);

                dbHelper.deleteItem(item.id);
                historyList.remove(position);
                adapter.notifyItemRemoved(position);

                Toast.makeText(HistoryActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        // --- 2. زر مسح الكل ---
        ImageButton btnDeleteAll = findViewById(R.id.btn_delete_all);
        btnDeleteAll.setOnClickListener(v -> {
            if (historyList.isEmpty()) {
                Toast.makeText(this, "History is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Delete All")
                    .setMessage("Clear all history?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbHelper.deleteAll();
                        historyList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // --- 3. الشريط السفلي ---
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_history);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_history) return true;
                if (itemId == R.id.navigation_detection) {
                    startActivity(new Intent(HistoryActivity.this, MainActivity.class)); finish(); return true;
                }
                if (itemId == R.id.navigation_settings) {
                    startActivity(new Intent(HistoryActivity.this, SettingsActivity.class)); finish(); return true;
                }
                if (itemId == R.id.navigation_about) {
                    startActivity(new Intent(HistoryActivity.this, AboutActivity.class)); finish(); return true;
                }
                return false;
            }
        });
    }

    // --- Adapter ---
    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
        List<DatabaseHelper.HistoryItem> items;

        public HistoryAdapter(List<DatabaseHelper.HistoryItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }

        // تحديد مكان كل حاجه
        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            DatabaseHelper.HistoryItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDate.setText(item.date);
            holder.tvScore.setText(item.score);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvScore;
            public HistoryViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.history_title);
                tvDate = itemView.findViewById(R.id.history_date);
                tvScore = itemView.findViewById(R.id.history_confidence);
            }
        }
    }
}
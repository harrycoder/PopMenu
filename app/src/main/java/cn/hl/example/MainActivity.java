package cn.hl.example;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.hl.popmenu.ActionItem;
import cn.hl.popmenu.PopMenu;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private CommonAdapter<String> mAdapter;
    private List<String> mList;
    private PopMenu mPopMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mFab.setVisibility(View.GONE);

        initPopMenu();

        mList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            mList.add(i + "");
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(this)
                .color(Color.BLUE)
                .size(1)
                .build());
        mAdapter = new CommonAdapter<String>(this, R.layout.list_item_main, mList) {

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopMenu(mPopMenu, v, (String) v.getTag());
                }
            };

            @Override
            protected void convert(ViewHolder holder, String s, int position) {
                holder.setText(R.id.tv_left, s);
                holder.setText(R.id.tv_center, s);
                holder.setText(R.id.tv_right, s);
                holder.setTag(R.id.tv_left, s + "--left");
                holder.setTag(R.id.tv_center, s + "--center");
                holder.setTag(R.id.tv_right, s + "--right");
                holder.setOnClickListener(R.id.tv_left, listener);
                holder.setOnClickListener(R.id.tv_center, listener);
                holder.setOnClickListener(R.id.tv_right, listener);
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }


    private void initPopMenu() {
        List<ActionItem> mPopItems = new ArrayList<>();
        mPopItems.add(new ActionItem(0, "好", R.mipmap.ic_launcher));
        mPopItems.add(new ActionItem(1, "很好"));
        mPopItems.add(new ActionItem(2, "很好啊"));
        mPopMenu = new PopMenu.Builder(this)
                .addData(mPopItems)
                //.setCancelable(false)
                .setCornerRadius(10f)
                .build();
    }

    private void showPopMenu(PopMenu popMenu, View v, final String text) {
        if (popMenu.isShowing()) {
            popMenu.dismiss();
        } else {
            popMenu.show(v);
            popMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {

                @Override
                public void onItemClickListener(ActionItem item, int position) {
                    switch (item.getTag()) {
                        case 0:
                            showToast(item.getText() + "one:" + text);
                            break;
                        case 1:
                            showToast(item.getText() + "two:" + text);
                            break;
                        case 2:
                            showToast(item.getText() + "three:" + text);
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            View view = findViewById(R.id.action_settings);
            showPopMenu(mPopMenu, view, item.getTitle().toString());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

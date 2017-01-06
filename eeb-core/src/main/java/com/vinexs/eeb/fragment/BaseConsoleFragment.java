package com.vinexs.eeb.fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vinexs.R;
import com.vinexs.eeb.BaseFragment;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;

public abstract class BaseConsoleFragment extends BaseFragment {

    public ListView lstConsole;
    public LogAdapter adapter;

    public abstract int getConsoleListViewResId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int consoleId = getConsoleListViewResId();
        if (consoleId == 0) {
            removeSelf();
        }

        lstConsole = (ListView) view.findViewById(getConsoleListViewResId());
        if (lstConsole == null) {
            Log.e("ConsoleFragment", "Cannot find console ListView by resource id.");
            removeSelf();
        }

        adapter = new LogAdapter(getActivity(), R.layout.console_row_adapter);
        lstConsole.setAdapter(adapter);
        lstConsole.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                LogData data = adapter.getItem(i);
                if (data == null) {
                    return false;
                }
                ClipData clip = ClipData.newPlainText(data.tag, data.tag + "|" + data.msg);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), R.string.copied_clipboard, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem openItem = menu.add(R.string.clear_log);
        openItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                adapter.listItem.clear();
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @SuppressWarnings("unused")
    public void addLog(String tag, String msg) {
        adapter.listItem.add(new LogData(tag, msg));
        adapter.notifyDataSetChanged();
        lstConsole.post(new Runnable() {
            @Override
            public void run() {
                lstConsole.setSelection(lstConsole.getCount() - 1);
            }
        });
    }

    public class LogAdapter extends ArrayAdapter {

        private LayoutInflater inflater;
        private int resourceLayout;
        ArrayList<LogData> listItem = new ArrayList<>();

        LogAdapter(Context context, int resource) {
            super(context, resource);
            resourceLayout = resource;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @SuppressWarnings("NullableProblems")
        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(resourceLayout, null);
                holder = new ViewHolder();
                holder.txtTag = (TextView) convertView.findViewById(R.id.txt_tag);
                holder.txtContent = (TextView) convertView.findViewById(R.id.txt_msg);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.txtTag.setText(listItem.get(position).tag);
            holder.txtContent.setText(listItem.get(position).msg);
            return convertView;
        }

        @Override
        public int getCount() {
            return listItem.size();
        }

        @Override
        public LogData getItem(int position) {
            return listItem.get(position);
        }
    }

    public class LogData {

        String tag = "";

        String msg = "";

        LogData(String tag, String msg) {
            this.tag = tag;
            this.msg = msg;
        }

    }

    private class ViewHolder {

        TextView txtTag;

        TextView txtContent;

    }
}

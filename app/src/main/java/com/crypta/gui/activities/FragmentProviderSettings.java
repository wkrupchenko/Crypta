package com.crypta.gui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.crypta.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D064343 on 15.07.2016.
 */
public class FragmentProviderSettings extends PreferenceFragmentCompat {

    private ProviderAdapter adapter;

    public FragmentProviderSettings() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String string) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
           /* View rootView = inflater.inflate(R.layout.fragment_preferences, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));*/
        View rootView = inflater.inflate(R.layout.fragment_provider_list, container, false);
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        adapter = new ProviderAdapter(rootView.getContext());
        gridview.setAdapter(adapter);
        registerForContextMenu(gridview);

        return rootView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.item_actions_provider_settings, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        SharedPreferences prefs = getActivity().getSharedPreferences("provider-tokens", Context.MODE_PRIVATE);
        switch (item.getItemId()) {
            case R.id.nav_change_files_password:
                Intent it = new Intent(getActivity().getApplicationContext(),
                        ChangeLocalPasswordActivity.class);
                startActivity(it);
                // Do some stuff
                return true;
            case R.id.nav_delete_provider:
                GridView gv = (GridView) info.targetView.getParent();
                adapter.mThumbIds.remove(0);
                adapter.notifyDataSetChanged();
                prefs.edit().remove("dropbox-access-token").commit();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    class ProviderAdapter extends BaseAdapter {
        ProviderAdapter adapter = this;
        private Context mContext;
        private List<Integer> mThumbIds = new ArrayList<Integer>();

        public ProviderAdapter(Context c) {
            mContext = c;
            mThumbIds.add(R.drawable.dropbox_logo);
        }

        public int getCount() {
            return mThumbIds.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each added provider
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            final Integer i;
            i = mThumbIds.get(position);

            SharedPreferences prefs = getActivity().getSharedPreferences("provider-tokens", Context.MODE_PRIVATE);
            String dropboxAccessToken = prefs.getString("dropbox-access-token", null);

            if (dropboxAccessToken != null) {

                imageView.setImageResource(mThumbIds.get(position));
                imageView.setBackground(ContextCompat.getDrawable(imageView.getContext(), R.drawable.image_border));
            }

           /* imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    adapter.mThumbIds.remove(i);
                    adapter.notifyDataSetChanged();

                }
            });*/

            return imageView;
        }


    }

}

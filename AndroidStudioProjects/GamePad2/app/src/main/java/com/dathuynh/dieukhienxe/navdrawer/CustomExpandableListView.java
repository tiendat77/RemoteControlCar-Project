package com.dathuynh.dieukhienxe.navdrawer;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.dathuynh.dieukhienxe.R;
import com.dathuynh.dieukhienxe.model.CarModel;

import java.util.HashMap;
import java.util.List;

public class CustomExpandableListView extends BaseExpandableListAdapter {
    Context context;
    List<String> listHeader;
    HashMap<String, List<CarModel>> listChild;
    public boolean darkmode = false;

    public CustomExpandableListView(Context context, List<String> listHeader, HashMap<String, List<CarModel>> listChild, boolean darkmode) {
        this.context = context;
        this.listHeader = listHeader;
        this.listChild = listChild;
        this.darkmode = darkmode;
    }

    @Override
    public int getGroupCount() {
        return listHeader.size();
    }

    @Override
    // dem so phan tu child cua 1 header
    public int getChildrenCount(int groupPosition) {
        return listChild.get(listHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listChild.get(listHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView textheader;
        convertView = layoutInflater.inflate(R.layout.nav_group_view, null);
        textheader = convertView.findViewById(R.id.tvGroup);
        if (darkmode == false) {
            textheader.setTextColor(ContextCompat.getColor(context, R.color.text_group));
            textheader.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_memu));
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_memu));
        } else {
            textheader.setTextColor(ContextCompat.getColor(context, R.color.text_group_dark));
            textheader.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_menu_dark));
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_menu_dark));

        }
        textheader.setText(headerTitle);
        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        //String item = (String) getChild(groupPosition,childPosition);
        String item = (String) listChild.get(listHeader.get(groupPosition)).get(childPosition).CarName;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView textitem;

        convertView = layoutInflater.inflate(R.layout.nav_child_view, null);
        textitem = convertView.findViewById(R.id.tvChild);

        //check darkmode
        if (darkmode == false) {
            textitem.setTextColor(ContextCompat.getColor(context, R.color.text_child));
            textitem.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_child));


        } else {
            textitem.setTextColor(ContextCompat.getColor(context, R.color.text_child_dark));
            textitem.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_child_dark));

        }

        textitem.setText(item);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
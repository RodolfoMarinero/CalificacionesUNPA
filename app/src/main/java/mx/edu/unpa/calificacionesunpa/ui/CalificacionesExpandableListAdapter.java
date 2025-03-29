package mx.edu.unpa.calificacionesunpa.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import java.util.List;
import java.util.Map;

public class CalificacionesExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> materias;
    private Map<String, List<String>> calificaciones;

    public CalificacionesExpandableListAdapter(Context context, List<String> materias, Map<String, List<String>> calificaciones) {
        this.context = context;
        this.materias = materias;
        this.calificaciones = calificaciones;
    }

    @Override
    public int getGroupCount() {
        return materias.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return calificaciones.get(materias.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return materias.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return calificaciones.get(materias.get(groupPosition)).get(childPosition);
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
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText((String) getGroup(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText((String) getChild(groupPosition, childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

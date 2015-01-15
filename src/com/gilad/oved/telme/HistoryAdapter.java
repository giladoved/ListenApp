package com.gilad.oved.telme;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HistoryAdapter extends BaseAdapter {
	
	private final Context context;
	private ArrayList<HistoryItem> historyItemList;

	public HistoryAdapter(Context context, ArrayList<HistoryItem> historyItemList) {
		  this.context = context;
		  this.historyItemList = historyItemList;
	}
	
	@Override
    public int getCount() {
        return historyItemList.size();
    }

    @Override
    public Object getItem(int pos) {
        return historyItemList.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }
    
    public HistoryItem getHistoryItem(int position)
    {
        return historyItemList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	if(convertView == null) {
    		 LayoutInflater inflater = (LayoutInflater) context
    			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		 String sentBool = historyItemList.get(position).sentBool;
    		 if (sentBool.charAt(0) == Constants.SENT_FLAG.charAt(0))
    			 convertView = inflater.inflate(R.layout.list_row_layout_odd, parent,false);
    		 else
    			 convertView = inflater.inflate(R.layout.list_row_layout_even, parent,false);
        }

	    TextView textView = (TextView) convertView.findViewById(R.id.text);
	    textView.setText(historyItemList.get(position).date);
	    //ImageView imageView = (ImageView) rowView.findViewById(R.id.user_img);
	    //textView.setText();
	    
	    return convertView;
    }

}
	
/*	
	private final Context context;
	private final Child child;

	public HistoryAdapter(Context context, Child child) {
		  super(context, R.layout.list_row_layout_even, child.getDates());
		  this.context = context;
		  this.child = child;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = null;
	    if (this.sent)
	    	rowView = inflater.inflate(R.layout.list_row_layout_even, parent, false);
	    else 
	    	rowView = inflater.inflate(R.layout.list_row_layout_odd, parent, false);
	    
	    TextView textView = (TextView) rowView.findViewById(R.id.text);
	    //ImageView imageView = (ImageView) rowView.findViewById(R.id.user_img);
	    //textView.setText();

	    return rowView;
	  }
	} 
*/
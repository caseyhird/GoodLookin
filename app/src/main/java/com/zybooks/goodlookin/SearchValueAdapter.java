package com.zybooks.goodlookin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchValueAdapter extends RecyclerView.Adapter<SearchValueAdapter.ViewHolder> {

    private List<ResultValue> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    SearchValueAdapter(Context context, List<ResultValue> data){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        String name = mData.get(position).getName();
        String url = mData.get(position).getUrl();
        String snippet = mData.get(position).getSnippet();

        holder.nameTextView.setText(name);
        holder.urlTextView.setText(url);
        holder.snippetTextView.setText(snippet);
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView urlTextView;
        TextView nameTextView;
        TextView snippetTextView;

        ViewHolder(View itemView){
            super(itemView);
            urlTextView = itemView.findViewById(R.id.urlLink);
            nameTextView = itemView.findViewById(R.id.name);
            snippetTextView = itemView.findViewById(R.id.snippet);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){
            if(mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    ResultValue getItem(int id){
        return mData.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onItemClick(View view, int position);
    }
}

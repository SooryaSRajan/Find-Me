package com.ssr_projects.findmyphone.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.ssr_projects.findmyphone.AddContactsActivity;
import com.ssr_projects.findmyphone.R;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<NumberDataHolder> numberList;
    SharedPreferences sharedPreferences;
    AddContactsActivity context;


    public RecyclerAdapter(ArrayList<NumberDataHolder> numberList, SharedPreferences sharedPreferences, AddContactsActivity context) {
        this.numberList = numberList;
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new numberViewHolder(inflater.inflate(R.layout.number_layout, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((numberViewHolder)holder).nameTextView.setText(numberList.get(position).userName);
        ((numberViewHolder)holder).numberTextView.setText(numberList.get(position).userNumber);
        ((numberViewHolder)holder).nameInitialTextView.setText(numberList.get(position).userName.charAt(0) + "");
    }

    @Override
    public int getItemCount() {
        return numberList.size();
    }

    public class numberViewHolder extends RecyclerView.ViewHolder {
        TextView numberTextView;
        TextView nameTextView;
        TextView nameInitialTextView;
        ImageButton deleteNumber;

        public numberViewHolder(View inflate) {
            super(inflate);
            deleteNumber = inflate.findViewById(R.id.delete_number);
            numberTextView = inflate.findViewById(R.id.number);
            nameTextView = inflate.findViewById(R.id.name);
            nameInitialTextView = inflate.findViewById(R.id.name_letter_first);

            deleteNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    numberList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    saveArrayList(numberList, AddContactsActivity.PREF_ARRAY_LIST);
                    context.setRecycerVisibility(true);
                }
            });
        }
    }

    public void saveArrayList(ArrayList<NumberDataHolder> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

}

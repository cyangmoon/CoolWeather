package com.ismael.weather.custom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ismael.weather.MainActivity;
import com.ismael.weather.R;
import com.ismael.weather.db.SelectedCity;

import java.util.List;

public class SelectedCityAdapter extends RecyclerView.Adapter<SelectedCityAdapter.ViewHolder> {

    private Context mContext;
    private List<SelectedCity> mSelectedCityList;
    private Context context;
    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView mCardView;
        TextView mCityName;
        TextView mCityWeatherInfo;
        ImageButton mImageButton;
        public ViewHolder(View view){
            super(view);
            mCityName = view.findViewById(R.id.selected_city_item_cityName_textView);
            mCityWeatherInfo = view.findViewById(R.id.selected_city_item_type_textView);
            mImageButton = view.findViewById(R.id.selected_city_item_home_imageView);
            mCardView = view.findViewById(R.id.cardView);
        }
    }

    public SelectedCityAdapter(List<SelectedCity> selectedCityList,Context context){
        mSelectedCityList=selectedCityList;
        this.context = context;
    }

    @NonNull
    @Override
    public SelectedCityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) { mContext = parent.getContext(); }
        View view = LayoutInflater.from(mContext).inflate(R.layout.selected_city_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedCityAdapter.ViewHolder holder, final int position) {
        final SelectedCity selectedCity = mSelectedCityList.get(position);
        holder.mCityName.setText(selectedCity.getCityName());
        holder.mCityWeatherInfo.setText(selectedCity.getCityWeatherInfo());
        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.instance).edit();
                editor.putInt("permanentCountyCode",selectedCity.getCountyCode());
                editor.putInt("currentCountyCode",selectedCity.getCountyCode());
                editor.apply();
                ((Activity)mContext).setResult(Activity.RESULT_OK);
                ((Activity) mContext).finish();
            }
        });
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.instance).edit();
                editor.putInt("currentCountyCode",selectedCity.getCountyCode());
                editor.apply();
                ((Activity)mContext).setResult(Activity.RESULT_OK);
                ((Activity) mContext).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSelectedCityList.size();
    }

}

package com.finalproject.weather;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainFragment extends Fragment {

    ArrayList<ConsolidatedWeather> consolidatedWeather;
    ArrayList<Source> sources;

    TextView todayT, cityName, tempToday, minTemp, maxTemp  , tyepWeather, humidity, predictability, moreDetails;
    ImageView iconWeather;

    LinearLayout linearLayout;
    View rootView;
    Bundle bundle;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.fragment_weather,container,false);
        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cityName = rootView.findViewById(R.id.city);
        tempToday = rootView.findViewById(R.id.temperature);
        minTemp = rootView.findViewById(R.id.minimum);
        maxTemp = rootView.findViewById(R.id.max);
        tyepWeather = rootView.findViewById(R.id.type);
        humidity = rootView.findViewById(R.id.humidity);
        predictability = rootView.findViewById(R.id.predictability);
        iconWeather = rootView.findViewById(R.id.image);
        todayT = rootView.findViewById(R.id.day);

        moreDetails = rootView.findViewById(R.id.details);

        moreDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                MainwebView mainwebView = new MainwebView();
                fragmentTransaction.add(R.id.firstOne, mainwebView);
                fragmentTransaction.addToBackStack(null);
                mainwebView.setArguments(bundle);
                fragmentTransaction.commit();
            }
        });


        linearLayout = rootView.findViewById(R.id.myLayout);

        getWeather();
    }

    public void getWeather()
    {
        GetDataServiceInterface service = RetrofitClientInstance.getRetrofitInstance().create(GetDataServiceInterface.class);

        Call<Weather> call = service.getWeather(getArguments().getInt("geoid"));

        call.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {


                Weather weather = response.body();

                sources = new ArrayList<>(weather.getSources());
                Parent parent = weather.getParent();

                consolidatedWeather = new ArrayList<>(weather.getConsolidatedWeather());

                bundle = new Bundle();

                bundle.putString("bbc_url", sources.get(0).getUrl() + getArguments().getInt("bbcid"));
                bundle.putString("city", weather.getTitle());

                Date date = new Date();
                String date1 = consolidatedWeather.get(0).getApplicableDate();
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd").parse(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMM");
                String today = simpleDateFormat.format(date);
                cityName.setText(weather.getTitle() + ",\n" + parent.getTitle());
                tempToday.setText(String.format("%.2f",consolidatedWeather.get(0).getTheTemp()) + "°C");
                minTemp.setText(String.format("%.2f",consolidatedWeather.get(0).getMinTemp()) + "°");
                maxTemp.setText(String.format("%.2f",consolidatedWeather.get(0).getMaxTemp()) + "°");
               tyepWeather.setText(consolidatedWeather.get(0).getWeatherStateName());
                humidity.setText(consolidatedWeather.get(0).getHumidity().toString() + "%");
                predictability.setText(consolidatedWeather.get(0).getPredictability().toString() + "%");
                Picasso.get().load(getWeatherImage(consolidatedWeather.get(0).getWeatherStateAbbr())).into(iconWeather);
                todayT.setText(today);

                initForecastWeather(consolidatedWeather);

                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(weather.getTitle().toUpperCase());

                linearLayout.setBackgroundResource(R.drawable.mybackground);



            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {

            }
        });
    }

    public String getWeatherImage(String weatherStateAbbr)
    {
        return "https://www.metaweather.com/static/img/weather/png/" + weatherStateAbbr + ".png";
    }

    public void initForecastWeather(ArrayList<ConsolidatedWeather> consolidatedWeather)
    {
        RecyclerView recyclerView = rootView.findViewById(R.id.fiveRecycle);
        consolidatedWeather.remove(0);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        WeatherAdapter adapter = new WeatherAdapter(consolidatedWeather, getActivity().getApplicationContext());
        recyclerView.setAdapter(adapter);
    }

    
}

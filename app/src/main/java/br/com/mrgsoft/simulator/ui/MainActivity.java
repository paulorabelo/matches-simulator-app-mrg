package br.com.mrgsoft.simulator.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import br.com.mrgsoft.simulator.R;
import br.com.mrgsoft.simulator.data.MatchesApi;
import br.com.mrgsoft.simulator.databinding.ActivityMainBinding;
import br.com.mrgsoft.simulator.domain.Match;
import br.com.mrgsoft.simulator.ui.adapter.MatchesAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MatchesApi matchesApi;
    private MatchesAdapter matchesAdapter = new MatchesAdapter(Collections.emptyList());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatingActionButton();
    }

    private void setupHttpClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://paulorabelo.github.io/matches-sim-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        matchesApi = retrofit.create(MatchesApi.class);
    }

    private void setupMatchesList() {
        binding.rvMatches.setHasFixedSize(true);
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMatches.setAdapter(matchesAdapter);
        findMatchesFromApi();
    }

    private void setupMatchesRefresh() {
        binding.srlMatches.setOnRefreshListener(this::findMatchesFromApi);
    }
    private void setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener(view -> {
            view.animate().rotationBy(360).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Random random = new Random();
                    for (int i = 0; i < matchesAdapter.getItemCount(); i++) {
                        Match match = matchesAdapter.getMatches().get(i);
                        match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStars() + 1));
                        match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStars() + 1));
                        matchesAdapter.notifyItemChanged(i);
                    }
                }
            });
//            throw new RuntimeException("Teste Crashlytics");
        });
    }
    private void findMatchesFromApi() {
        binding.srlMatches.setRefreshing(true);
        matchesApi.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(@NonNull Call<List<Match>> call, @NonNull Response<List<Match>> response) {
                if (response.isSuccessful()) {
                    List<Match> matches = response.body();
                    // Log.i("Simulator", "Deu tudo certo! Total de partidas: " + matches.size());
                    matchesAdapter = new MatchesAdapter(matches);
                    binding.rvMatches.setAdapter(matchesAdapter);
                } else {
                    showErrorMessage();
                }
                binding.srlMatches.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<Match>> call, @NonNull Throwable t) {
                showErrorMessage();
                binding.srlMatches.setRefreshing(false);
            }
        });
    }

    private void showErrorMessage() {
        //TODO:
        Snackbar.make(binding.fabSimulate, R.string.error_api, Snackbar.LENGTH_LONG).show();
    }
}

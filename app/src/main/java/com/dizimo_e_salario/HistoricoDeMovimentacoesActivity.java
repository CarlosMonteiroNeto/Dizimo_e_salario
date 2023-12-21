package com.dizimo_e_salario;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;

public class HistoricoDeMovimentacoesActivity extends AppCompatActivity {
    RecyclerView movimentacoesRecycler;
    HistoricoDeMovimentacoesAdapter adapter;

    HistoricoDeMovimentacoesViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_de_movimentacoes);
        SharedPreferences sharedPreferences = getSharedPreferences("saldos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        viewModel = new ViewModelProvider(this).get(HistoricoDeMovimentacoesViewModel.class);

        movimentacoesRecycler = findViewById(R.id.recyclerview_movimentacoes);
        adapter = new HistoricoDeMovimentacoesAdapter(new ArrayList<>(), sharedPreferences, viewModel);
        movimentacoesRecycler.setLayoutManager(new LinearLayoutManager(this));
        movimentacoesRecycler.setAdapter(adapter);
        movimentacoesRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        viewModel.getMovimentacoes().observe(HistoricoDeMovimentacoesActivity.this, movimentacoes -> adapter.atualizarItens(movimentacoes));
    }
}
package com.dizimo_e_salario;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HistoricoDeMovimentacoesActivity extends AppCompatActivity {
    RecyclerView movimentacoesRecycler;
    HistoricoDeMovimentacoesAdapter adapter;
    ProgressBar progressBar;
    View blockingView;

    SharedViewModel viewModel;
    String usuarioLogado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_de_movimentacoes);
        progressBar = findViewById(R.id.progressBar);
        blockingView = findViewById(R.id.blockingView);

        SharedPreferences preferences = getSharedPreferences(LoginActivity.DADOS_DE_LOGIN, Context.MODE_PRIVATE);
        usuarioLogado = preferences.getString(LoginActivity.CHAVE_USUARIO, LoginActivity.USUARIO_PADRAO);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //É obrigatório chamar carregarViewModel após o construtor para inicializá-lo corretamente
        viewModel = ((MinhaAplicacao) getApplication()).getViewModel();
//        viewModel.carregarViewModel(usuarioLogado);
        viewModel.carregarMovimentacoes();

        movimentacoesRecycler = findViewById(R.id.recyclerview_movimentacoes);
        adapter = new HistoricoDeMovimentacoesAdapter(new ArrayList<>(), db, viewModel, usuarioLogado);
        movimentacoesRecycler.setLayoutManager(new LinearLayoutManager(this));
        movimentacoesRecycler.setAdapter(adapter);
        movimentacoesRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        viewModel.getMovimentacoes().observe(HistoricoDeMovimentacoesActivity.this, movimentacoes -> adapter.atualizarItens(movimentacoes));

        viewModel.getMensagemDeExclusaoDeMovimentacao().observe(HistoricoDeMovimentacoesActivity.this, mensagem ->
                Toast.makeText(HistoricoDeMovimentacoesActivity.this, mensagem, Toast.LENGTH_SHORT).show());

        viewModel.isLoading().observe(HistoricoDeMovimentacoesActivity.this, isLoading ->{
            if (isLoading){
                progressBar.setVisibility(View.VISIBLE);
                blockingView.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                blockingView.setVisibility(View.GONE);
            }
        });
    }
}
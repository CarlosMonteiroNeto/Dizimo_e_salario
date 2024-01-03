package com.dizimo_e_salario;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ValoresAReceberActivity extends AppCompatActivity {

    RecyclerView valoresAReceberRecycler;

    ImageButton btnAddValorAReceber;
    TextView subAddValorAReceber;
    ValoresAReceberAdapter adapter;

    SharedViewModel viewModel;
    String usuarioLogado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO AJUSTAR layout
        setContentView(R.layout.activity_valores_a_receber);

        SharedPreferences preferences = getSharedPreferences(LoginActivity.DADOS_DE_LOGIN, Context.MODE_PRIVATE);
        usuarioLogado = preferences.getString(LoginActivity.CHAVE_USUARIO, LoginActivity.USUARIO_PADRAO);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //É obrigatório chamar carregarViewModel após o construtor para inicializá-lo corretamente
        viewModel = ((MinhaAplicacao) getApplication()).getViewModel();
        viewModel.carregarViewModel(usuarioLogado);
        viewModel.carregarValoresAReceber();

        valoresAReceberRecycler = findViewById(R.id.recyclerview_valores_a_receber);
        adapter = new ValoresAReceberAdapter(new ArrayList<>(), db, viewModel, usuarioLogado);
        valoresAReceberRecycler.setLayoutManager(new LinearLayoutManager(this));
        valoresAReceberRecycler.setAdapter(adapter);
        valoresAReceberRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        btnAddValorAReceber = findViewById(R.id.btn_add_valor_a_receber);
        subAddValorAReceber = findViewById(R.id.sub_btn_add_valor_a_receber);

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK){
                Intent dados = result.getData();
                if(dados != null && dados.hasExtra("Valor a receber")){
                    ValorAReceber valorNovo = (ValorAReceber) dados.getSerializableExtra("Valor a receber");
                    viewModel.addValorAReceber(valorNovo).observe(this, mensagem -> {
                        if (mensagem != null) {
                            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        btnAddValorAReceber.setOnClickListener(v -> launcher.launch(new Intent(ValoresAReceberActivity.this, AddValorAReceberActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getValoresAReceber().observe(ValoresAReceberActivity.this, valores -> adapter.atualizarItens(valores));
    }
}

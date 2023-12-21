package com.dizimo_e_salario;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dizimo_e_salario.canarinho.formatador.FormatadorValor;
import com.dizimo_e_salario.canarinho.watcher.ValorMonetarioWatcher;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //public static final String CHAVE_SALDO = "CHAVE_SALDO";
    public static final String CHAVE_DIZIMO = "CHAVE_DIZIMO";
    public static final String CHAVE_SALARIO = "CHAVE_SALARIO";
    //public static final float SALDO_PADRAO = 0;
    public static final float DIZIMO_PENDENTE_PADRAO = 0;
    public static final float SALARIO_RESTANTE_PADRAO = 0;
    TextView tituloValorDaMovimentacao, tituloTipoDaMovimentacao, tituloDizimo, valorDizimoPendente, tituloSalario, valorSalarioRestante, tituloDescricao;
    EditText edtTxtValorDaMovimentacao, editDescricao;
    //descricaoDaMovimentacao;
    Spinner spnTiposDeMovimentacao;
    Button botaoRegistrar;
    ImageButton botaoHistoricoDeMovimentacoes;

    public static final String MOVIMENTACOES_FINANCEIRAS = "Movimentações financeiras";
    public static final String INFORMACOES_PRINCIPAIS = "Informações principais";

    public static final String SALARIO_RESTANTE = "Salário restante";
    public static final String DIZIMO_PENDENTE = "Dízimo pendente";
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("saldos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
//        if (!sharedPreferences.contains(CHAVE_SALDO)){
//            editor.putFloat(CHAVE_SALDO, SALDO_PADRAO);
//            editor.apply();
//        }
        if (!sharedPreferences.contains(CHAVE_DIZIMO)){
            editor.putFloat(CHAVE_DIZIMO, DIZIMO_PENDENTE_PADRAO);
            editor.apply();
        }
        if (!sharedPreferences.contains(CHAVE_SALARIO)){
            editor.putFloat(CHAVE_SALARIO, SALARIO_RESTANTE_PADRAO);
            editor.apply();
        }
        tituloValorDaMovimentacao = findViewById(R.id.titulo_valor);
        tituloTipoDaMovimentacao = findViewById(R.id.titulo_tipo_de_movimentacao);
        tituloDizimo = findViewById(R.id.titulo_dizimo);
        valorDizimoPendente = findViewById(R.id.dizimo_pendente);
        valorDizimoPendente.setText(FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(sharedPreferences.getFloat(CHAVE_DIZIMO,DIZIMO_PENDENTE_PADRAO))));
//        valorDizimoPendente.addTextChangedListener(new ValorMonetarioWatcher.Builder().comSimboloReal().comMantemZerosAoLimpar().build());
        tituloSalario = findViewById(R.id.titulo_salario);
        valorSalarioRestante = findViewById(R.id.salario_restante);
        valorSalarioRestante.setText(FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(sharedPreferences.getFloat(CHAVE_SALARIO,SALARIO_RESTANTE_PADRAO))));
//        valorSalarioRestante.addTextChangedListener(new ValorMonetarioWatcher.Builder().comSimboloReal().comMantemZerosAoLimpar().build());
        edtTxtValorDaMovimentacao = findViewById(R.id.valor_movimentado);
        edtTxtValorDaMovimentacao.addTextChangedListener(new ValorMonetarioWatcher.Builder()
                .comSimboloReal()
                .comMantemZerosAoLimpar()
                .build());
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        edtTxtValorDaMovimentacao.setOnFocusChangeListener((view, b) -> {
            if (b){
                inputMethodManager.showSoftInput(edtTxtValorDaMovimentacao, InputMethodManager.SHOW_IMPLICIT);
            } else {
                inputMethodManager.hideSoftInputFromWindow(edtTxtValorDaMovimentacao.getWindowToken(),0);
            }
        });


//        descricaoDaMovimentacao = findViewById(R.id.descricao);
        spnTiposDeMovimentacao = findViewById(R.id.spinner_tipo_de_movimentacao);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.tipos_de_movimentacao, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTiposDeMovimentacao.setAdapter(adapter);
        spnTiposDeMovimentacao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edtTxtValorDaMovimentacao.requestFocus();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        tituloDescricao = findViewById(R.id.titulo_descricao);
        editDescricao = findViewById(R.id.edit_descricao);
        botaoRegistrar = findViewById(R.id.botao_registrar);

        botaoRegistrar.setOnClickListener(v -> {
            if (edtTxtValorDaMovimentacao.getText().toString().trim().isEmpty()){
                Toast.makeText(MainActivity.this, "O valor está em branco", Toast.LENGTH_LONG).show();
//            } else if (descricaoDaMovimentacao.getText().toString().trim().isEmpty()) {
//                Toast.makeText(MainActivity.this, "A descrição está em branco", Toast.LENGTH_LONG).show();
            } else if (spnTiposDeMovimentacao.getSelectedItem().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Selecione um tipo de movimentação financeira", Toast.LENGTH_LONG).show();
            } else {

                float dizimoPendente = sharedPreferences.getFloat(CHAVE_DIZIMO, DIZIMO_PENDENTE_PADRAO);
                float salarioRestante = sharedPreferences.getFloat(CHAVE_SALARIO, SALARIO_RESTANTE_PADRAO);
                float valorMovimentado = Float.parseFloat(FormatadorValor.VALOR_COM_SIMBOLO.desformata(edtTxtValorDaMovimentacao.getText().toString()));
//                float saldo = sharedPreferences.getFloat(CHAVE_SALDO, SALDO_PADRAO);

                if (spnTiposDeMovimentacao.getSelectedItem().toString().equals("Saída")) {
//                    saldo = saldo - (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
                    dizimoPendente = (dizimoPendente - (valorMovimentado * (float) 0.1));
                    salarioRestante = (salarioRestante - (valorMovimentado * (float) 0.72));

                } else if (spnTiposDeMovimentacao.getSelectedItem().toString().equals("Entrada")) {
//                    saldo = saldo + (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
                    dizimoPendente = (dizimoPendente + (valorMovimentado * (float) 0.1));
                    salarioRestante = (salarioRestante + (valorMovimentado * (float) 0.72));

                } else if (spnTiposDeMovimentacao.getSelectedItem().toString().equals("Dar dízimo")) {
//                    saldo = saldo - (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
                    dizimoPendente = dizimoPendente - valorMovimentado;

                } else if (spnTiposDeMovimentacao.getSelectedItem().toString().equals("Gasto pessoal")) {
//                    saldo = saldo - (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
                    salarioRestante = salarioRestante - valorMovimentado;
                }
//                editor.putFloat(CHAVE_SALDO, saldo);
                editor.putFloat(CHAVE_DIZIMO, dizimoPendente);
                editor.putFloat(CHAVE_SALARIO, salarioRestante);
                editor.apply();


                valorDizimoPendente.setText(FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(dizimoPendente)));
                valorSalarioRestante.setText(FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(salarioRestante)));

                MovimentacaoFinanceira movimentacaoFinanceira = new MovimentacaoFinanceira();
                movimentacaoFinanceira.setID(UUID.randomUUID().toString());
                movimentacaoFinanceira.setTipo(spnTiposDeMovimentacao.getSelectedItem().toString());
                movimentacaoFinanceira.setValor(edtTxtValorDaMovimentacao.getText().toString());
                movimentacaoFinanceira.setDescricao(editDescricao.getText().toString());
                movimentacaoFinanceira.setData(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

                db.collection(MOVIMENTACOES_FINANCEIRAS).document(movimentacaoFinanceira.getID()).set(movimentacaoFinanceira)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(v.getContext(), "Adicionado com sucesso", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Falha. tente novamente", Toast.LENGTH_SHORT).show());

                Map<String, Object> novoSalarioRestante = new HashMap<>();
                novoSalarioRestante.put("valor", salarioRestante);
                db.collection(INFORMACOES_PRINCIPAIS).document(SALARIO_RESTANTE).set(novoSalarioRestante);

                Map<String, Object> novoDizimoPendente = new HashMap<>();
                novoDizimoPendente.put("valor", dizimoPendente);
                db.collection(INFORMACOES_PRINCIPAIS).document(DIZIMO_PENDENTE).set(novoDizimoPendente);

                edtTxtValorDaMovimentacao.setText("");
            }

        });
        botaoHistoricoDeMovimentacoes = findViewById(R.id.botao_historico);
        botaoHistoricoDeMovimentacoes.setOnClickListener(v -> startActivity(new Intent(this, HistoricoDeMovimentacoesActivity.class)));
    }
}
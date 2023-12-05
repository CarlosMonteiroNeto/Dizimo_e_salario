package com.dizimo_e_salario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dizimo_e_salario.canarinho.formatador.FormatadorValor;
import com.dizimo_e_salario.canarinho.watcher.ValorMonetarioWatcher;

public class MainActivity extends AppCompatActivity {

    //public static final String CHAVE_SALDO = "CHAVE_SALDO";
    public static final String CHAVE_DIZIMO = "CHAVE_DIZIMO";
    public static final String CHAVE_SALARIO = "CHAVE_SALARIO";
    //public static final float SALDO_PADRAO = 0;
    public static final float DIZIMO_PENDENTE_PADRAO = 0;
    public static final float SALARIO_RESTANTE_PADRAO = 0;
    TextView tituloValorDaMovimentacao, tituloTipoDaMovimentacao, tituloDizimo, valorDizimoPendente, tituloSalario, valorSalarioRestante;
    EditText valorDaMovimentacao;
    //descricaoDaMovimentacao;
    Spinner spnTiposDeMovimentacao;
    Button botaoRegistrar;
    ImageButton botaoHistoricoDeMovimentacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        valorDizimoPendente.setText(String.valueOf(sharedPreferences.getFloat(CHAVE_DIZIMO,DIZIMO_PENDENTE_PADRAO)));
//        valorDizimoPendente.addTextChangedListener(new ValorMonetarioWatcher.Builder().comSimboloReal().comMantemZerosAoLimpar().build());
        tituloSalario = findViewById(R.id.titulo_salario);
        valorSalarioRestante = findViewById(R.id.salario_restante);
        valorSalarioRestante.setText(String.valueOf(sharedPreferences.getFloat(CHAVE_SALARIO,SALARIO_RESTANTE_PADRAO)));
//        valorSalarioRestante.addTextChangedListener(new ValorMonetarioWatcher.Builder().comSimboloReal().comMantemZerosAoLimpar().build());
        valorDaMovimentacao = findViewById(R.id.valor_movimentado);
//        valorDaMovimentacao.addTextChangedListener(new ValorMonetarioWatcher.Builder()
//                .comSimboloReal()
//                .comMantemZerosAoLimpar()
//                .build());
//        descricaoDaMovimentacao = findViewById(R.id.descricao);
        spnTiposDeMovimentacao = findViewById(R.id.spinner_tipo_de_movimentacao);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.tipos_de_movimentacao, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTiposDeMovimentacao.setAdapter(adapter);
        botaoRegistrar = findViewById(R.id.botao_registrar);

        botaoRegistrar.setOnClickListener(v -> {
            if (valorDaMovimentacao.getText().toString().trim().isEmpty()){
                Toast.makeText(MainActivity.this, "O valor está em branco", Toast.LENGTH_LONG).show();
//            } else if (descricaoDaMovimentacao.getText().toString().trim().isEmpty()) {
//                Toast.makeText(MainActivity.this, "A descrição está em branco", Toast.LENGTH_LONG).show();
            } else if (spnTiposDeMovimentacao.getSelectedItem().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Selecione um tipo de movimentação financeira", Toast.LENGTH_LONG).show();
            } else {

                float dizimoPendente = sharedPreferences.getFloat(CHAVE_DIZIMO, DIZIMO_PENDENTE_PADRAO);
                float salarioRestante = sharedPreferences.getFloat(CHAVE_SALARIO, SALARIO_RESTANTE_PADRAO);
//                float saldo = sharedPreferences.getFloat(CHAVE_SALDO, SALDO_PADRAO);

                if (spnTiposDeMovimentacao.getSelectedItem().toString().equals("Saída")){
//                    saldo = saldo - (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
                    dizimoPendente = (dizimoPendente - (Float.parseFloat(valorDaMovimentacao.getText().toString())*(float)0.1));
                    salarioRestante = (salarioRestante - (Float.parseFloat(valorDaMovimentacao.getText().toString())*(float)0.72));

                } else if (spnTiposDeMovimentacao.getSelectedItem().toString().equals("Entrada")) {
//                    saldo = saldo + (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
                    dizimoPendente =  (dizimoPendente + (Float.parseFloat(valorDaMovimentacao.getText().toString())*(float)0.1));
                    salarioRestante = (salarioRestante + (Float.parseFloat(valorDaMovimentacao.getText().toString())*(float)0.72));

                } else if (spnTiposDeMovimentacao.getSelectedItem().toString().equals("Dar dízimo")) {
//                    saldo = saldo - (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
                    dizimoPendente = dizimoPendente - Float.parseFloat(valorDaMovimentacao.getText().toString());

                } else if (spnTiposDeMovimentacao.getSelectedItem().toString().equals("Gasto pessoal")) {
//                    saldo = saldo - (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
                    salarioRestante = salarioRestante - Float.parseFloat(valorDaMovimentacao.getText().toString());
                }
//                editor.putFloat(CHAVE_SALDO, saldo);
                editor.putFloat(CHAVE_DIZIMO, dizimoPendente);
                editor.putFloat(CHAVE_SALARIO, salarioRestante);
                editor.apply();

                valorDizimoPendente.setText(String.valueOf(sharedPreferences.getFloat(CHAVE_DIZIMO,DIZIMO_PENDENTE_PADRAO)));
                valorSalarioRestante.setText(String.valueOf(sharedPreferences.getFloat(CHAVE_SALARIO,SALARIO_RESTANTE_PADRAO)));

                valorDaMovimentacao.clearComposingText();
//                descricaoDaMovimentacao.clearComposingText();
                spnTiposDeMovimentacao.setVerticalScrollbarPosition(0);

                //TODO Falta registrar no banco de dados "movimentações financeiras" junto com a data
            }
        });
        botaoHistoricoDeMovimentacoes = findViewById(R.id.botao_historico);
        botaoHistoricoDeMovimentacoes.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Em breve", Toast.LENGTH_SHORT).show();
        });
    }
}
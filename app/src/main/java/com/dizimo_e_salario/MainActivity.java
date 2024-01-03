package com.dizimo_e_salario;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
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

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //public static final String CHAVE_SALDO = "CHAVE_SALDO";
    public static final String CHAVE_DIZIMO = "CHAVE_DIZIMO";
    public static final String CHAVE_SALARIO = "CHAVE_SALARIO";
    //public static final float SALDO_PADRAO = 0;
    public static final float DIZIMO_PENDENTE_PADRAO = 0;
    public static final float SALARIO_RESTANTE_PADRAO = 0;
    TextView tituloValorDaMovimentacao, tituloTipoDaMovimentacao, tituloDizimo, valorDizimoPendente, tituloSalario, valorSalarioRestante, tituloDescricao;
    EditText editDescricao;
    EditText edtTxtValorDaMovimentacao;
    //descricaoDaMovimentacao;
    Spinner spnTiposDeMovimentacao;
    Button botaoRegistrar, botaoValoresAReceber, botaoSair;
    ImageButton botaoHistoricoDeMovimentacoes;
    private String usuarioLogado;

    public static final String MOVIMENTACOES_FINANCEIRAS = "Movimentações financeiras";
    public static final String VALORES_A_RECEBER = "Valores a receber";
    public static final String INFORMACOES_PRINCIPAIS = "Informações principais";

    FirebaseFirestore db;
    Float salarioRestante;
    Float dizimoPendente;
    SharedViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.DADOS_DE_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        usuarioLogado = sharedPreferences.getString(LoginActivity.CHAVE_USUARIO, LoginActivity.USUARIO_PADRAO);

        salarioRestante = 0.00f;
        dizimoPendente = 0.00f;
        //É obrigatório chamar carregarViewModel após o construtor para inicializá-lo corretamente
        viewModel = ((MinhaAplicacao) getApplication()).getViewModel();
        viewModel.carregarViewModel(usuarioLogado);
        viewModel.carregarMovimentacoes();
        db = FirebaseFirestore.getInstance();

//        SharedPreferences sharedPreferences = getSharedPreferences("saldos", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
////        if (!sharedPreferences.contains(CHAVE_SALDO)){
////            editor.putFloat(CHAVE_SALDO, SALDO_PADRAO);
////            editor.apply();
////        }
//        if (!sharedPreferences.contains(CHAVE_DIZIMO)){
//            editor.putFloat(CHAVE_DIZIMO, DIZIMO_PENDENTE_PADRAO);
//            editor.apply();
//        }
//        if (!sharedPreferences.contains(CHAVE_SALARIO)){
//            editor.putFloat(CHAVE_SALARIO, SALARIO_RESTANTE_PADRAO);
//            editor.apply();
//        }
        tituloValorDaMovimentacao = findViewById(R.id.titulo_valor_movimentado);
        tituloTipoDaMovimentacao = findViewById(R.id.titulo_tipo_de_movimentacao);
        tituloDizimo = findViewById(R.id.titulo_dizimo);
        valorDizimoPendente = findViewById(R.id.dizimo_pendente);
//        valorDizimoPendente.setText(FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(dizimoPendente)));
//        valorDizimoPendente.addTextChangedListener(new ValorMonetarioWatcher.Builder().comSimboloReal().comMantemZerosAoLimpar().build());
        tituloSalario = findViewById(R.id.titulo_salario);
        valorSalarioRestante = findViewById(R.id.salario_restante);
//        valorSalarioRestante.setText(FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(salarioRestante)));
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
        spnTiposDeMovimentacao = findViewById(R.id.spinner_tipo_de_movimentacao);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.tipos_de_movimentacao, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTiposDeMovimentacao.setAdapter(adapter);
        tituloDescricao = findViewById(R.id.titulo_descricao_da_movimentacao);
        editDescricao = findViewById(R.id.edit_descricao_da_movimentacao);
        botaoRegistrar = findViewById(R.id.botao_registrar_valor_movimentado);
        botaoValoresAReceber = findViewById(R.id.btn_valores_a_receber);
        botaoValoresAReceber.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ValoresAReceberActivity.class)));

        botaoRegistrar.setOnClickListener(v -> {
            if (edtTxtValorDaMovimentacao.getText().toString().trim().isEmpty()
                    || editDescricao.getText().toString().trim().isEmpty()
                    || spnTiposDeMovimentacao.getSelectedItem().toString().isEmpty()){

                if (edtTxtValorDaMovimentacao.getText().toString().trim().isEmpty()){
                    edtTxtValorDaMovimentacao.setError("O valor está em branco");
                }
                if (editDescricao.getText().toString().trim().isEmpty()) {
                    editDescricao.setError("A descrição está em branco");
                }
                if (spnTiposDeMovimentacao.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Selecione um tipo de movimentação financeira", Toast.LENGTH_LONG).show();
                }
            } else {
                float valorMovimentado = Float.parseFloat(removerMascara(edtTxtValorDaMovimentacao.getText().toString()));
                String tipoDeMovimentacao = spnTiposDeMovimentacao.getSelectedItem().toString();

                MovimentacaoFinanceira movimentacaoFinanceira = new MovimentacaoFinanceira();
//                movimentacaoFinanceira.setID(UUID.randomUUID().toString());
                movimentacaoFinanceira.setTipo(spnTiposDeMovimentacao.getSelectedItem().toString());
                movimentacaoFinanceira.setValor(edtTxtValorDaMovimentacao.getText().toString());
                movimentacaoFinanceira.setDescricao(editDescricao.getText().toString());
                movimentacaoFinanceira.setData(new Date().getTime());

                db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                        .collection(MOVIMENTACOES_FINANCEIRAS).add(movimentacaoFinanceira)
                        .addOnSuccessListener(documentReference -> {
                    viewModel.atualizarSalarioEDizimo(valorMovimentado, tipoDeMovimentacao);
                    edtTxtValorDaMovimentacao.setText("");
                    Toast.makeText(v.getContext(), "Adicionado com sucesso", Toast.LENGTH_SHORT).show();
                })
                        .addOnFailureListener(exception -> Toast.makeText(v.getContext(), "Falha. tente novamente", Toast.LENGTH_SHORT).show());
            }
        });
        botaoHistoricoDeMovimentacoes = findViewById(R.id.botao_historico);
        botaoHistoricoDeMovimentacoes.setOnClickListener(v -> startActivity(new Intent(this, HistoricoDeMovimentacoesActivity.class)));

        botaoSair = findViewById(R.id.btn_sair);
        botaoSair.setOnClickListener(v -> {
            editor.putBoolean(LoginActivity.CHAVE_LOGIN_AUTOMATICO, false);
            editor.putString(LoginActivity.CHAVE_USUARIO, LoginActivity.USUARIO_PADRAO);
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getDizimoPendente().observe(this, dizimo ->
                valorDizimoPendente.setText(FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(dizimo/100))));
        viewModel.getSalarioRestante().observe(this, salario ->
                valorSalarioRestante.setText(FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(salario/100))));
    }

    public static String removerMascara(String textoComMascara) {
        return textoComMascara.replaceAll("[^0-9]", "");
    }
}
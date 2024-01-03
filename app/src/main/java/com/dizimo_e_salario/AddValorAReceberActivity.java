package com.dizimo_e_salario;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vicmikhailau.maskededittext.MaskedEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddValorAReceberActivity extends AppCompatActivity {

    TextView titulodaActivity, tituloNomeDoDevedor, tituloValorDevido, tituloDescricaoDaDivida, tituloDataDePagamento;
    EditText edtNomeDoDevedor, edtValorDevido, edtDescricaoDaDivida;
    MaskedEditText maskedEdtDataDePagamento;
    Button btnRegistrarValorAReceber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_valor_a_receber);
        titulodaActivity = findViewById(R.id.titulo_activity_add_valor_a_receber);
        tituloNomeDoDevedor = findViewById(R.id.titulo_nome_do_devedor);
        tituloValorDevido = findViewById(R.id.titulo_valor_devido);
        tituloDescricaoDaDivida = findViewById(R.id.titulo_descricao_da_divida);
        tituloDataDePagamento = findViewById(R.id.titulo_data_de_pagamento);

        edtNomeDoDevedor = findViewById(R.id.nome_do_devedor);
        edtValorDevido = findViewById(R.id.valor_devido);
        edtDescricaoDaDivida = findViewById(R.id.edit_descricao_da_divida);
        maskedEdtDataDePagamento = findViewById(R.id.edit_data_de_pagamento);
        maskedEdtDataDePagamento.setText("");

        btnRegistrarValorAReceber = findViewById(R.id.botao_registrar_valor_a_receber);

        btnRegistrarValorAReceber.setOnClickListener(v -> {

            if (edtNomeDoDevedor.getText().toString().trim().isEmpty()
                    || edtValorDevido.getText().toString().trim().isEmpty()
                    || edtDescricaoDaDivida.getText().toString().trim().isEmpty()
                    || !EhdataSulamericanaValida(maskedEdtDataDePagamento.getText().toString())){
                if (edtNomeDoDevedor.getText().toString().trim().isEmpty()) {
                    edtNomeDoDevedor.setError("Insira um nome");
                }
                if (edtValorDevido.getText().toString().trim().isEmpty()) {
                    edtValorDevido.setError("Insira um valor");
                }
                if (edtDescricaoDaDivida.getText().toString().trim().isEmpty()){
                    edtDescricaoDaDivida.setError("Sem descrição");
                }
                if(!EhdataSulamericanaValida(maskedEdtDataDePagamento.getText().toString())){
                    maskedEdtDataDePagamento.setError("Data inválida");
                }
                return;
            }

            ValorAReceber novoValor = new ValorAReceber();
            novoValor.setCliente(edtNomeDoDevedor.getText().toString());
            novoValor.setValor(Float.parseFloat(MainActivity.removerMascara(edtValorDevido.getText().toString())));
            novoValor.setDescricao(edtDescricaoDaDivida.getText().toString());
            try {
                novoValor.setData(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(maskedEdtDataDePagamento.getText().toString()).getTime());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Intent resultIntent = new Intent();
            resultIntent.putExtra("Valor a receber", novoValor);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

    }

    public boolean EhdataSulamericanaValida(String date) {
        // Verifica o padrão da string usando uma expressão regular
        if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false; // Não corresponde ao padrão "##/##/####"
        }

        // Divide a string da data em dia, mês e ano
        String[] partesData = date.split("/");

        // Verifica se os componentes são numéricos
        for (String parte : partesData) {
            try {
                Integer.parseInt(parte);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        // Converte os componentes para inteiros
        int dia = Integer.parseInt(partesData[0]);
        int mes = Integer.parseInt(partesData[1]);
        int ano = Integer.parseInt(partesData[2]);

        // Verifica se o dia, mês e ano estão dentro dos limites aceitáveis
        if (dia < 1 || dia > 31 || mes < 1 || mes > 12 || ano < 1900) {
            return false;
        }
        // Verifica meses com 30 dias
        if ((mes == 4 || mes == 6 || mes == 9 || mes == 11) && dia > 30) {
            return false;
        }
        // Verifica fevereiro e anos bissextos
        if (mes == 2) {
            if (ano % 4 == 0 && (ano % 100 != 0 || ano % 400 == 0)) { // Ano bissexto
                if (dia > 29) {
                    return false;
                }
            } else {
                if (dia > 28) {
                    return false;
                }
            }
        }
        return true;
    }
}
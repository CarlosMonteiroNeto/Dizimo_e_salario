package com.dizimo_e_salario;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import androidx.lifecycle.MutableLiveData;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedViewModel extends AndroidViewModel {

    private final FirebaseFirestore db;
    private MutableLiveData<List<MovimentacaoFinanceira>> movimentacoes;
    private MutableLiveData<List<ValorAReceber>> valoresAReceber;
    private MutableLiveData<String>mensagemDeExclusao;
    private MutableLiveData<String>mensagemDeAdicao;
    public static final String SALARIO_RESTANTE = "Salário restante";
    public static final String DIZIMO_PENDENTE = "Dízimo pendente";
    private final MutableLiveData<Float>salarioRestante = new MutableLiveData<>();
    private final MutableLiveData<Float>dizimoPendente = new MutableLiveData<>();
    DocumentReference financeRef;
    private String usuarioLogado = "";

    public SharedViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
    }
    public void carregarViewModel(String usuarioLogado){
        this.usuarioLogado = usuarioLogado;
        financeRef = db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado);
        financeRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Recupera os valores atuais
                this.salarioRestante.setValue(documentSnapshot.getDouble(SALARIO_RESTANTE).floatValue());
                this.dizimoPendente.setValue(documentSnapshot.getDouble(DIZIMO_PENDENTE).floatValue());
            } else {
                this.salarioRestante.setValue(0.00f);
                this.dizimoPendente.setValue(0.00f);
            }
        });
    }
    public void carregarMovimentacoes(){

        db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                .collection(MainActivity.MOVIMENTACOES_FINANCEIRAS).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        List<MovimentacaoFinanceira> movs = new ArrayList<>();
                        for (QueryDocumentSnapshot document:task.getResult())
                        {
                            MovimentacaoFinanceira mov =document.toObject(MovimentacaoFinanceira.class);
                            mov.setID(document.getId());
                            movs.add(mov);
                        }
                        movimentacoes.setValue(movs);
                    }
                });
    }
    public void carregarValoresAReceber(){
        db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                .collection(MainActivity.VALORES_A_RECEBER).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        List<ValorAReceber> valores = new ArrayList<>();
                        for (QueryDocumentSnapshot document:task.getResult())
                        {
                            ValorAReceber valor =document.toObject(ValorAReceber.class);
                            valor.setId(document.getId());
                            valores.add(valor);
                        }
                        valoresAReceber.setValue(valores);
                    }
                });
    }
    public MutableLiveData<List<MovimentacaoFinanceira>> getMovimentacoes(){
        return movimentacoes;
    }

    public MutableLiveData<List<ValorAReceber>> getValoresAReceber() {
        return valoresAReceber;
    }

    public MutableLiveData<String> getMensagemDeExclusao() {
        return mensagemDeExclusao;
    }

    public MutableLiveData<Float> getDizimoPendente() {
        return dizimoPendente;
    }

    public MutableLiveData<Float> getSalarioRestante() {
        return salarioRestante;
    }

    public void setMovimentacoes (List<MovimentacaoFinanceira> movimentacaoFinanceira){
        movimentacoes.setValue(movimentacaoFinanceira);
    }

    //O valor passado depende do tipo de operação.
    // Add movimentação: valor positivo.
    // Excluir movimentação: valor negativo.
    // Editar movimentação: Valor novo - valor anterior
    public void atualizarSalarioEDizimo (float valor, String tipoDeMovimentacao){
        float salarioAtual = salarioRestante.getValue();
        float dizimoAtual = dizimoPendente.getValue();
        // Realiza operações com base no tipo e valor
        switch (tipoDeMovimentacao) {
            case "Entrada":
                // Calcula novos valores
                salarioAtual = salarioAtual + (0.72f * valor);
                dizimoAtual = dizimoAtual + (0.1f * valor);
                // Atualiza os valores no Firestore
                financeRef.update(SALARIO_RESTANTE, salarioAtual,DIZIMO_PENDENTE, dizimoAtual);
                break;

            case "Saída":
                // Calcula novos valores
                salarioAtual = salarioAtual - (0.72f * valor);
                dizimoAtual = dizimoAtual - (0.1f * valor);

                // Atualiza os valores no Firestore
                financeRef.update(SALARIO_RESTANTE, salarioAtual, DIZIMO_PENDENTE, dizimoAtual);
                break;

            case "Dar dízimo":
                dizimoAtual = dizimoAtual - valor;
                financeRef.update(DIZIMO_PENDENTE, dizimoAtual);
                break;

            case "Gasto pessoal":
                salarioAtual = salarioAtual - valor;
                financeRef.update(SALARIO_RESTANTE, salarioAtual);
                break;
        }
        salarioRestante.setValue(salarioAtual);
        dizimoPendente.setValue(dizimoAtual);
    }

    public MutableLiveData<String> addValorAReceber(ValorAReceber valorAReceber){
        List<ValorAReceber> valoresAtuais = valoresAReceber.getValue();
        MutableLiveData<String> mensagem = new MutableLiveData<>();
        if (valoresAtuais == null){
            valoresAtuais = new ArrayList<>();
        }

        db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                .collection(MainActivity.VALORES_A_RECEBER).add(valorAReceber)
                .addOnSuccessListener(documentReference -> {
                    mensagem.setValue("Contato adicionado com sucesso");
                    Log.d("Sucesso ao add", "DocumentSnapshot added");
                })
                .addOnFailureListener(e -> {
                    mensagem.setValue("Erro: " + e.getMessage());
                    Log.w("Falha ao add", "Error adding document", e);
                });
        valoresAtuais.add(valorAReceber);
        valoresAReceber.setValue(valoresAtuais);
        return mensagem;
    }
    public MutableLiveData<String> deletarValorAReceber(ValorAReceber valorAReceber){
        MutableLiveData<String> mensagem = new MutableLiveData<>();
        List<ValorAReceber> valoresAtuais = valoresAReceber.getValue();
        DocumentReference docRef = db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                .collection(MainActivity.VALORES_A_RECEBER).document(valorAReceber.getId());
        docRef.delete()
                .addOnSuccessListener(unused -> {
                    valoresAtuais.remove(valorAReceber);
                    valoresAReceber.setValue(valoresAtuais);
                    mensagem.setValue("Excluído com sucesso");
                })
                .addOnFailureListener(e -> mensagem.setValue("Falha ao excluir"));
        return mensagem;
    }
//    public void excluirMovimentacao(MovimentacaoFinanceira movimentacaoFinanceira){
//        List<MovimentacaoFinanceira> movimentacoesAtuais = new ArrayList<>();
//        db.collection(MainActivity.MOVIMENTACOES_FINANCEIRAS)
//                .document(movimentacaoFinanceira.getID()).delete()
//                .addOnSuccessListener(unused -> {
//                    movimentacoesAtuais.remove(movimentacaoFinanceira);
//                    movimentacoes.setValue(movimentacoesAtuais);
//                });
//    }
}

package com.dizimo_e_salario;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends AndroidViewModel {

    private final FirebaseFirestore db;
    private MutableLiveData<List<MovimentacaoFinanceira>> movimentacoes = new MutableLiveData<>();
    private MutableLiveData<List<ValorAReceber>> valoresAReceber = new MutableLiveData<>();
    private MutableLiveData<String> mensagemDeExclusaoDeValoresAReceber = new MutableLiveData<>();
    private MutableLiveData<String> mensagemDeExclusaoDeMovimentacao = new MutableLiveData<>();
    private MutableLiveData<String> mensagemDeAdicaoDeMovimentacao = new MutableLiveData<>();
    private MutableLiveData<String> mensagemDeAddValorAReceber = new MutableLiveData<>();
    public static final String SALARIO_RESTANTE = "Salário restante";
    public static final String DIZIMO_PENDENTE = "Dízimo pendente";
    private final MutableLiveData<Float>salarioRestante = new MutableLiveData<>();
    private final MutableLiveData<Float>dizimoPendente = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    DocumentReference financeRef;
    private String usuarioLogado = "";

    public SharedViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
    }
    public void carregarViewModel(String usuarioLogado){
        isLoading.setValue(true);
        movimentacoes.setValue(new ArrayList<>());
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
        }
        );
        isLoading.setValue(false);
    }
    public void carregarMovimentacoes(){
        isLoading.setValue(true);
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
        isLoading.setValue(false);
    }
    public void carregarValoresAReceber(){
        isLoading.setValue(true);
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
        isLoading.setValue(false);
    }
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
    public MutableLiveData<List<MovimentacaoFinanceira>> getMovimentacoes(){
        return movimentacoes;
    }

    public MutableLiveData<List<ValorAReceber>> getValoresAReceber() {
        return valoresAReceber;
    }

    public MutableLiveData<String> getMensagemDeAddValorAReceber() {
        return mensagemDeAddValorAReceber;
    }

    public MutableLiveData<String> getMensagemDeExclusaoDeValoresAReceber() {
        return mensagemDeExclusaoDeValoresAReceber;
    }

    public MutableLiveData<String> getMensagemDeAdicaoDeMovimentacao() {
        return mensagemDeAdicaoDeMovimentacao;
    }

    public MutableLiveData<String> getMensagemDeExclusaoDeMovimentacao() {
        return mensagemDeExclusaoDeMovimentacao;
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

    public void addMovimentacaoFinanceira(MovimentacaoFinanceira movimentacaoFinanceira, float valor, String tipo){

        isLoading.setValue(true);
        List<MovimentacaoFinanceira> movsAtuais = movimentacoes.getValue();

        db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                .collection(MainActivity.MOVIMENTACOES_FINANCEIRAS).add(movimentacaoFinanceira)
                .addOnSuccessListener(documentReference -> {
                    isLoading.setValue(false);
                    mensagemDeAdicaoDeMovimentacao.setValue("Movimentação adicionada com sucesso");
                    Log.d("Sucesso ao add", "DocumentSnapshot added");
                    movsAtuais.add(movimentacaoFinanceira);
                    movimentacoes.setValue(movsAtuais);
                    atualizarSalarioEDizimo(valor, tipo);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    mensagemDeAdicaoDeMovimentacao.setValue("Erro: " + e.getMessage());
                    Log.w("Falha ao add", "Error adding document", e);
                });
    }
    public void deletarMovimentacaoFinanceira(MovimentacaoFinanceira movimentacaoFinanceira, float valor, String tipo){
        isLoading.setValue(true);
        List<MovimentacaoFinanceira> movsAtuais = movimentacoes.getValue();
        DocumentReference docRef = db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                .collection(MainActivity.MOVIMENTACOES_FINANCEIRAS).document(movimentacaoFinanceira.getID());
        docRef.delete()
                .addOnSuccessListener(unused -> {
                    isLoading.setValue(false);
                    movsAtuais.remove(movimentacaoFinanceira);
                    movimentacoes.setValue(movsAtuais);
                    atualizarSalarioEDizimo(-valor, tipo);
                    mensagemDeExclusaoDeMovimentacao.setValue("Excluído com sucesso");
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    mensagemDeExclusaoDeMovimentacao.setValue("Erro: " + e.getMessage());
                });
    }
    public void addValorAReceber(ValorAReceber valorAReceber){
        isLoading.setValue(true);
        List<ValorAReceber> valoresAtuais = valoresAReceber.getValue();
        if (valoresAtuais == null){
            valoresAtuais = new ArrayList<>();
        }

        List<ValorAReceber> finalValoresAtuais = valoresAtuais;
        db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                .collection(MainActivity.VALORES_A_RECEBER).add(valorAReceber)
                .addOnSuccessListener(documentReference -> {
                    isLoading.setValue(false);
                    mensagemDeAddValorAReceber.setValue("Valor adicionado com sucesso");
                    Log.d("Sucesso ao add", "DocumentSnapshot added");
                    finalValoresAtuais.add(valorAReceber);
                    valoresAReceber.setValue(finalValoresAtuais);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    mensagemDeAddValorAReceber.setValue("Erro: " + e.getMessage());
                    Log.w("Falha ao add", "Error adding document", e);
                });
    }
    public void deletarValorAReceber(ValorAReceber valorAReceber){
        isLoading.setValue(true);
        List<ValorAReceber> valoresAtuais = valoresAReceber.getValue();
        DocumentReference docRef = db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                .collection(MainActivity.VALORES_A_RECEBER).document(valorAReceber.getId());
        docRef.delete()
                .addOnSuccessListener(unused -> {
                    isLoading.setValue(false);
                    valoresAtuais.remove(valorAReceber);
                    valoresAReceber.setValue(valoresAtuais);
                    mensagemDeExclusaoDeValoresAReceber.setValue("Excluído com sucesso");
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    mensagemDeExclusaoDeValoresAReceber.setValue("Erro: " + e.getMessage());
                });
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

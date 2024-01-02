package com.dizimo_e_salario;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import androidx.lifecycle.MutableLiveData;


import com.dizimo_e_salario.canarinho.formatador.FormatadorValor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class HistoricoDeMovimentacoesViewModel extends AndroidViewModel {

    private final FirebaseFirestore db;
    private final MutableLiveData<List<MovimentacaoFinanceira>> movimentacoes;
    public static final String SALARIO_RESTANTE = "Salário restante";
    public static final String DIZIMO_PENDENTE = "Dízimo pendente";
    private final MutableLiveData<Float>salarioRestante = new MutableLiveData<>();
    private final MutableLiveData<Float>dizimoPendente = new MutableLiveData<>();
    DocumentReference financeRef;

    public HistoricoDeMovimentacoesViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        movimentacoes = carregarMovimentacoes();
        financeRef = db.collection(MainActivity.INFORMACOES_PRINCIPAIS).document(MainActivity.INFORMACOES_PRINCIPAIS);
        carregarInformacoesPrincipais();
    }
    public MutableLiveData<List<MovimentacaoFinanceira>> getMovimentacoes(){
        return movimentacoes;
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
    public MutableLiveData<List<MovimentacaoFinanceira>> carregarMovimentacoes(){
        MutableLiveData<List<MovimentacaoFinanceira>> movims = new MutableLiveData<>();
        db.collection(MainActivity.MOVIMENTACOES_FINANCEIRAS).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        List<MovimentacaoFinanceira> movs = new ArrayList<>();
                        for (QueryDocumentSnapshot document:task.getResult())
                        {
                            MovimentacaoFinanceira mov =document.toObject(MovimentacaoFinanceira.class);
                            mov.setID(document.getId());
                            movs.add(mov);
                        }
                        movims.setValue(movs);
                    }
                });
        return movims;
    }
    private void carregarInformacoesPrincipais (){

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

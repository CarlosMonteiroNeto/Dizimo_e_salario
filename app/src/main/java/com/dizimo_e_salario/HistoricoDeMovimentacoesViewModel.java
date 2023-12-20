package com.dizimo_e_salario;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import androidx.lifecycle.MutableLiveData;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class HistoricoDeMovimentacoesViewModel extends AndroidViewModel {

    private final FirebaseFirestore db;
    private final MutableLiveData<List<MovimentacaoFinanceira>> movimentacoes;

    public HistoricoDeMovimentacoesViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        movimentacoes = carregarMovimentacoes();
    }
    public MutableLiveData<List<MovimentacaoFinanceira>> getMovimentacoes(){
        return movimentacoes;
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
                            movs.add(mov);
                        }
                        movims.setValue(movs);
                    }
                });
        return movims;
    }
}

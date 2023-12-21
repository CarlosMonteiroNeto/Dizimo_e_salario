package com.dizimo_e_salario;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dizimo_e_salario.canarinho.formatador.FormatadorValor;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoricoDeMovimentacoesAdapter extends RecyclerView.Adapter<HistoricoDeMovimentacoesAdapter.HistoricoDeMovimentacoesViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<MovimentacaoFinanceira> movimentacoes;
    public boolean botaoModoIniciarEdicao = true;
    private final SharedPreferences sharedPreferences;
    HistoricoDeMovimentacoesViewModel viewModel;

    public HistoricoDeMovimentacoesAdapter(List<MovimentacaoFinanceira> movimentacoes, SharedPreferences preferences, HistoricoDeMovimentacoesViewModel viewModel){
        this.movimentacoes = movimentacoes;
        this.sharedPreferences = preferences;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public HistoricoDeMovimentacoesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoricoDeMovimentacoesViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_movimentacoes, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoricoDeMovimentacoesViewHolder holder, int position) {
        MovimentacaoFinanceira movimentacaoFinanceira = movimentacoes.get(position);
        String tipo = movimentacaoFinanceira.getTipo();
        String valor = movimentacaoFinanceira.getValor();
        String descricao = movimentacaoFinanceira.getDescricao();
        String data = movimentacaoFinanceira.getData();
        holder.tipo.setText(tipo);
        holder.valor.setText(valor);
        holder.descricao.setText(descricao);
        holder.data.setText(data);

//        holder.btnEditar.setOnClickListener(view -> {
//            if (botaoModoIniciarEdicao){
//                desbloquearEditText(holder.tipo);
//                desbloquearEditText(holder.valor);
//                desbloquearEditText(holder.descricao);
//                botaoModoIniciarEdicao = false;
//            } else {
//                DocumentReference docRef = db.collection(MainActivity.MOVIMENTACOES_FINANCEIRAS)
//                        .document(movimentacaoFinanceira.getID());
//                Map<String, Object> novaMovimentacao = new HashMap<>();
//                novaMovimentacao.put("tipo", tipo);
//                novaMovimentacao.put("valor", valor);
//                novaMovimentacao.put("descricao", descricao);
//                novaMovimentacao.put("data", data);
//                docRef.update(novaMovimentacao)
//                        .addOnSuccessListener(unused -> {
//                            movimentacoes.set(position, movimentacaoFinanceira);
//                            notifyItemChanged(position);
//                            Toast.makeText(view.getContext(), "Atualizado com sucesso", Toast.LENGTH_SHORT).show();
//                        })
//                        .addOnFailureListener(e -> Toast.makeText(view.getContext(), "Falha na atualização. tente novamente", Toast.LENGTH_SHORT).show());
//                botaoModoIniciarEdicao = true;
//            }
//        });

        holder.btnExcluir.setOnClickListener(view -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
            dialogBuilder.setTitle("Atenção!");
            dialogBuilder.setMessage("Deseja excluir esta movimentação financeira?");
            dialogBuilder.setPositiveButton("Sim", (dialog, which) -> {
                List<MovimentacaoFinanceira> movimentacoesAtuais = new ArrayList<>();
                DocumentReference docRef = db.collection(MainActivity.MOVIMENTACOES_FINANCEIRAS)
                        .document(movimentacaoFinanceira.getID());
                docRef.delete()
                        .addOnSuccessListener(unused -> {
                            movimentacoesAtuais.remove(movimentacaoFinanceira);
                            viewModel.setMovimentacoes(movimentacoesAtuais);
                            atualizarSharedPreferencesCasoExclua(tipo, valor);
                            Toast.makeText(view.getContext(), "Excluído com sucesso", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(view.getContext(), "Falha na exclusão. tente novamente", Toast.LENGTH_SHORT).show());
            });
            dialogBuilder.setNegativeButton("Não", (dialogInterface, i) -> dialogInterface.dismiss());
            dialogBuilder.show();

        });
    }

    @Override
    public int getItemCount() {
        return movimentacoes != null ? movimentacoes.size() : 0;
    }
    public void atualizarItens (List < MovimentacaoFinanceira > movimentacoes) {
        this.movimentacoes = movimentacoes;
        notifyDataSetChanged();
    }

    public static class HistoricoDeMovimentacoesViewHolder extends RecyclerView.ViewHolder{
        public TextView tipo, valor, descricao, data;
        public ImageButton btnEditar, btnExcluir;
        public HistoricoDeMovimentacoesViewHolder(@NonNull View itemView) {
            super(itemView);

            tipo =itemView.findViewById(R.id.tipo);
            valor = itemView.findViewById(R.id.valor);
            descricao = itemView.findViewById(R.id.descricao);
            data = itemView.findViewById(R.id.data);
            btnEditar = itemView.findViewById(R.id.botao_editar);
            btnExcluir = itemView.findViewById(R.id.botao_excluir);
        }
    }
    private void bloqueareditText (EditText edittext){
        edittext.setFocusable(false); // Impede o foco
        edittext.setClickable(false); // Impede cliques
        edittext.setCursorVisible(false);
    }
    private void desbloquearEditText (EditText edittext){
        edittext.setFocusable(true);
        edittext.setClickable(true);
        edittext.setCursorVisible(true);
    }
    private void atualizarSharedPreferencesCasoExclua(String tipo, String valor){

        SharedPreferences.Editor editor = sharedPreferences.edit();
        float dizimoPendente = sharedPreferences.getFloat(MainActivity.CHAVE_DIZIMO, MainActivity.DIZIMO_PENDENTE_PADRAO);
        float salarioRestante = sharedPreferences.getFloat(MainActivity.CHAVE_SALARIO, MainActivity.SALARIO_RESTANTE_PADRAO);
        float valorMovimentado = Float.parseFloat(FormatadorValor.VALOR_COM_SIMBOLO.desformata(valor));
//                float saldo = sharedPreferences.getFloat(CHAVE_SALDO, SALDO_PADRAO);

        if (tipo.equals("Saída")) {
//                    saldo = saldo - (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
            dizimoPendente = (dizimoPendente + (valorMovimentado * (float) 0.1));
            salarioRestante = (salarioRestante + (valorMovimentado * (float) 0.72));

        } else if (tipo.equals("Entrada")) {
//                    saldo = saldo + (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
            dizimoPendente = (dizimoPendente - (valorMovimentado * (float) 0.1));
            salarioRestante = (salarioRestante - (valorMovimentado * (float) 0.72));

        } else if (tipo.equals("Dar dízimo")) {
//                    saldo = saldo - (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
            dizimoPendente = dizimoPendente + valorMovimentado;

        } else if (tipo.equals("Gasto pessoal")) {
//                    saldo = saldo - (Float.parseFloat(valorDaMovimentacao.getText().toString())/100);
            salarioRestante = salarioRestante + valorMovimentado;
        }
//                editor.putFloat(CHAVE_SALDO, saldo);
        editor.putFloat(MainActivity.CHAVE_DIZIMO, dizimoPendente);
        editor.putFloat(MainActivity.CHAVE_SALARIO, salarioRestante);
        editor.apply();
    }
}

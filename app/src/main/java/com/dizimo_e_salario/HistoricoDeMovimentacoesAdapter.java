package com.dizimo_e_salario;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoricoDeMovimentacoesAdapter extends RecyclerView.Adapter<HistoricoDeMovimentacoesAdapter.HistoricoDeMovimentacoesViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<MovimentacaoFinanceira> movimentacoes;
    public boolean botaoModoIniciarEdicao = true;

    public HistoricoDeMovimentacoesAdapter(List<MovimentacaoFinanceira> movimentacoes){
        this.movimentacoes = movimentacoes;
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
        bloqueareditText(holder.tipo);
        holder.valor.setText(valor);
        bloqueareditText(holder.valor);
        holder.descricao.setText(descricao);
        bloqueareditText(holder.descricao);
        holder.data.setText(data);

        holder.btnEditar.setOnClickListener(view -> {
            if (botaoModoIniciarEdicao){
                desbloquearEditText(holder.tipo);
                desbloquearEditText(holder.valor);
                desbloquearEditText(holder.descricao);
                botaoModoIniciarEdicao = false;
            } else {
                DocumentReference docRef = db.collection(MainActivity.MOVIMENTACOES_FINANCEIRAS)
                        .document(movimentacaoFinanceira.getID());
                Map<String, Object> novaMovimentacao = new HashMap<>();
                novaMovimentacao.put("tipo", tipo);
                novaMovimentacao.put("valor", valor);
                novaMovimentacao.put("descricao", descricao);
                novaMovimentacao.put("data", data);
                docRef.update(novaMovimentacao)
                        .addOnSuccessListener(unused -> {
                            movimentacoes.set(position, movimentacaoFinanceira);
                            notifyItemChanged(position);
                            Toast.makeText(view.getContext(), "Atualizado com sucesso", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(view.getContext(), "Falha na atualização. tente novamente", Toast.LENGTH_SHORT).show());
                botaoModoIniciarEdicao = true;
            }
        });

        holder.btnExcluir.setOnClickListener(view -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
            dialogBuilder.setTitle("Atenção!");
            dialogBuilder.setMessage("Deseja excluir esta movimentação financeira?");
            dialogBuilder.setPositiveButton("Sim", (dialog, which) -> {
                DocumentReference docRef = db.collection(MainActivity.MOVIMENTACOES_FINANCEIRAS)
                        .document(movimentacaoFinanceira.getID());
                docRef.delete()
                        .addOnSuccessListener(unused -> {
                            movimentacoes.remove(position);
                            notifyItemRemoved(position);
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
        public EditText tipo, valor, descricao;
        TextView data;
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
}

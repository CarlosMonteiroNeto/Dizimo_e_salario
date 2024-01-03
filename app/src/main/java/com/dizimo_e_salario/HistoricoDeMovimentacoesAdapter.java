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

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoricoDeMovimentacoesAdapter extends RecyclerView.Adapter<HistoricoDeMovimentacoesAdapter.HistoricoDeMovimentacoesViewHolder> {
    FirebaseFirestore db;
    private List<MovimentacaoFinanceira> movimentacoes;
    public boolean botaoModoIniciarEdicao = true;
    SharedViewModel viewModel;
    String usuarioLogado;

    public HistoricoDeMovimentacoesAdapter(List<MovimentacaoFinanceira> movimentacoes, FirebaseFirestore db, SharedViewModel viewModel, String usuarioLogado){
        this.movimentacoes = ordenarPorData(movimentacoes);
        this.db = db;
        this.viewModel = viewModel;
        this.usuarioLogado = usuarioLogado;
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
        String id = movimentacaoFinanceira.getID();
        String tipo = movimentacaoFinanceira.getTipo();
        String valor = movimentacaoFinanceira.getValor();
        String descricao = movimentacaoFinanceira.getDescricao();
        long data = movimentacaoFinanceira.getData();
        holder.tipo.setText(tipo);
        holder.valor.setText(valor);
        holder.descricao.setText(descricao);
        holder.data.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(data)));


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
                List<MovimentacaoFinanceira> movimentacoesAtuais = viewModel.getMovimentacoes().getValue();
                DocumentReference docRef = db.collection(LoginActivity.CHAVE_USUARIO).document(usuarioLogado)
                        .collection(MainActivity.MOVIMENTACOES_FINANCEIRAS).document(id);
                docRef.delete()
                        .addOnSuccessListener(unused -> {
                            movimentacoesAtuais.remove(movimentacaoFinanceira);
                            viewModel.setMovimentacoes(movimentacoesAtuais);
                            viewModel.atualizarSalarioEDizimo(-Float.parseFloat(MainActivity.removerMascara(valor)), tipo);
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
        this.movimentacoes = ordenarPorData(movimentacoes);
        notifyDataSetChanged();
    }

    public static class HistoricoDeMovimentacoesViewHolder extends RecyclerView.ViewHolder{
        public TextView tipo, valor, descricao, data;
        public ImageButton btnEditar, btnExcluir;
        public HistoricoDeMovimentacoesViewHolder(@NonNull View itemView) {
            super(itemView);

            tipo =itemView.findViewById(R.id.nome);
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
    public List<MovimentacaoFinanceira> ordenarPorData(List<MovimentacaoFinanceira> listaDesordenada) {
        Comparator<MovimentacaoFinanceira> comparador = Comparator.comparingLong(MovimentacaoFinanceira::getData).reversed();
        Collections.sort(listaDesordenada, comparador);
        return listaDesordenada;
    }
}

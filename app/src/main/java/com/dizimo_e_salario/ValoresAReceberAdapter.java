package com.dizimo_e_salario;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.dizimo_e_salario.canarinho.formatador.FormatadorValor;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ValoresAReceberAdapter extends RecyclerView.Adapter<ValoresAReceberAdapter.ValoresAReceberViewHolder>{

    FirebaseFirestore db;
    private List<ValorAReceber> valoresAReceber;
    public boolean botaoModoIniciarEdicao = true;
    SharedViewModel viewModel;
    String usuarioLogado;

    public ValoresAReceberAdapter(List<ValorAReceber> valoresAReceber, FirebaseFirestore db, SharedViewModel viewModel, String usuarioLogado){
        this.valoresAReceber = ordenarPorData(valoresAReceber);
        this.db = db;
        this.viewModel = viewModel;
        this.usuarioLogado = usuarioLogado;
    }

    @NonNull
    @Override
    public ValoresAReceberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ValoresAReceberViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_valores_a_receber, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ValoresAReceberAdapter.ValoresAReceberViewHolder holder, int position) {
        ValorAReceber valorAReceber = valoresAReceber.get(position);
        String id = valorAReceber.getId();
        String cliente = valorAReceber.getCliente();
        float valor = valorAReceber.getValor();
        String descricao = valorAReceber.getDescricao();
        long data = valorAReceber.getData();
        holder.clienteDevedor.setText(cliente);
        holder.valor.setText(FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(valor/100)));
        holder.descricao.setText(descricao);
        holder.data.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(data)));
        marcarDatasVencidas(data, holder);


//        holder.btnEditar.setOnClickListener(view -> {
//            if (botaoModoIniciarEdicao){
//                desbloquearEditText(holder.tipo);
//                desbloquearEditText(holder.valor);
//                desbloquearEditText(holder.descricao);
//                botaoModoIniciarEdicao = false;
//            } else {
//                DocumentReference docRef = db.collection(MainActivity.MOVIMENTACOES_FINANCEIRAS)
//                        .document(valorAReceber.getID());
//                Map<String, Object> novaMovimentacao = new HashMap<>();
//                novaMovimentacao.put("tipo", tipo);
//                novaMovimentacao.put("valor", valor);
//                novaMovimentacao.put("descricao", descricao);
//                novaMovimentacao.put("data", data);
//                docRef.update(novaMovimentacao)
//                        .addOnSuccessListener(unused -> {
//                            movimentacoes.set(position, valorAReceber);
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
            dialogBuilder.setMessage("Deseja excluir este valor a receber?");
            dialogBuilder.setPositiveButton("Sim", (dialog, which) -> viewModel.deletarValorAReceber(valorAReceber));
            dialogBuilder.setNegativeButton("Não", (dialogInterface, i) -> dialogInterface.dismiss());
            dialogBuilder.show();

        });
    }

    @Override
    public int getItemCount() {
        return valoresAReceber != null ? valoresAReceber.size() : 0;
    }
    public void atualizarItens (List < ValorAReceber > valoresAReceber) {
        this.valoresAReceber = ordenarPorData(valoresAReceber);
        notifyDataSetChanged();
    }

    public static class ValoresAReceberViewHolder extends RecyclerView.ViewHolder{
        public TextView clienteDevedor, valor, descricao, data;
        public ImageButton btnEditar, btnExcluir;
        public ValoresAReceberViewHolder(@NonNull View itemView) {
            super(itemView);

            clienteDevedor =itemView.findViewById(R.id.nome_do_devedor);
            valor = itemView.findViewById(R.id.valor_a_receber);
            descricao = itemView.findViewById(R.id.descricao_para_receber);
            data = itemView.findViewById(R.id.data_para_receber);
            btnEditar = itemView.findViewById(R.id.botao_editar_valor_a_receber);
            btnExcluir = itemView.findViewById(R.id.botao_excluir_valor_a_receber);
        }
    }
    public List<ValorAReceber> ordenarPorData(List<ValorAReceber> listaDesordenada) {
        Comparator<ValorAReceber> comparador = Comparator.comparingLong(ValorAReceber::getData);
        Collections.sort(listaDesordenada, comparador);
        return listaDesordenada;
    }

    private void marcarDatasVencidas(long dataDePagar, ValoresAReceberViewHolder holder){

        Calendar hoje = Calendar.getInstance();
        hoje.add(Calendar.HOUR_OF_DAY, 24);
        long milissegundos24HorasDepois = hoje.getTimeInMillis();

        if (dataDePagar < milissegundos24HorasDepois) {
            holder.itemView.setBackgroundColor(Color.GRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}

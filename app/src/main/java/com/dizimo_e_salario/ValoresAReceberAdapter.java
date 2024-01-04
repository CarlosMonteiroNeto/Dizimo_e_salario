package com.dizimo_e_salario;

import android.app.AlertDialog;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dizimo_e_salario.canarinho.formatador.FormatadorValor;
import com.dizimo_e_salario.canarinho.watcher.ValorMonetarioWatcher;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vicmikhailau.maskededittext.MaskedEditText;

import java.text.ParseException;
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

        holder.btnReceberValor.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Valor total: " + FormatadorValor.VALOR_COM_SIMBOLO.formata(String.valueOf(valor/100)) + ". Qual o valor pago?");

            final EditText input = new EditText(v.getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.addTextChangedListener(new ValorMonetarioWatcher.Builder()
                    .comSimboloReal()
                    .comMantemZerosAoLimpar()
                    .build());
            builder.setView(input);

            builder.setPositiveButton("Confirmar", (dialog, which) -> {
                String inputText = input.getText().toString();
                float valorPago = Float.parseFloat(MainActivity.removerMascara(input.getText().toString()));
                if (inputText.trim().isEmpty() || valorPago > valor) {
                    if (inputText.trim().isEmpty()) {
                        input.setError("O valor está em branco");
                    } else {
                        input.setError("Pagamento maior que a dívida");
                    }
                } else {
                    if (valorPago == valor) {
                        viewModel.deletarValorAReceber(valorAReceber);
                    } else {
                        valorAReceber.setValor(valor-valorPago);
                        viewModel.alterarValorAReceber(position, valorAReceber);
                    }
                    MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira();
                    movimentacao.setTipo("Entrada");
                    movimentacao.setValor(inputText);
                    movimentacao.setDescricao(valorAReceber.getCliente() + " - " + valorAReceber.getDescricao());
                    movimentacao.setData(new Date().getTime());
                    viewModel.addMovimentacaoFinanceira(movimentacao, valorPago, movimentacao.getTipo());
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        holder.btnEditar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Data atual: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(data)) + ". Qual a nova data");

            AttributeSet attrs= new AttributeSet() {
                @Override
                public int getAttributeCount() {
                    return 0;
                }

                @Override
                public String getAttributeName(int index) {
                    return null;
                }

                @Override
                public String getAttributeValue(int index) {
                    return null;
                }

                @Override
                public String getAttributeValue(String namespace, String name) {
                    return null;
                }

                @Override
                public String getPositionDescription() {
                    return null;
                }

                @Override
                public int getAttributeNameResource(int index) {
                    return 0;
                }

                @Override
                public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
                    return 0;
                }

                @Override
                public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
                    return false;
                }

                @Override
                public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
                    return 0;
                }

                @Override
                public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
                    return 0;
                }

                @Override
                public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
                    return 0;
                }

                @Override
                public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
                    return 0;
                }

                @Override
                public int getAttributeListValue(int index, String[] options, int defaultValue) {
                    return 0;
                }

                @Override
                public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
                    return false;
                }

                @Override
                public int getAttributeResourceValue(int index, int defaultValue) {
                    return 0;
                }

                @Override
                public int getAttributeIntValue(int index, int defaultValue) {
                    return 0;
                }

                @Override
                public int getAttributeUnsignedIntValue(int index, int defaultValue) {
                    return 0;
                }

                @Override
                public float getAttributeFloatValue(int index, float defaultValue) {
                    return 0;
                }

                @Override
                public String getIdAttribute() {
                    return null;
                }

                @Override
                public String getClassAttribute() {
                    return null;
                }

                @Override
                public int getIdAttributeResourceValue(int defaultValue) {
                    return 0;
                }

                @Override
                public int getStyleAttribute() {
                    return 0;
                }
            };
            final MaskedEditText input = new MaskedEditText(v.getContext(), attrs);
            input.setMask("##/##/####");
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("Confirmar", (dialog, which) -> {
                String inputText = input.getText().toString();
                if (!AddValorAReceberActivity.EhdataSulamericanaValida(inputText)) {
                    input.setError("Data inválida");
                } else {
                    try {
                        long novaData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .parse(inputText).getTime();
                        valorAReceber.setData(novaData);
                        viewModel.alterarDataDaDivida(position, valorAReceber);
                        dialog.dismiss();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            builder.show();
        });


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
        public ImageButton btnReceberValor, btnEditar, btnExcluir;
        public ValoresAReceberViewHolder(@NonNull View itemView) {
            super(itemView);

            clienteDevedor =itemView.findViewById(R.id.nome_do_devedor);
            valor = itemView.findViewById(R.id.valor_a_receber);
            descricao = itemView.findViewById(R.id.descricao_para_receber);
            data = itemView.findViewById(R.id.data_para_receber);
            btnReceberValor = itemView.findViewById(R.id.botao_receber_valor);
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

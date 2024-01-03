package com.dizimo_e_salario;

import java.io.Serializable;

public class MovimentacaoFinanceira implements Serializable {

    public MovimentacaoFinanceira(){}

    private String ID;
    private String tipo;
    private String valor;
    private String descricao;
    private long data;
    public String getTipo() {
        return tipo;
    }
    public String getID() {
        return ID;
    }
    public void setID(String ID) {
        this.ID = ID;
    }
    public String getValor() {
        return valor;
    }
    public String getDescricao() {
        return descricao;
    }
    public long getData() {
        return data;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public void setValor(String valor) {
        this.valor = valor;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public void setData(long data) {
        this.data = data;
    }
}

package com.dizimo_e_salario;

public class MovimentacaoFinanceira {

    public MovimentacaoFinanceira(){}

    private String ID;
    private String tipo;
    private String valor;
    private String descricao;
    private String data;
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
    public String getData() {
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
    public void setData(String data) {
        this.data = data;
    }
}

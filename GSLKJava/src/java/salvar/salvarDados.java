package salvar;

import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import br.com.tecnicon.server.util.funcoes.Funcoes;
import javax.ejb.Stateless;

/**
 *
 * @author Lucas Kretschmer
 */
@Stateless
public class salvarDados {

    public String salvarDados(VariavelSessao vs) throws ExcecaoTecnicon {
        Funcoes.validaVSNNNome(vs, new String[]{
            "NOME", "Nome"
        }, new String[]{
            "COD", "Código do Cliente"
        }, new String[]{
            "EMAIL", "Email"
        }, new String[]{
            "CEP", "CEP"
        }, new String[]{
            "ENDERECO", "Endereço"
        }, new String[]{
            "BAIRRO", "Bairro"
        }, new String[]{
            "CIDADE", "Cidade"
        }, new String[]{
            "TELEFONE", "Telefone"
        }, new String[]{
            "CELULAR", "Celular"
        }, new String[]{
            "CPF", "CPF"
        });

        TClientDataSet cdsAcesso = TClientDataSet.create(vs, "GSACESSO");
        cdsAcesso.createDataSet();
        cdsAcesso.condicao("WHERE GSACESSO.CCLIFOR = " + vs.getParameter("COD"));
        cdsAcesso.open();
        cdsAcesso.first();

        TClientDataSet cdsCliforend = TClientDataSet.create(vs, "CLIFOREND");
        cdsCliforend.createDataSet();
        cdsCliforend.condicao("WHERE CLIFOREND.CCLIFOR = " + vs.getParameter("COD"));
        cdsCliforend.open();
        cdsCliforend.first();

        TClientDataSet cdsClifor = TClientDataSet.create(vs, "CLIFOR");
        cdsClifor.createDataSet();
        cdsClifor.condicao("WHERE CLIFOR.CCLIFOR = " + vs.getParameter("COD"));
        cdsClifor.open();
        cdsClifor.first();

        cdsClifor.edit();
        cdsClifor.fieldByName("NOME").asString(vs.getParameter("NOME"));
        cdsClifor.post();
        cdsAcesso.edit();
        cdsAcesso.fieldByName("EMAIL").asString(vs.getParameter("EMAIL"));
        cdsAcesso.post();

        if (!cdsClifor.isEmpty()) {
            cdsCliforend.edit();
            cdsCliforend.fieldByName("CCIDADE").asInteger(17);
            cdsCliforend.fieldByName("CCIDADE1").asInteger(17);
            cdsCliforend.fieldByName("CCLIFOR").asInteger(Funcoes.strToInt(vs.getParameter("COD")));
            cdsCliforend.fieldByName("ENDERECO").asString(vs.getParameter("ENDERECO"));
            cdsCliforend.fieldByName("NUMERO").asString(vs.getParameter("NUMERO"));
            cdsCliforend.fieldByName("BAIRRO").asString(vs.getParameter("BAIRRO"));
            cdsCliforend.fieldByName("COMPLEMENTO").asString(vs.getParameter("CIDADE"));
            cdsCliforend.fieldByName("CEP").asString(vs.getParameter("CEP"));
            cdsCliforend.fieldByName("FONE").asInteger(Funcoes.strToInt(vs.getParameter("TELEFONE")));
            cdsCliforend.fieldByName("CGC").asInteger(Funcoes.strToInt(vs.getParameter("CPF")));
            cdsCliforend.fieldByName("CELULAR").asInteger(Funcoes.strToInt(vs.getParameter("CELULAR")));
            cdsCliforend.post();

        }

        return "Registro Inserido com sucesso!";
    }
}
